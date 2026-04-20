package com.rra.taxhandbook.content.dto;

import java.util.List;

public record PublicSectionDetailResponse(
	Long id,
	Long parentId,
	String name,
	String slug,
	String summary,
	String type,
	Integer sortOrder,
	List<SectionSummaryResponse> children,
	List<TopicSummaryResponse> topics
) {
}
