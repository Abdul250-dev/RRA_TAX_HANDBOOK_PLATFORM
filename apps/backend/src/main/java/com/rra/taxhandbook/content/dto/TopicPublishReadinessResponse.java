package com.rra.taxhandbook.content.dto;

import java.util.List;

public record TopicPublishReadinessResponse(
	Long topicId,
	String status,
	boolean ready,
	List<String> requiredLocales,
	List<String> issues
) {
}
