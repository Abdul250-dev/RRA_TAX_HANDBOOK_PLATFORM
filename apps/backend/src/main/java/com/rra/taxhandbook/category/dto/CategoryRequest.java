package com.rra.taxhandbook.category.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record CategoryRequest(
	String name,
	LanguageCode language
) {
}
