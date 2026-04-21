package com.rra.taxhandbook.content.dto;

import java.time.Instant;

public record TopicWorkflowActionRequest(
	String action,
	Instant scheduledAt,
	String comment
) {
	public TopicWorkflowActionRequest(String action) {
		this(action, null, null);
	}

	public TopicWorkflowActionRequest(String action, Instant scheduledAt) {
		this(action, scheduledAt, null);
	}
}
