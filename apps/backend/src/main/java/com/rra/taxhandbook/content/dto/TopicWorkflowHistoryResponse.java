package com.rra.taxhandbook.content.dto;

import java.time.Instant;

public record TopicWorkflowHistoryResponse(
	Long id,
	Long topicId,
	String action,
	String fromStatus,
	String toStatus,
	String comment,
	String performedBy,
	Instant createdAt
) {
}
