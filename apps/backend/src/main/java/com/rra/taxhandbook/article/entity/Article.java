package com.rra.taxhandbook.article.entity;

import com.rra.taxhandbook.common.enums.ContentStatus;

public record Article(
	Long id,
	String slug,
	ContentStatus status
) {
}
