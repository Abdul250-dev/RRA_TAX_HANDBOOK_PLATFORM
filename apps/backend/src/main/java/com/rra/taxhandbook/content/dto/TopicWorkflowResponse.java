package com.rra.taxhandbook.content.dto;

public record TopicWorkflowResponse(
	Long topicId,
	String title,
	String slug,
	String status,
	String action,
	String performedBy
) {
}
