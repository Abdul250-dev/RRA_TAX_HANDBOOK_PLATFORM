package com.rra.taxhandbook.content.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockType;

public record AdminUpdateTopicBlockRequest(
	TopicBlockType blockType,
	Integer sortOrder,
	String anchorKey,
	LanguageCode locale,
	String title,
	String body
) {
}
