package com.rra.taxhandbook.article.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record ArticleSearchRequest(
	String keyword,
	LanguageCode language
) {
}
