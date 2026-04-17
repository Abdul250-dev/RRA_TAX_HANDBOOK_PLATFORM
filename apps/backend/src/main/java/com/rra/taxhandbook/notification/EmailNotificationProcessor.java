package com.rra.taxhandbook.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProcessor {

	private final EmailNotificationQueueService emailNotificationQueueService;

	public EmailNotificationProcessor(EmailNotificationQueueService emailNotificationQueueService) {
		this.emailNotificationQueueService = emailNotificationQueueService;
	}

	@Scheduled(fixedDelayString = "${app.mail.queue.processing.fixed-delay-ms:60000}")
	public void processDueNotifications() {
		emailNotificationQueueService.processDueNotifications();
	}
}
