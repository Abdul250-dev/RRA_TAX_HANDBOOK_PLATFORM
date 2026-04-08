package com.rra.taxhandbook.article.dto;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;

public record ArticleRequest(
	String title,
	String summary,
	String content,
	LanguageCode language,
	ContentStatus status
) {
}
