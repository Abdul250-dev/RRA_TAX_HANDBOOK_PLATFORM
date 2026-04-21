package com.rra.taxhandbook.content.dto;

public record AdminHomepageCardRequest(
	Long sectionId,
	String sectionSlug,
	Integer sortOrder,
	String title,
	String description
) {}
