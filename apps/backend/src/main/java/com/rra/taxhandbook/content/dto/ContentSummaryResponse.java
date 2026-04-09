package com.rra.taxhandbook.content.dto;

public record ContentSummaryResponse(
	long totalTopics,
	long draftTopics,
	long reviewTopics,
	long approvedTopics,
	long publishedTopics,
	long archivedTopics,
	long totalSections,
	long draftSections,
	long publishedSections,
	long archivedSections
) {
}
