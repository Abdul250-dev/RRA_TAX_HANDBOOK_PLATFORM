package com.rra.taxhandbook.content.dto;

public record SectionSummaryResponse(
	Long id,
	Long parentId,
	String name,
	String slug,
	String summary,
	String type,
	Integer sortOrder
) {
}
