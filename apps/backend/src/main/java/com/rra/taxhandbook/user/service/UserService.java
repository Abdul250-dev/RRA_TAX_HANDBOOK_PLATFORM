package com.rra.taxhandbook.user.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.dto.UserActivityResponse;
import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.auth.dto.InvitePreviewResponse;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.notification.EmailNotificationQueueService;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.service.RoleService;
import com.rra.taxhandbook.user.dto.AcceptInviteRequest;
import com.rra.taxhandbook.user.dto.AdminSetPasswordUserRequest;
import com.rra.taxhandbook.user.dto.InviteUserRequest;
import com.rra.taxhandbook.user.dto.PendingInviteResponse;
import com.rra.taxhandbook.user.dto.UpdateUserProfileRequest;
import com.rra.taxhandbook.user.dto.UpdateUserRoleRequest;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserInviteResponse;
import com.rra.taxhandbook.user.dto.UserResponse;
import com.rra.taxhandbook.user.dto.UserSummaryResponse;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

@Service
public class UserService {
	private static final String PASSWORD_POLICY_MESSAGE = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character.";

	private final UserRepository userRepository;
	private final RoleService roleService;
	private final PasswordEncoder passwordEncoder;
	private final EmailNotificationQueueService emailNotificationQueueService;
	private final AuditLogService auditLogService;

	@Value("${app.security.expose-sensitive-tokens:false}")
	private boolean exposeSensitiveTokens;

	public UserService(
		UserRepository userRepository,
		RoleService roleService,
		PasswordEncoder passwordEncoder,
		EmailNotificationQueueService emailNotificationQueueService,
		AuditLogService auditLogService
	) {
		this.userRepository = userRepository;
		this.roleService = roleService;
		this.passwordEncoder = passwordEncoder;
		this.emailNotificationQueueService = emailNotificationQueueService;
		this.auditLogService = auditLogService;
	}

	public List<UserResponse> getUsers() {
		return getUsers(null, null);
	}

	public List<UserResponse> getUsers(String status, String search) {
		String normalizedStatus = normalizeFilter(status);
		String normalizedSearch = normalizeFilter(search);
		return userRepository.findAllByOrderByCreatedAtDesc().stream()
			.filter(user -> matchesStatus(user, normalizedStatus))
			.filter(user -> matchesSearch(user, normalizedSearch))
			.map(this::toResponse)
			.toList();
	}

	public List<PendingInviteResponse> getPendingInvites() {
		return userRepository.findByStatusOrderByCreatedAtDesc("PENDING").stream()
			.map(this::toPendingInviteResponse)
			.toList();
	}

	public UserSummaryResponse getUserSummary() {
		return new UserSummaryResponse(
			userRepository.count(),
			userRepository.countByStatusIgnoreCase("ACTIVE"),
			userRepository.countByStatusIgnoreCase("PENDING"),
			userRepository.countByStatusIgnoreCase("SUSPENDED"),
			userRepository.countByStatusIgnoreCase("DEACTIVATED")
		);
	}

	public UserResponse getUserById(Long id) {
		return userRepository.findById(id)
			.map(this::toResponse)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
	}

	public List<UserActivityResponse> getUserActivity(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		return auditLogService.getRecentUserActivity(user.getEmail());
	}

