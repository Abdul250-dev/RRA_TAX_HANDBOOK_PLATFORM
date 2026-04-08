package com.rra.taxhandbook.category.entity;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record CategoryTranslation(
	Long categoryId,
	LanguageCode language,
	String name
) {
}
