package com.rra.taxhandbook.notification;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {

	List<EmailNotification> findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAscCreatedAtAsc(
		List<EmailNotificationStatus> statuses,
		Instant nextAttemptAt
	);
}
