package com.rra.taxhandbook.auth.service;

import org.springframework.stereotype.Service;
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
import com.rra.taxhandbook.notification.EmailDeliveryService;
import com.rra.taxhandbook.security.JwtService;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.service.UserService;
import com.rra.taxhandbook.user.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final EmailDeliveryService emailDeliveryService;
	private final AuditLogService auditLogService;
	private final UserService userService;

	public AuthService(
		JwtService jwtService,
		UserDetailsService userDetailsService,
		PasswordEncoder passwordEncoder,
		UserRepository userRepository,
		EmailDeliveryService emailDeliveryService,
		AuditLogService auditLogService,
		UserService userService
	) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.emailDeliveryService = emailDeliveryService;
		this.auditLogService = auditLogService;
		this.userService = userService;
	}

	public LoginResponse login(LoginRequest request) {
		String username = request.username() == null || request.username().isBlank() ? "admin" : request.username();
		String password = request.password() == null ? "" : request.password();

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new UnauthorizedException("Invalid username or password.");
		}

		String role = userDetails.getAuthorities().stream()
			.findFirst()
			.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
			.orElse("ADMIN");

		return new LoginResponse(username, jwtService.generateToken(username, role), role);
	}

	public ApiResponse<PasswordResetResponse> forgotPassword(ForgotPasswordRequest request) {
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
			.orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + request.email()));
		if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Password reset is only available for active users.");
		}

		Instant expiresAt = Instant.now().plusSeconds(60 * 30);
		String resetToken = UUID.randomUUID().toString();
		user.issuePasswordReset(resetToken, expiresAt);
		userRepository.save(user);
		emailDeliveryService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken, expiresAt.toString());
		auditLogService.log("PASSWORD_RESET_REQUESTED", user.getEmail(), user.getEmail(), "Password reset token generated");

		return new ApiResponse<>("Password reset token generated", new PasswordResetResponse(
			user.getEmail(),
			resetToken,
			expiresAt.toString()
		));
	}

	public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
		if (request.token() == null || request.token().isBlank()) {
			throw new IllegalArgumentException("Reset token is required.");
		}
		if (request.newPassword() == null || request.newPassword().isBlank()) {
			throw new IllegalArgumentException("New password is required.");
		}
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
}
