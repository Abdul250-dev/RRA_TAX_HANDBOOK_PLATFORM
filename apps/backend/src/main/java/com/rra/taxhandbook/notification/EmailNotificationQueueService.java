package com.rra.taxhandbook.notification;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.common.exception.NotificationDeliveryException;

@Service
public class EmailNotificationQueueService {

	private static final Logger log = LoggerFactory.getLogger(EmailNotificationQueueService.class);

	private final EmailNotificationRepository emailNotificationRepository;
	private final EmailDeliveryService emailDeliveryService;
	private final AuditLogService auditLogService;

	@Value("${app.mail.queue.max-attempts:10}")
	private int queueMaxAttempts = 10;

	@Value("${app.mail.queue.retry-delay-seconds:300}")
	private long queueRetryDelaySeconds = 300L;

	@Value("${app.mail.queue.processing.enabled:true}")
	private boolean queueProcessingEnabled;

	public EmailNotificationQueueService(
		EmailNotificationRepository emailNotificationRepository,
		EmailDeliveryService emailDeliveryService,
		AuditLogService auditLogService
	) {
		this.emailNotificationRepository = emailNotificationRepository;
		this.emailDeliveryService = emailDeliveryService;
		this.auditLogService = auditLogService;
	}

	@Transactional(noRollbackFor = NotificationDeliveryException.class)
	public void queueInviteEmail(
		String recipientEmail,
		String recipientName,
		String recipientUsername,
		String inviteToken,
		Instant expiresAt,
		String actor,
		String actionPrefix
	) {
		EmailNotification notification = createNotification(
			EmailNotificationType.INVITE,
			recipientEmail,
			recipientName,
			recipientUsername,
			inviteToken,
			expiresAt
		);
		attemptImmediateDelivery(notification, actor, actionPrefix);
	}

	@Transactional(noRollbackFor = NotificationDeliveryException.class)
	public void queuePasswordResetEmail(String recipientEmail, String recipientName, String resetToken, Instant expiresAt, String actor) {
		EmailNotification notification = createNotification(
			EmailNotificationType.PASSWORD_RESET,
			recipientEmail,
			recipientName,
			null,
			resetToken,
			expiresAt
		);
		attemptImmediateDelivery(notification, actor, "PASSWORD_RESET");
	}

	@Transactional
	public int processDueNotifications() {
		if (!queueProcessingEnabled) {
			return 0;
		}
		List<EmailNotification> dueNotifications = emailNotificationRepository
			.findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAscCreatedAtAsc(
				List.of(EmailNotificationStatus.PENDING),
				Instant.now()
			);
		for (EmailNotification notification : dueNotifications) {
			processNotification(notification, true);
		}
		return dueNotifications.size();
	}

	private EmailNotification createNotification(
		EmailNotificationType type,
		String recipientEmail,
		String recipientName,
		String recipientUsername,
		String token,
		Instant expiresAt
	) {
		return emailNotificationRepository.save(new EmailNotification(
			type,
			recipientEmail,
			recipientName,
			recipientUsername,
			token,
			expiresAt,
			Math.max(1, queueMaxAttempts),
			Instant.now()
		));
	}

	private void attemptImmediateDelivery(EmailNotification notification, String actor, String actionPrefix) {
		try {
			processNotification(notification, false);
			auditLogService.log(actionPrefix + "_EMAIL_SENT", actor, notification.getRecipientEmail(), "Email dispatched");
		}
		catch (RuntimeException ex) {
			auditLogService.log(actionPrefix + "_EMAIL_FAILED", actor, notification.getRecipientEmail(), "Email delivery failed and notification was queued for retry");
			throw new NotificationDeliveryException(
				notification.getType() == EmailNotificationType.PASSWORD_RESET
					? "Password reset token was created but the reset email could not be sent. Retry the request or review mail delivery configuration."
					: "Invite was created but the email could not be sent. The user remains in a pending state and invite delivery should be retried.",
				ex
			);
		}
	}

	private void processNotification(EmailNotification notification, boolean retryRun) {
		notification.markSendingAttempt();
		emailNotificationRepository.save(notification);
		try {
			switch (notification.getType()) {
				case INVITE -> emailDeliveryService.sendInviteEmail(
					notification.getRecipientEmail(),
					notification.getRecipientName(),
					notification.getRecipientUsername(),
					notification.getToken(),
					notification.getTokenExpiresAt().toString()
				);
				case PASSWORD_RESET -> emailDeliveryService.sendPasswordResetEmail(
					notification.getRecipientEmail(),
					notification.getRecipientName(),
					notification.getToken(),
					notification.getTokenExpiresAt().toString()
				);
			}
			notification.markSent();
			emailNotificationRepository.save(notification);
			if (retryRun) {
				auditLogService.log(
					notification.getType() == EmailNotificationType.PASSWORD_RESET ? "PASSWORD_RESET_EMAIL_RETRY_SENT" : "INVITE_EMAIL_RETRY_SENT",
					"system",
					notification.getRecipientEmail(),
					"Queued email dispatched successfully on retry"
				);
			}
		}
		catch (RuntimeException ex) {
			notification.scheduleRetry(ex.getMessage(), Instant.now().plusSeconds(Math.max(1L, queueRetryDelaySeconds)));
			emailNotificationRepository.save(notification);
			if (retryRun) {
				log.warn("Queued {} email delivery failed for {} on attempt {} of {}", notification.getType(), notification.getRecipientEmail(), notification.getAttempts(), notification.getMaxAttempts(), ex);
				if (notification.getStatus() == EmailNotificationStatus.FAILED) {
					auditLogService.log(
						notification.getType() == EmailNotificationType.PASSWORD_RESET ? "PASSWORD_RESET_EMAIL_RETRY_FAILED" : "INVITE_EMAIL_RETRY_FAILED",
						"system",
						notification.getRecipientEmail(),
						"Queued email reached max retry attempts"
					);
				}
			}
			throw ex;
		}
	}
}
