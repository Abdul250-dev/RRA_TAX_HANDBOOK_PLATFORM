package com.rra.taxhandbook.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.rra.taxhandbook.audit.repository.AuditLogRepository;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.exception.NotificationDeliveryException;
import com.rra.taxhandbook.notification.EmailDeliveryService;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;
import com.rra.taxhandbook.user.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserLifecycleAliasIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

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
	void deactivateAliasSuspendsActiveUser() throws Exception {
		User activeUser = userRepository.save(buildUser(
			"LOCAL-ACTIVE-ALIAS",
			"Active Alias User",
			"active-alias@rra.test",
			"ACTIVE",
			passwordEncoder.encode("StrongPass!123"),
			null,
			null
		));

		mockMvc.perform(post("/api/users/" + activeUser.getId() + "/deactivate")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk());

		User savedUser = userRepository.findById(activeUser.getId()).orElseThrow();
		assertEquals("SUSPENDED", savedUser.getStatus());
		assertAuditLog("USER_SUSPENDED", "admin@rra.test", "active-alias@rra.test");
	}

	@Test
	void removeAliasRemovesUserAccess() throws Exception {
		User activeUser = userRepository.save(buildUser(
			"LOCAL-REMOVE-ALIAS",
			"Remove Alias User",
			"remove-alias@rra.test",
			"ACTIVE",
			passwordEncoder.encode("StrongPass!123"),
			null,
			null
		));

		mockMvc.perform(post("/api/users/" + activeUser.getId() + "/remove")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk());

		User savedUser = userRepository.findById(activeUser.getId()).orElseThrow();
		assertEquals("DEACTIVATED", savedUser.getStatus());
		assertNull(savedUser.getPasswordHash());
		assertNull(savedUser.getInviteToken());
		assertAuditLog("USER_REMOVED", "admin@rra.test", "remove-alias@rra.test");
	}

	@Test
	void cancelAliasCancelsPendingInvite() throws Exception {
		User invitedUser = userRepository.save(buildUser(
			"LOCAL-CANCEL-ALIAS",
			"Cancel Alias User",
			"cancel-alias@rra.test",
			"PENDING",
			null,
			"invite-token-cancel",
			Instant.now().plusSeconds(3600)
		));

		mockMvc.perform(post("/api/users/" + invitedUser.getId() + "/cancel")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk());

		User savedUser = userRepository.findById(invitedUser.getId()).orElseThrow();
		assertEquals("DEACTIVATED", savedUser.getStatus());
		assertNull(savedUser.getInviteToken());
		assertNull(savedUser.getInviteExpiresAt());
		assertAuditLog("INVITE_CANCELLED", "admin@rra.test", "cancel-alias@rra.test");
	}

	@Test
	void resendAliasReissuesInviteToken() throws Exception {
		User invitedUser = userRepository.save(buildUser(
			"LOCAL-RESEND-ALIAS",
			"Resend Alias User",
			"resend-alias@rra.test",
			"PENDING",
			null,
			"invite-token-original",
			Instant.now().plusSeconds(3600)
		));

		mockMvc.perform(post("/api/users/" + invitedUser.getId() + "/resend")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk());

		User savedUser = userRepository.findById(invitedUser.getId()).orElseThrow();
		assertEquals("PENDING", savedUser.getStatus());
		assertNotEquals("invite-token-original", savedUser.getInviteToken());
		verify(emailDeliveryService).sendInviteEmail(anyString(), anyString(), nullable(String.class), anyString(), anyString());
		assertAuditLog("INVITE_RESENT", "admin@rra.test", "resend-alias@rra.test");
	}

	@Test
	void resendAliasFailsClearlyWhenInviteEmailDeliveryFails() {
		User invitedUser = userRepository.save(buildUser(
			"LOCAL-RESEND-FAIL",
			"Resend Failure User",
			"resend-failure@rra.test",
			"PENDING",
			null,
			"invite-token-original",
			Instant.now().plusSeconds(3600)
		));
		doThrow(new RuntimeException("SMTP unavailable"))
			.when(emailDeliveryService)
			.sendInviteEmail(anyString(), anyString(), nullable(String.class), anyString(), anyString());

		var exception = org.junit.jupiter.api.Assertions.assertThrows(
			NotificationDeliveryException.class,
			() -> userService.resendInvite(invitedUser.getId(), "admin@rra.test")
		);

		assertEquals(
			"Invite was created but the email could not be sent. The user remains in a pending state and invite delivery should be retried.",
			exception.getMessage()
		);
		assertAuditLog("INVITE_RESENT_EMAIL_FAILED", "admin@rra.test", "resend-failure@rra.test");
	}

	private void assertAuditLog(String action, String actor, String targetEmail) {
		var matchingLog = auditLogRepository.findAll().stream()
			.filter(log -> action.equals(log.getAction()))
			.filter(log -> actor.equals(log.getActor()))
			.filter(log -> targetEmail.equals(log.getTargetEmail()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected audit log for action " + action + " and target " + targetEmail));

		org.junit.jupiter.api.Assertions.assertNotNull(matchingLog.getCreatedAt());
	}

	private User buildUser(
		String userCode,
		String fullName,
		String email,
		String status,
		String passwordHash,
		String inviteToken,
		Instant inviteExpiresAt
	) {
		return new User(
			userCode,
			fullName,
			email,
			passwordHash,
			LanguageCode.EN,
			UserSource.LOCAL,
			status,
			inviteToken,
			inviteExpiresAt,
			null,
			null,
			Instant.now(),
			adminRole
		);
	}
}
