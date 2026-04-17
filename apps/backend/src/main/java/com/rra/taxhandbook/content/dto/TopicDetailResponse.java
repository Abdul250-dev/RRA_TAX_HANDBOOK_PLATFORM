package com.rra.taxhandbook.content.dto;

import java.time.Instant;
import java.util.List;

public record TopicDetailResponse(
	Long id,
	Long sectionId,
	String title,
	String slug,
	String summary,
	String introText,
	String topicType,
	String status,
	Instant scheduledPublishAt,
	List<TopicBlockResponse> blocks
) {
}
