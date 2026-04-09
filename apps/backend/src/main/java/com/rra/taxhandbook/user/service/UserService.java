package com.rra.taxhandbook.user.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.dto.UserActivityResponse;
import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.auth.dto.InvitePreviewResponse;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.notification.EmailDeliveryService;
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

	private final UserRepository userRepository;
	private final RoleService roleService;
	private final PasswordEncoder passwordEncoder;
	private final EmailDeliveryService emailDeliveryService;
	private final AuditLogService auditLogService;

	public UserService(
		UserRepository userRepository,
		RoleService roleService,
		PasswordEncoder passwordEncoder,
		EmailDeliveryService emailDeliveryService,
		AuditLogService auditLogService
	) {
		this.userRepository = userRepository;
		this.roleService = roleService;
		this.passwordEncoder = passwordEncoder;
		this.emailDeliveryService = emailDeliveryService;
		this.auditLogService = auditLogService;
	}

	public List<UserResponse> getUsers() {
		return getUsers(null, null);
	}

	public List<UserResponse> getUsers(String status, String search) {
		String normalizedStatus = normalizeFilter(status);
		String normalizedSearch = normalizeFilter(search);
		return userRepository.findForAdminList(normalizedStatus, normalizedSearch).stream()
			.map(this::toResponse)
			.toList();
	}

	public List<PendingInviteResponse> getPendingInvites() {
		return userRepository.findByStatusOrderByCreatedAtDesc("INVITED").stream()
			.map(this::toPendingInviteResponse)
			.toList();
	}

	public UserSummaryResponse getUserSummary() {
		return new UserSummaryResponse(
			userRepository.count(),
			userRepository.countByStatusIgnoreCase("ACTIVE"),
			userRepository.countByStatusIgnoreCase("INVITED"),
			userRepository.countByStatusIgnoreCase("SUSPENDED"),
			userRepository.countByStatusIgnoreCase("REMOVED")
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
		if (request.fullName() == null || request.fullName().isBlank()) {
			throw new IllegalArgumentException("Full name is required.");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		String normalizedEmail = request.email().trim().toLowerCase();
		userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new IllegalArgumentException("A system user already exists for email " + normalizedEmail);
			}
		});
		LanguageCode locale = request.preferredLocale() == null ? LanguageCode.EN : request.preferredLocale();
		user.updateProfile(request.fullName().trim(), normalizedEmail, locale);
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

	public ApiResponse<UserResponse> createUser(UserRequest request) {
		return createUserWithPassword(new AdminSetPasswordUserRequest(
			request.fullName(),
			request.email(),
			request.roleName(),
			request.preferredLocale(),
			request.password()
		));
	}

	public ApiResponse<UserResponse> createUserWithPassword(AdminSetPasswordUserRequest request) {
		if (request.password() == null || request.password().isBlank()) {
			throw new IllegalArgumentException("Password is required when creating a local active user.");
		}
		User savedUser = userRepository.save(buildUser(request.fullName(), request.email(), request.roleName(), request.preferredLocale(), "ACTIVE", null, null, passwordEncoder.encode(request.password())));
		return new ApiResponse<>("Local system user created successfully", toResponse(savedUser));
	}

	public ApiResponse<UserInviteResponse> inviteUser(InviteUserRequest request) {
		Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
		String inviteToken = UUID.randomUUID().toString();
		User savedUser = userRepository.save(buildUser(request.fullName(), request.email(), request.roleName(), request.preferredLocale(), "INVITED", inviteToken, expiresAt, null));
		emailDeliveryService.sendInviteEmail(savedUser.getEmail(), savedUser.getFullName(), inviteToken, expiresAt.toString());
		auditLogService.log("USER_INVITED", "system", savedUser.getEmail(), "Invitation created for role " + savedUser.getRole().getName());
		return new ApiResponse<>("User invitation created successfully", new UserInviteResponse(
			savedUser.getId(),
			savedUser.getEmail(),
			inviteToken,
			expiresAt.toString(),
			savedUser.getStatus()
		));
	}

	public ApiResponse<UserResponse> acceptInvite(AcceptInviteRequest request) {
		if (request.token() == null || request.token().isBlank()) {
			throw new IllegalArgumentException("Invitation token is required.");
		}
		if (request.password() == null || request.password().isBlank()) {
			throw new IllegalArgumentException("Password is required.");
		}
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
		if (!"INVITED".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Invite can only be resent for users in INVITED status.");
		}
		Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
		String inviteToken = UUID.randomUUID().toString();
		user.reissueInvite(inviteToken, expiresAt);
		User savedUser = userRepository.save(user);
		emailDeliveryService.sendInviteEmail(savedUser.getEmail(), savedUser.getFullName(), inviteToken, expiresAt.toString());
		auditLogService.log("INVITE_RESENT", actor, savedUser.getEmail(), "Invite token regenerated");
		return new ApiResponse<>("Invite resent successfully", new UserInviteResponse(
			savedUser.getId(),
			savedUser.getEmail(),
			inviteToken,
			expiresAt.toString(),
			savedUser.getStatus()
		));
	}

	public ApiResponse<String> cancelInvite(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		if (!"INVITED".equalsIgnoreCase(user.getStatus())) {
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
		if (!"REMOVED".equalsIgnoreCase(user.getStatus())) {
			throw new IllegalArgumentException("Only removed users can be restored.");
		}
		Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
		String inviteToken = UUID.randomUUID().toString();
		user.reissueInvite(inviteToken, expiresAt);
		User savedUser = userRepository.save(user);
		emailDeliveryService.sendInviteEmail(savedUser.getEmail(), savedUser.getFullName(), inviteToken, expiresAt.toString());
		auditLogService.log("USER_RESTORED", actor, savedUser.getEmail(), "Removed user restored and re-invited");
		return new ApiResponse<>("User restored and re-invited successfully", new UserInviteResponse(
			savedUser.getId(),
			savedUser.getEmail(),
			inviteToken,
			expiresAt.toString(),
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
			return new InvitePreviewResponse(false, false, null, null, null, null, null, "Invite token is required.");
		}
		return userRepository.findByInviteToken(token)
			.map(user -> {
				boolean expired = user.getInviteExpiresAt() == null || user.getInviteExpiresAt().isBefore(Instant.now());
				return new InvitePreviewResponse(
					!expired && "INVITED".equalsIgnoreCase(user.getStatus()),
					expired,
					user.getEmail(),
					user.getFullName(),
					user.getRole().getName(),
					user.getPreferredLocale().name(),
					user.getInviteExpiresAt() == null ? null : user.getInviteExpiresAt().toString(),
					expired ? "Invite token has expired." : "Invite token is valid."
				);
			})
			.orElse(new InvitePreviewResponse(false, false, null, null, null, null, null, "Invite token is invalid."));
	}

	public ApiResponse<String> removeUser(Long id, String actor) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		user.removeAccess();
		userRepository.save(user);
		auditLogService.log("USER_REMOVED", actor, user.getEmail(), "User access removed");
		return new ApiResponse<>("User removed successfully", user.getEmail());
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getUserCode(),
			user.getFullName(),
			user.getEmail(),
			user.getRole().getName(),
			user.getPreferredLocale().name(),
			user.getSource().name(),
			user.getStatus()
		);
	}

	private PendingInviteResponse toPendingInviteResponse(User user) {
		boolean expired = user.getInviteExpiresAt() != null && user.getInviteExpiresAt().isBefore(Instant.now());
		return new PendingInviteResponse(
			user.getId(),
			user.getUserCode(),
			user.getFullName(),
			user.getEmail(),
			user.getRole().getName(),
			user.getPreferredLocale().name(),
			user.getInviteToken(),
			user.getInviteExpiresAt() == null ? null : user.getInviteExpiresAt().toString(),
			expired,
			user.getStatus()
		);
	}

	private User buildUser(
		String fullName,
		String email,
		String roleName,
		LanguageCode preferredLocale,
		String status,
		String inviteToken,
		Instant inviteExpiresAt,
		String passwordHash
	) {
		if (fullName == null || fullName.isBlank()) {
			throw new IllegalArgumentException("Full name is required.");
		}
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		LanguageCode resolvedLocale = preferredLocale == null ? LanguageCode.EN : preferredLocale;
		Role role = roleService.getRoleByName(roleName);
		if (UserRole.PUBLIC.name().equalsIgnoreCase(role.getName())) {
			throw new IllegalArgumentException("PUBLIC cannot be assigned to a local authenticated system user.");
		}

		String normalizedEmail = email.trim().toLowerCase();
		String userCode = generateUserCode(fullName);
		userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for email " + normalizedEmail);
		});
		userRepository.findByUserCode(userCode).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for generated code " + userCode);
		});

		return new User(
			userCode,
			fullName.trim(),
			normalizedEmail,
			passwordHash,
			resolvedLocale,
			UserSource.LOCAL,
			status,
			inviteToken,
			inviteExpiresAt,
			null,
			null,
			Instant.now(),
			role
		);
	}

	private String generateUserCode(String fullName) {
		String normalized = fullName == null ? "user" : fullName.trim().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "");
		if (normalized.isBlank()) {
			normalized = "user";
		}
		return "LOCAL-" + normalized.toUpperCase() + "-" + Instant.now().toEpochMilli();
	}

	private String normalizeFilter(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
