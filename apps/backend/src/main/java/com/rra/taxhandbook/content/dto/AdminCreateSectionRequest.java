package com.rra.taxhandbook.content.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.section.entity.SectionType;

public record AdminCreateSectionRequest(
	Long parentId,
	SectionType type,
	Integer sortOrder,
	LanguageCode locale,
	String name,
	String slug,
	String summary
) {
}
