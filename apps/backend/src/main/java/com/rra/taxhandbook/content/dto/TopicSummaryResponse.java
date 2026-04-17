package com.rra.taxhandbook.content.dto;

import java.time.Instant;

public record TopicSummaryResponse(
	Long id,
	Long sectionId,
	String title,
	String slug,
	String summary,
	String topicType,
	String status,
	Integer sortOrder,
	Instant scheduledPublishAt
) {
}
