package com.rra.taxhandbook.content.dto;

public record TopicBlockResponse(
	Long id,
	String title,
	String body,
	String blockType,
	String anchorKey,
	Integer sortOrder
) {
}
