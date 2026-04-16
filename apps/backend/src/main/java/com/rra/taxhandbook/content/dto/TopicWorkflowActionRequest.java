package com.rra.taxhandbook.content.dto;

import java.time.Instant;

public record TopicWorkflowActionRequest(
	String action,
	Instant scheduledAt
) {
	public TopicWorkflowActionRequest(String action) {
		this(action, null);
	}
}
