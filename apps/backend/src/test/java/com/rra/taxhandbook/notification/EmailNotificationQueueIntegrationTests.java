package com.rra.taxhandbook.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import com.rra.taxhandbook.audit.repository.AuditLogRepository;
import com.rra.taxhandbook.common.exception.NotificationDeliveryException;

@SpringBootTest
@ActiveProfiles("test")
class EmailNotificationQueueIntegrationTests {

	@Autowired
	private EmailNotificationQueueService emailNotificationQueueService;

	@Autowired
	private EmailNotificationRepository emailNotificationRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@MockitoBean
	private EmailDeliveryService emailDeliveryService;

	@BeforeEach
	void setUp() {
		auditLogRepository.deleteAll();
		emailNotificationRepository.deleteAll();
		ReflectionTestUtils.setField(emailNotificationQueueService, "queueProcessingEnabled", true);
	}

	@Test
	void failedImmediateInviteDeliveryLeavesQueuedNotification() {
		doThrow(new RuntimeException("SMTP down"))
			.when(emailDeliveryService)
			.sendInviteEmail(anyString(), anyString(), anyString(), anyString(), anyString());

		assertThrows(NotificationDeliveryException.class,
			() -> emailNotificationQueueService.queueInviteEmail(
				"invitee@rra.test",
				"Invitee User",
				"inviteeuser001",
				"invite-token",
				Instant.now().plusSeconds(3600),
				"admin@rra.test",
				"USER_INVITED"
			));

		EmailNotification savedNotification = emailNotificationRepository.findAll().stream()
			.findFirst()
			.orElseThrow();
		assertEquals(EmailNotificationType.INVITE, savedNotification.getType());
		assertEquals(EmailNotificationStatus.PENDING, savedNotification.getStatus());
		assertEquals(1, savedNotification.getAttempts());
		assertEquals("inviteeuser001", savedNotification.getRecipientUsername());
		assertNotNull(savedNotification.getLastError());
	}

	@Test
	void queuedPasswordResetNotificationCanBeDeliveredLaterByProcessor() {
		doThrow(new RuntimeException("SMTP down"))
			.when(emailDeliveryService)
			.sendPasswordResetEmail(anyString(), anyString(), anyString(), anyString());

		assertThrows(NotificationDeliveryException.class,
			() -> emailNotificationQueueService.queuePasswordResetEmail(
				"reset@rra.test",
				"Reset User",
				"reset-token",
				Instant.now().plusSeconds(3600),
				"reset@rra.test"
			));

		reset(emailDeliveryService);
		doNothing()
			.when(emailDeliveryService)
			.sendPasswordResetEmail(anyString(), anyString(), anyString(), anyString());

		EmailNotification queuedNotification = emailNotificationRepository.findAll().stream()
			.findFirst()
			.orElseThrow();
		ReflectionTestUtils.setField(queuedNotification, "nextAttemptAt", Instant.now().minusSeconds(5));
		emailNotificationRepository.save(queuedNotification);

		int processedCount = emailNotificationQueueService.processDueNotifications();

		assertEquals(1, processedCount);
		EmailNotification savedNotification = emailNotificationRepository.findAll().stream()
			.findFirst()
			.orElseThrow();
		assertEquals(EmailNotificationStatus.SENT, savedNotification.getStatus());
		assertNotNull(savedNotification.getSentAt());
		assertEquals(2, savedNotification.getAttempts());
	}
}
