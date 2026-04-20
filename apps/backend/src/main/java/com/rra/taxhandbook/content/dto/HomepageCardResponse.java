package com.rra.taxhandbook.content.dto;

public record HomepageCardResponse(
	Long sectionId,
	String title,
	String slug,
	String description,
	Integer sortOrder
) {}