	public ApiResponse<UserResponse> updateUserProfile(Long id, UpdateUserProfileRequest request, String actor) {
		if (request.employeeId() == null || request.employeeId().isBlank()) {
			throw new IllegalArgumentException("Employee ID is required.");
		}
		if (request.firstName() == null || request.firstName().isBlank()) {
			throw new IllegalArgumentException("First name is required.");
		}
		if (request.lastName() == null || request.lastName().isBlank()) {
			throw new IllegalArgumentException("Last name is required.");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		String normalizedEmployeeId = request.employeeId().trim();
		String normalizedEmail = request.email().trim().toLowerCase();
		String normalizedUsername = UsernameGenerator.generate(request.firstName(), normalizedEmployeeId);
		userRepository.findByEmployeeId(normalizedEmployeeId).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new IllegalArgumentException("A system user already exists for employee ID " + normalizedEmployeeId);
			}
		});
		userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new IllegalArgumentException("A system user already exists for email " + normalizedEmail);
			}
		});
		if (normalizedUsername != null) {
			userRepository.findByUsername(normalizedUsername).ifPresent(existing -> {
				if (!existing.getId().equals(id)) {
					throw new IllegalArgumentException("A system user already exists for username " + normalizedUsername);
				}
			});
		}
		LanguageCode locale = request.preferredLocale() == null ? LanguageCode.EN : request.preferredLocale();
		user.updateProfile(
			normalizedEmployeeId,
			request.firstName().trim(),
			request.lastName().trim(),
			normalizedEmail,
			normalizedUsername,
			normalizeOptionalValue(request.phoneNumber()),
			normalizeOptionalValue(request.department()),
			normalizeOptionalValue(request.position()),
			locale,
			resolveActorId(actor)
		);
		User savedUser = userRepository.save(user);
		auditLogService.log("USER_PROFILE_UPDATED", actor, savedUser.getEmail(), "User profile details updated");
		return new ApiResponse<>("User profile updated successfully", toResponse(savedUser));
	}

	public ApiResponse<UserResponse> updateUserRole(Long id, UpdateUserRoleRequest request, String actor) {
		if (request.roleName() == null || request.roleName().isBlank()) {
			throw new IllegalArgumentException("Role name is required.");
		}
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		Role role = roleService.getRoleByName(request.roleName());
		if (UserRole.PUBLIC.name().equalsIgnoreCase(role.getName())) {
			throw new IllegalArgumentException("PUBLIC cannot be assigned to a local authenticated system user.");
		}
		user.assignRole(role);
		User savedUser = userRepository.save(user);
		auditLogService.log("USER_ROLE_UPDATED", actor, savedUser.getEmail(), "Role updated to " + role.getName());
		return new ApiResponse<>("User role updated successfully", toResponse(savedUser));
	}

	public ApiResponse<UserResponse> createUser(UserRequest request, String actor) {
		return createUserWithPassword(new AdminSetPasswordUserRequest(
			request.employeeId(),
			request.firstName(),
			request.lastName(),
			request.email(),
			null,
			request.roleName(),
			request.preferredLocale(),
			request.phoneNumber(),
			request.department(),
			request.position(),
			request.password()
		), actor);
	}

	public ApiResponse<UserResponse> createUser(UserRequest request) {
		return createUser(request, "system");
	}

	public ApiResponse<UserResponse> createUserWithPassword(AdminSetPasswordUserRequest request, String actor) {
		if (request.password() == null || request.password().isBlank()) {
			throw new IllegalArgumentException("Password is required when creating a local active user.");
		}
		validatePasswordStrength(request.password());
		User savedUser = userRepository.save(buildUser(
			request.employeeId(),
			request.firstName(),
			request.lastName(),
			request.email(),
			null,
			request.roleName(),
			request.preferredLocale(),
			request.phoneNumber(),
			request.department(),
			request.position(),
			"ACTIVE",
			null,
			null,
			passwordEncoder.encode(request.password()),
			resolveActorId(actor)
		));
		auditLogService.log("USER_CREATED", actor, savedUser.getEmail(), "Local system user created");
		return new ApiResponse<>("Local system user created successfully", toResponse(savedUser));
	}

	public ApiResponse<UserResponse> createUserWithPassword(AdminSetPasswordUserRequest request) {
		return createUserWithPassword(request, "system");
	}

	public ApiResponse<UserInviteResponse> inviteUser(InviteUserRequest request, String actor) {
		Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
		String inviteToken = UUID.randomUUID().toString();
		User savedUser = userRepository.save(buildUser(
			request.employeeId(),
			request.firstName(),
			request.lastName(),
			request.email(),
			null,
			request.roleName(),
			request.preferredLocale(),
			request.phoneNumber(),
			request.department(),
			request.position(),
			"PENDING",
			inviteToken,
			expiresAt,
			null,
			resolveActorId(actor)
		));
		sendInviteEmail(savedUser, inviteToken, expiresAt, actor, "USER_INVITED");
		auditLogService.log("USER_INVITED", actor, savedUser.getEmail(), "Invitation created for role " + savedUser.getRole().getName());
		return new ApiResponse<>("User invitation created successfully", new UserInviteResponse(
			savedUser.getId(),
			savedUser.getEmail(),
			exposeSensitiveTokens ? inviteToken : null,
			exposeSensitiveTokens ? expiresAt.toString() : null,
			savedUser.getStatus()
		));
	}

	public ApiResponse<UserInviteResponse> inviteUser(InviteUserRequest request) {
		return inviteUser(request, "system");
	}

	public ApiResponse<UserResponse> acceptInvite(AcceptInviteRequest request) {
		if (request.token() == null || request.token().isBlank()) {
			throw new IllegalArgumentException("Invitation token is required.");
		}
		if (request.password() == null || request.password().isBlank()) {
			throw new IllegalArgumentException("Password is required.");
		}
		validatePasswordStrength(request.password());
		User user = userRepository.findByInviteToken(request.token())
			.orElseThrow(() -> new ResourceNotFoundException("Invitation token is invalid."));
		if (user.getInviteExpiresAt() == null || user.getInviteExpiresAt().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Invitation token has expired.");
		}
		user.activateWithPassword(passwordEncoder.encode(request.password()));
		auditLogService.log("INVITE_ACCEPTED", user.getEmail(), user.getEmail(), "Invitation accepted and account activated");
		return new ApiResponse<>("Invitation accepted successfully", toResponse(userRepository.save(user)));
	}

	public ApiResponse<UserInviteResponse> resendInvite(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		if (!"PENDING".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Invite can only be resent for users in PENDING status.");
		}
		Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
		String inviteToken = UUID.randomUUID().toString();
		user.reissueInvite(inviteToken, expiresAt);
		User savedUser = userRepository.save(user);
		sendInviteEmail(savedUser, inviteToken, expiresAt, actor, "INVITE_RESENT");
		auditLogService.log("INVITE_RESENT", actor, savedUser.getEmail(), "Invite token regenerated");
		return new ApiResponse<>("Invite resent successfully", new UserInviteResponse(
			savedUser.getId(),
			savedUser.getEmail(),
			exposeSensitiveTokens ? inviteToken : null,
			exposeSensitiveTokens ? expiresAt.toString() : null,
			savedUser.getStatus()
		));
	}

	public ApiResponse<String> cancelInvite(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		if (!"PENDING".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Only pending invites can be cancelled.");
		}
		user.cancelInvite();
		userRepository.save(user);
		auditLogService.log("INVITE_CANCELLED", actor, user.getEmail(), "Pending invite cancelled");
		return new ApiResponse<>("Invite cancelled successfully", user.getEmail());
	}

	public ApiResponse<UserInviteResponse> restoreUser(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		if (!"DEACTIVATED".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Only deactivated users can be restored.");
		}
		Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
		String inviteToken = UUID.randomUUID().toString();
		user.reissueInvite(inviteToken, expiresAt);
		User savedUser = userRepository.save(user);
		sendInviteEmail(savedUser, inviteToken, expiresAt, actor, "USER_RESTORED");
		auditLogService.log("USER_RESTORED", actor, savedUser.getEmail(), "Deactivated user restored and re-invited");
		return new ApiResponse<>("User restored and re-invited successfully", new UserInviteResponse(
			savedUser.getId(),
			savedUser.getEmail(),
			exposeSensitiveTokens ? inviteToken : null,
			exposeSensitiveTokens ? expiresAt.toString() : null,
			savedUser.getStatus()
		));
	}

	public ApiResponse<UserResponse> suspendUser(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Only active users can be suspended.");
		}
		user.suspendAccess();
		User savedUser = userRepository.save(user);
		auditLogService.log("USER_SUSPENDED", actor, savedUser.getEmail(), "User access temporarily suspended");
		return new ApiResponse<>("User suspended successfully", toResponse(savedUser));
	}

	public ApiResponse<UserResponse> reactivateUser(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		if (!"SUSPENDED".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Only suspended users can be reactivated.");
		}
		if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
			throw new IllegalArgumentException("Suspended user cannot be reactivated without a password.");
		}
		user.reactivateAccess();
		User savedUser = userRepository.save(user);
		auditLogService.log("USER_REACTIVATED", actor, savedUser.getEmail(), "Suspended user reactivated");
		return new ApiResponse<>("User reactivated successfully", toResponse(savedUser));
	}

	public InvitePreviewResponse previewInviteToken(String token) {
		if (token == null || token.isBlank()) {
			return new InvitePreviewResponse(false, false, null, null, null, null, null, null, "Invite token is required.");
		}
		return userRepository.findByInviteToken(token)
			.map(user -> {
				boolean expired = user.getInviteExpiresAt() == null || user.getInviteExpiresAt().isBefore(Instant.now());
				return new InvitePreviewResponse(
					!expired && "PENDING".equalsIgnoreCase(user.getStatus()),
					expired,
					user.getEmail(),
					user.getFullName(),
					user.getUsername(),
					user.getRole().getName(),
					user.getPreferredLocale().name(),
					user.getInviteExpiresAt() == null ? null : user.getInviteExpiresAt().toString(),
					expired ? "Invite token has expired." : "Invite token is valid."
				);
			})
			.orElse(new InvitePreviewResponse(false, false, null, null, null, null, null, null, "Invite token is invalid."));
	}

	public ApiResponse<String> removeUser(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		user.removeAccess();
		userRepository.save(user);
		auditLogService.log("USER_REMOVED", actor, user.getEmail(), "User access deactivated");
		return new ApiResponse<>("User deactivated successfully", user.getEmail());
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmployeeId(),
			user.getUserCode(),
			user.getUsername(),
			user.getFirstName(),
			user.getLastName(),
			user.getFullName(),
			user.getEmail(),
			user.getPhoneNumber(),
			user.getDepartment(),
			user.getPosition(),
			user.getRole().getName(),
			user.getPreferredLocale().name(),
			user.getSource().name(),
			user.getStatus(),
			user.isActive(),
			user.isLocked(),
			user.getFailedLoginAttempts(),
			user.getLastLoginAt() == null ? null : user.getLastLoginAt().toString()
		);
	}

	private void sendInviteEmail(User user, String inviteToken, Instant expiresAt, String actor, String action) {
		emailNotificationQueueService.queueInviteEmail(
			user.getEmail(),
			user.getFullName(),
			user.getUsername(),
			inviteToken,
			expiresAt,
			actor,
			action
		);
	}

	private PendingInviteResponse toPendingInviteResponse(User user) {
		boolean expired = user.getInviteExpiresAt() != null && user.getInviteExpiresAt().isBefore(Instant.now());
		return new PendingInviteResponse(
			user.getId(),
			user.getEmployeeId(),
			user.getUserCode(),
			user.getUsername(),
			user.getFirstName(),
			user.getLastName(),
			user.getFullName(),
			user.getEmail(),
			user.getPhoneNumber(),
			user.getDepartment(),
			user.getPosition(),
			user.getRole().getName(),
			user.getPreferredLocale().name(),
			user.getInviteToken(),
			user.getInviteExpiresAt() == null ? null : user.getInviteExpiresAt().toString(),
			expired,
			user.getStatus()
		);
	}

	private User buildUser(
		String employeeId,
		String firstName,
		String lastName,
		String email,
		String username,
		String roleName,
		LanguageCode preferredLocale,
		String phoneNumber,
		String department,
		String position,
		String status,
		String inviteToken,
		Instant inviteExpiresAt,
		String passwordHash,
		Long createdBy
	) {
		if (employeeId == null || employeeId.isBlank()) {
			throw new IllegalArgumentException("Employee ID is required.");
		}
		if (firstName == null || firstName.isBlank()) {
			throw new IllegalArgumentException("First name is required.");
		}
		if (lastName == null || lastName.isBlank()) {
			throw new IllegalArgumentException("Last name is required.");
		}
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		LanguageCode resolvedLocale = preferredLocale == null ? LanguageCode.EN : preferredLocale;
		Role role = roleService.getRoleByName(roleName);
		if (UserRole.PUBLIC.name().equalsIgnoreCase(role.getName())) {
			throw new IllegalArgumentException("PUBLIC cannot be assigned to a local authenticated system user.");
		}

		String normalizedEmployeeId = employeeId.trim();
		String normalizedEmail = email.trim().toLowerCase();
		String normalizedUsername = UsernameGenerator.generate(firstName, normalizedEmployeeId);
		userRepository.findByEmployeeId(normalizedEmployeeId).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for employee ID " + normalizedEmployeeId);
		});
		userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for email " + normalizedEmail);
		});
		if (normalizedUsername != null) {
			userRepository.findByUsername(normalizedUsername).ifPresent(existing -> {
				throw new IllegalArgumentException("A system user already exists for username " + normalizedUsername);
			});
		}

		return new User(
			normalizedEmployeeId,
			firstName.trim(),
			lastName.trim(),
			normalizedEmail,
			normalizedUsername,
			passwordHash,
			resolvedLocale,
			UserSource.LOCAL,
			status,
			"ACTIVE".equalsIgnoreCase(status),
			false,
			0,
			null,
			inviteToken,
			inviteExpiresAt,
			null,
			null,
			normalizeOptionalValue(phoneNumber),
			normalizeOptionalValue(department),
			normalizeOptionalValue(position),
			Instant.now(),
			null,
			null,
			createdBy,
			null,
			role
		);
	}

	private String normalizeFilter(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean matchesStatus(User user, String status) {
		return status == null || user.getStatus().equalsIgnoreCase(status);
	}

	private boolean matchesSearch(User user, String search) {
		if (search == null) {
			return true;
		}

		String normalizedSearch = search.toLowerCase();
		return user.getEmployeeId().toLowerCase().contains(normalizedSearch)
			|| user.getFirstName().toLowerCase().contains(normalizedSearch)
			|| user.getLastName().toLowerCase().contains(normalizedSearch)
			|| user.getFullName().toLowerCase().contains(normalizedSearch)
			|| user.getEmail().toLowerCase().contains(normalizedSearch)
			|| (user.getUsername() != null && user.getUsername().toLowerCase().contains(normalizedSearch));
	}

	private String normalizeOptionalValue(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Long resolveActorId(String actor) {
		if (actor == null || actor.isBlank()) {
			return null;
		}
		String normalizedActor = actor.trim().toLowerCase();
		return userRepository.findByEmail(normalizedActor)
			.or(() -> userRepository.findByUsername(normalizedActor))
			.map(User::getId)
			.orElse(null);
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
}
