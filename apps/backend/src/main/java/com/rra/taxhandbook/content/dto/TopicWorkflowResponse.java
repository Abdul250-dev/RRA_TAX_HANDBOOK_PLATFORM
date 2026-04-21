package com.rra.taxhandbook.content.dto;

import java.time.Instant;

public record TopicWorkflowResponse(
	Long topicId,
	String title,
	String slug,
	String status,
	String action,
	String performedBy,
	String comment,
	Instant scheduledPublishAt
) {
}
