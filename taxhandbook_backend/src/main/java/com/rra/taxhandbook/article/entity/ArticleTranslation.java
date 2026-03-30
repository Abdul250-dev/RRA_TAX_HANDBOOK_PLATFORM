package com.rra.taxhandbook.article.entity;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record ArticleTranslation(
	Long articleId,
	LanguageCode language,
	String title,
	String summary,
	String content
) {
}
