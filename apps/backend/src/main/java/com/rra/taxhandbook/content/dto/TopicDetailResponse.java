package com.rra.taxhandbook.content.dto;

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
	List<TopicBlockResponse> blocks
) {
}
