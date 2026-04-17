package com.rra.taxhandbook.auth.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.auth.dto.ForgotPasswordRequest;
import com.rra.taxhandbook.auth.dto.InvitePreviewResponse;
import com.rra.taxhandbook.auth.dto.LoginRequest;
import com.rra.taxhandbook.auth.dto.LoginResponse;
import com.rra.taxhandbook.auth.dto.PasswordResetResponse;
import com.rra.taxhandbook.auth.dto.ResetPasswordRequest;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.notification.EmailNotificationQueueService;
import com.rra.taxhandbook.security.JwtService;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.service.UserService;
import com.rra.taxhandbook.user.repository.UserRepository;

@Service
public class AuthService {

	private static final String GENERIC_FORGOT_PASSWORD_MESSAGE = "If an active account exists for this email, password reset instructions have been sent.";
	private static final String PASSWORD_POLICY_MESSAGE = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character.";

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final EmailNotificationQueueService emailNotificationQueueService;
	private final AuditLogService auditLogService;
	private final UserService userService;

	@Value("${app.security.expose-sensitive-tokens:false}")
	private boolean exposeSensitiveTokens;

	public AuthService(
		JwtService jwtService,
		UserDetailsService userDetailsService,
		PasswordEncoder passwordEncoder,
		UserRepository userRepository,
		EmailNotificationQueueService emailNotificationQueueService,
		AuditLogService auditLogService,
		UserService userService
	) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.emailNotificationQueueService = emailNotificationQueueService;
		this.auditLogService = auditLogService;
		this.userService = userService;
	}

	public LoginResponse login(LoginRequest request) {
		if (request.username() == null || request.username().isBlank()) {
			throw new UnauthorizedException("Invalid username or password.");
		}
		String username = request.username().trim();
		String password = request.password() == null ? "" : request.password();
		String normalizedUsername = username.toLowerCase();

		User localUser = userRepository.findByUsername(normalizedUsername).orElse(null);
		if (localUser != null) {
			if (!"ACTIVE".equalsIgnoreCase(localUser.getStatus())
				|| !localUser.isActive()
				|| localUser.isLocked()
				|| localUser.getPasswordHash() == null
				|| localUser.getPasswordHash().isBlank()) {
				throw new UnauthorizedException("Invalid username or password.");
			}
			if (!passwordEncoder.matches(password, localUser.getPasswordHash())) {
				localUser.recordFailedLoginAttempt();
				userRepository.save(localUser);
				throw new UnauthorizedException("Invalid username or password.");
			}

			localUser.recordSuccessfulLogin();
			userRepository.save(localUser);
			return new LoginResponse(
				localUser.getUsername(),
				jwtService.generateToken(
					localUser.getUsername(),
					localUser.getRole().getName()
				),
				localUser.getRole().getName()
			);
		}

		UserDetails userDetails;
		try {
			userDetails = userDetailsService.loadUserByUsername(username);
		}
		catch (UsernameNotFoundException ex) {
			throw new UnauthorizedException("Invalid username or password.");
		}
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new UnauthorizedException("Invalid username or password.");
		}

		String role = userDetails.getAuthorities().stream()
			.findFirst()
			.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
			.orElse("ADMIN");

		return new LoginResponse(request.username(), jwtService.generateToken(request.username(), role), role);
	}

	public ApiResponse<PasswordResetResponse> forgotPassword(ForgotPasswordRequest request) {
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		String normalizedEmail = request.email().trim().toLowerCase();
		User user = userRepository.findByEmail(normalizedEmail).orElse(null);
		if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			return new ApiResponse<>(GENERIC_FORGOT_PASSWORD_MESSAGE, new PasswordResetResponse(
				normalizedEmail,
				null,
				null
			));
		}

		Instant expiresAt = Instant.now().plusSeconds(60 * 30);
		String resetToken = UUID.randomUUID().toString();
		user.issuePasswordReset(resetToken, expiresAt);
		userRepository.save(user);
		sendPasswordResetEmail(user, resetToken, expiresAt);
		auditLogService.log("PASSWORD_RESET_REQUESTED", user.getEmail(), user.getEmail(), "Password reset token generated");

		return new ApiResponse<>(GENERIC_FORGOT_PASSWORD_MESSAGE, new PasswordResetResponse(
			user.getEmail(),
			exposeSensitiveTokens ? resetToken : null,
			exposeSensitiveTokens ? expiresAt.toString() : null
		));
	}

	public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
		if (request.token() == null || request.token().isBlank()) {
			throw new IllegalArgumentException("Reset token is required.");
		}
		if (request.newPassword() == null || request.newPassword().isBlank()) {
			throw new IllegalArgumentException("New password is required.");
		}
		validatePasswordStrength(request.newPassword());
		User user = userRepository.findByPasswordResetToken(request.token())
			.orElseThrow(() -> new ResourceNotFoundException("Password reset token is invalid."));
		if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Password reset token has expired.");
		}
		user.resetPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
		auditLogService.log("PASSWORD_RESET_COMPLETED", user.getEmail(), user.getEmail(), "Password reset completed");
		return new ApiResponse<>("Password reset successfully", user.getEmail());
	}

	public InvitePreviewResponse previewInviteToken(String token) {
		return userService.previewInviteToken(token);
	}

	private void validatePasswordStrength(String password) {
		boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
		boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
		boolean hasDigit = password.chars().anyMatch(Character::isDigit);
		boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
		if (password.length() < 8 || !hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
			throw new IllegalArgumentException(PASSWORD_POLICY_MESSAGE);
		}
	}

	private void sendPasswordResetEmail(User user, String resetToken, Instant expiresAt) {
		emailNotificationQueueService.queuePasswordResetEmail(user.getEmail(), user.getFullName(), resetToken, expiresAt, user.getEmail());
	}
}
