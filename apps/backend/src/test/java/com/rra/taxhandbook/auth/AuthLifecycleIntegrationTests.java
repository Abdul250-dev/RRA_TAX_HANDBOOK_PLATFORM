package com.rra.taxhandbook.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import com.rra.taxhandbook.audit.repository.AuditLogRepository;
import com.rra.taxhandbook.auth.dto.ForgotPasswordRequest;
import com.rra.taxhandbook.auth.dto.LoginRequest;
import com.rra.taxhandbook.auth.dto.ResetPasswordRequest;
import com.rra.taxhandbook.auth.service.AuthService;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.exception.NotificationDeliveryException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.notification.EmailDeliveryService;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.dto.AcceptInviteRequest;
import com.rra.taxhandbook.user.dto.AdminSetPasswordUserRequest;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;
import com.rra.taxhandbook.user.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
class AuthLifecycleIntegrationTests {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private EmailDeliveryService emailDeliveryService;

	private Role adminRole;

	@BeforeEach
	void setUp() {
		auditLogRepository.deleteAll();
		userRepository.deleteAll();
		roleRepository.deleteAll();
		adminRole = roleRepository.save(new Role("ADMIN", "Administrative role"));
	}

	@Test
	void acceptInviteActivatesUserAndClearsInviteToken() {
		User invitedUser = userRepository.save(new User(
			"LOCAL-INVITED-1",
			"Invited User",
			"invited@rra.test",
			null,
			LanguageCode.EN,
			UserSource.LOCAL,
			"PENDING",
			"invite-token-1",
			Instant.now().plusSeconds(3600),
			null,
			null,
			Instant.now(),
			adminRole
		));

		var response = userService.acceptInvite(new AcceptInviteRequest("invite-token-1", "StrongPass!123"));

		assertEquals("ACTIVE", response.data().status());
		User savedUser = userRepository.findById(invitedUser.getId()).orElseThrow();
		assertEquals("ACTIVE", savedUser.getStatus());
		assertNotNull(savedUser.getPasswordHash());
		assertTrue(passwordEncoder.matches("StrongPass!123", savedUser.getPasswordHash()));
		assertNull(savedUser.getInviteToken());
		assertNull(savedUser.getInviteExpiresAt());
		assertAuditLog("INVITE_ACCEPTED", "invited@rra.test", "invited@rra.test");
	}

	@Test
	void acceptInviteRejectsExpiredToken() {
		userRepository.save(new User(
			"LOCAL-INVITED-2",
			"Expired Invite User",
			"expired-invite@rra.test",
			null,
			LanguageCode.EN,
			UserSource.LOCAL,
			"PENDING",
			"invite-token-expired",
			Instant.now().minusSeconds(60),
			null,
			null,
			Instant.now(),
			adminRole
		));

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> userService.acceptInvite(new AcceptInviteRequest("invite-token-expired", "StrongPass!123"))
		);

