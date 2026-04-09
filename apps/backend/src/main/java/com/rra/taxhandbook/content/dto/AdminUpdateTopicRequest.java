package com.rra.taxhandbook.content.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.topic.entity.TopicType;

public record AdminUpdateTopicRequest(
	Long sectionId,
	TopicType topicType,
	Integer sortOrder,
	LanguageCode locale,
	String title,
	String slug,
	String summary,
	String introText
) {
}
