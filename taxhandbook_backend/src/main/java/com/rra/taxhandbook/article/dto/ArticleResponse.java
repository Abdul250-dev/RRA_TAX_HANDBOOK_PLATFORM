package com.rra.taxhandbook.article.dto;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;

public record ArticleResponse(
	Long id,
	String title,
	String slug,
	LanguageCode language,
	ContentStatus status
) {
}
