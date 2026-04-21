package com.rra.taxhandbook.content.dto;

public record PublicSearchResultResponse(
	Long id,
	String title,
	String slug,
	String summary,
	String type,
	Long sectionId,
	String url
) {
}