		assertEquals("Invitation token has expired.", exception.getMessage());
	}

	@Test
	void acceptInviteRejectsWeakPassword() {
		userRepository.save(new User(
			"LOCAL-INVITED-4",
			"Weak Password Invite User",
			"weak-invite@rra.test",
			null,
			LanguageCode.EN,
			UserSource.LOCAL,
			"PENDING",
			"invite-token-weak",
			Instant.now().plusSeconds(3600),
			null,
			null,
			Instant.now(),
			adminRole
		));

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> userService.acceptInvite(new AcceptInviteRequest("invite-token-weak", "weakpass"))
		);

		assertEquals("Password must be at least 8 characters and include uppercase, lowercase, number, and special character.", exception.getMessage());
	}

	@Test
	void acceptInviteRejectsReusedTokenAfterSuccessfulActivation() {
		userRepository.save(new User(
			"LOCAL-INVITED-3",
			"Replay Invite User",
			"replay-invite@rra.test",
			null,
			LanguageCode.EN,
			UserSource.LOCAL,
			"PENDING",
			"invite-token-replay",
			Instant.now().plusSeconds(3600),
			null,
			null,
			Instant.now(),
			adminRole
		));

		userService.acceptInvite(new AcceptInviteRequest("invite-token-replay", "StrongPass!123"));

		var exception = assertThrows(
			com.rra.taxhandbook.common.exception.ResourceNotFoundException.class,
			() -> userService.acceptInvite(new AcceptInviteRequest("invite-token-replay", "AnotherPass!456"))
		);

		assertEquals("Invitation token is invalid.", exception.getMessage());
	}

	@Test
	void invitePreviewIncludesGeneratedUsername() {
		userRepository.save(new User(
			"LOCAL-INVITED-PREVIEW",
			"Preview",
			"Invite User",
			"preview-invite@rra.test",
			"prelocalinvitedpreview",
			null,
			LanguageCode.EN,
			UserSource.LOCAL,
			"PENDING",
			false,
			false,
			0,
			null,
			"invite-preview-token",
			Instant.now().plusSeconds(3600),
			null,
			null,
			null,
			null,
			null,
			Instant.now(),
			null,
			null,
			null,
			null,
			adminRole
		));

		var preview = userService.previewInviteToken("invite-preview-token");

		assertTrue(preview.valid());
		assertFalse(preview.expired());
		assertEquals("preview-invite@rra.test", preview.email());
		assertEquals("Preview Invite User", preview.fullName());
		assertEquals("prelocalinvitedpreview", preview.username());
		assertEquals("ADMIN", preview.roleName());
		assertEquals("EN", preview.preferredLocale());
		assertNotNull(preview.expiresAt());
		assertEquals("Invite token is valid.", preview.message());
	}

	@Test
	void forgotPasswordIssuesResetTokenForActiveUser() {
		userRepository.save(new User(
			"LOCAL-ACTIVE-1",
			"Active User",
			"active@rra.test",
			passwordEncoder.encode("CurrentPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			null,
			null,
			Instant.now(),
			adminRole
		));

		var response = authService.forgotPassword(new ForgotPasswordRequest("active@rra.test"));

		assertEquals("active@rra.test", response.data().email());
		assertNull(response.data().resetToken());
		assertNull(response.data().expiresAt());
		User savedUser = userRepository.findByEmail("active@rra.test").orElseThrow();
		assertNotNull(savedUser.getPasswordResetToken());
		assertNotNull(savedUser.getPasswordResetExpiresAt());
		verify(emailDeliveryService).sendPasswordResetEmail(anyString(), anyString(), anyString(), anyString());
		assertAuditLog("PASSWORD_RESET_REQUESTED", "active@rra.test", "active@rra.test");
	}

	@Test
	void forgotPasswordFailsClearlyWhenResetEmailDeliveryFails() {
		userRepository.save(new User(
			"LOCAL-ACTIVE-FAIL-1",
			"Mail Failure User",
			"mail-failure@rra.test",
			passwordEncoder.encode("CurrentPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			null,
			null,
			Instant.now(),
			adminRole
		));
		doThrow(new RuntimeException("SMTP unavailable"))
			.when(emailDeliveryService)
			.sendPasswordResetEmail(anyString(), anyString(), anyString(), anyString());

		var exception = assertThrows(
			NotificationDeliveryException.class,
			() -> authService.forgotPassword(new ForgotPasswordRequest("mail-failure@rra.test"))
		);

		assertEquals(
			"Password reset token was created but the reset email could not be sent. Retry the request or review mail delivery configuration.",
			exception.getMessage()
		);
		assertAuditLog("PASSWORD_RESET_EMAIL_FAILED", "mail-failure@rra.test", "mail-failure@rra.test");
	}

	@Test
	void loginSucceedsForActiveUser() {
		userRepository.save(new User(
			"LOCAL-LOGIN-1",
			"Login",
			"User",
			"login@rra.test",
			"loglocallogin1",
			passwordEncoder.encode("CurrentPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			true,
			false,
			0,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			Instant.now(),
			null,
			null,
			null,
			null,
			adminRole
		));

		var response = authService.login(new LoginRequest("loglocallogin1", "CurrentPass!123"));

		assertEquals("loglocallogin1", response.username());
		assertEquals("ADMIN", response.role());
		assertNotNull(response.token());
	}

	@ParameterizedTest
	@ValueSource(strings = { "ADMIN", "EDITOR", "REVIEWER", "PUBLISHER", "AUDITOR", "CONTENT_OFFICER", "VIEWER" })
	void loginSucceedsForActiveUserWithAssignedRole(String roleName) {
		Role role = roleRepository.findByName(roleName)
			.orElseGet(() -> roleRepository.save(new Role(roleName, roleName + " test role")));
		String username = roleName.toLowerCase().replace("_", "") + "login";
		userRepository.save(new User(
			"LOCAL-ROLE-" + roleName,
			roleName,
			"User",
			roleName.toLowerCase() + "@rra.test",
			username,
			passwordEncoder.encode("CurrentPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			true,
			false,
			0,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			Instant.now(),
			null,
			null,
			null,
			null,
			role
		));

		var response = authService.login(new LoginRequest(username, "CurrentPass!123"));

		assertEquals(username, response.username());
		assertEquals(roleName, response.role());
		assertNotNull(response.token());
	}

	@Test
	void loginRejectsSuspendedUser() {
		userRepository.save(new User(
			"LOCAL-LOGIN-2",
			"Suspended",
			"Login User",
			"suspended-login@rra.test",
			"suslocallogin2",
			passwordEncoder.encode("CurrentPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"SUSPENDED",
			false,
			false,
			0,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			Instant.now(),
			null,
			null,
			null,
			null,
			adminRole
		));

		var exception = assertThrows(
			UnauthorizedException.class,
			() -> authService.login(new LoginRequest("suslocallogin2", "CurrentPass!123"))
		);

		assertEquals("Invalid username or password.", exception.getMessage());
	}

	@Test
	void loginRejectsRemovedUser() {
		userRepository.save(new User(
			"LOCAL-LOGIN-3",
			"Removed",
			"Login User",
			"removed-login@rra.test",
			"remlocallogin3",
			passwordEncoder.encode("CurrentPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"DEACTIVATED",
			false,
			false,
			0,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			Instant.now(),
			null,
			Instant.now(),
			null,
			null,
			adminRole
		));

		var exception = assertThrows(
			UnauthorizedException.class,
			() -> authService.login(new LoginRequest("remlocallogin3", "CurrentPass!123"))
		);

		assertEquals("Invalid username or password.", exception.getMessage());
	}

	@Test
	void loginRejectsBlankUsernameInsteadOfFallingBack() {
		var exception = assertThrows(
			UnauthorizedException.class,
			() -> authService.login(new LoginRequest("   ", "whatever"))
		);

		assertEquals("Invalid username or password.", exception.getMessage());
	}

	@Test
	void forgotPasswordDoesNotRevealUnknownUser() {
		var response = authService.forgotPassword(new ForgotPasswordRequest("unknown@rra.test"));

		assertEquals("unknown@rra.test", response.data().email());
		assertNull(response.data().resetToken());
		assertNull(response.data().expiresAt());
	}

	@Test
	void resetPasswordUpdatesHashAndClearsResetToken() {
		userRepository.save(new User(
			"LOCAL-ACTIVE-2",
			"Reset User",
			"reset@rra.test",
			passwordEncoder.encode("OldPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			"reset-token-1",
			Instant.now().plusSeconds(1800),
			Instant.now(),
			adminRole
		));

		var response = authService.resetPassword(new ResetPasswordRequest("reset-token-1", "NewPass!456"));

		assertEquals("reset@rra.test", response.data());
		User savedUser = userRepository.findByEmail("reset@rra.test").orElseThrow();
		assertTrue(passwordEncoder.matches("NewPass!456", savedUser.getPasswordHash()));
		assertNull(savedUser.getPasswordResetToken());
		assertNull(savedUser.getPasswordResetExpiresAt());
		assertAuditLog("PASSWORD_RESET_COMPLETED", "reset@rra.test", "reset@rra.test");
	}

	@Test
	void resetPasswordRejectsExpiredToken() {
		userRepository.save(new User(
			"LOCAL-ACTIVE-3",
			"Expired Reset User",
			"expired-reset@rra.test",
			passwordEncoder.encode("OldPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			"reset-token-expired",
			Instant.now().minusSeconds(30),
			Instant.now(),
			adminRole
		));

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> authService.resetPassword(new ResetPasswordRequest("reset-token-expired", "NewPass!456"))
		);

		assertEquals("Password reset token has expired.", exception.getMessage());
		User savedUser = userRepository.findByEmail("expired-reset@rra.test").orElseThrow();
		assertEquals("reset-token-expired", savedUser.getPasswordResetToken());
		assertFalse(passwordEncoder.matches("NewPass!456", savedUser.getPasswordHash()));
	}

	@Test
	void resetPasswordRejectsWeakPassword() {
		userRepository.save(new User(
			"LOCAL-ACTIVE-5",
			"Weak Reset User",
			"weak-reset@rra.test",
			passwordEncoder.encode("OldPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			"reset-token-weak",
			Instant.now().plusSeconds(1800),
			Instant.now(),
			adminRole
		));

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> authService.resetPassword(new ResetPasswordRequest("reset-token-weak", "weakpass"))
		);

		assertEquals("Password must be at least 8 characters and include uppercase, lowercase, number, and special character.", exception.getMessage());
	}

	@Test
	void resetPasswordRejectsReusedTokenAfterSuccessfulReset() {
		userRepository.save(new User(
			"LOCAL-ACTIVE-4",
			"Replay Reset User",
			"replay-reset@rra.test",
			passwordEncoder.encode("OldPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			"reset-token-replay",
			Instant.now().plusSeconds(1800),
			Instant.now(),
			adminRole
		));

		authService.resetPassword(new ResetPasswordRequest("reset-token-replay", "NewPass!456"));

		var exception = assertThrows(
			com.rra.taxhandbook.common.exception.ResourceNotFoundException.class,
			() -> authService.resetPassword(new ResetPasswordRequest("reset-token-replay", "AnotherPass!789"))
		);

		assertEquals("Password reset token is invalid.", exception.getMessage());
	}

	@Test
	void createLocalUserRejectsWeakPassword() {
		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> userService.createUserWithPassword(new AdminSetPasswordUserRequest(
				"EMP-WEAK-001",
				"Local",
				"Weak User",
				"local-weak@rra.test",
				null,
				"ADMIN",
				LanguageCode.EN,
				null,
				null,
				null,
				"weakpass"
			))
		);

		assertEquals("Password must be at least 8 characters and include uppercase, lowercase, number, and special character.", exception.getMessage());
	}

	private void assertAuditLog(String action, String actor, String targetEmail) {
		var matchingLog = auditLogRepository.findAll().stream()
			.filter(log -> action.equals(log.getAction()))
			.filter(log -> actor.equals(log.getActor()))
			.filter(log -> targetEmail.equals(log.getTargetEmail()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected audit log for action " + action + " and target " + targetEmail));

		assertNotNull(matchingLog.getCreatedAt());
	}
}
