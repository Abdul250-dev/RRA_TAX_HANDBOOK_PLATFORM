package com.rra.taxhandbook.content.dto;

import java.time.Instant;
import java.util.List;

import com.rra.taxhandbook.document.dto.DocumentResponse;
import com.rra.taxhandbook.faq.dto.FAQResponse;

public record TopicDetailResponse(
	Long id,
	Long sectionId,
	String title,
	String slug,
	String summary,
	String introText,
	String topicType,
	String status,
	Instant scheduledPublishAt,
	Instant lastUpdated,
	List<TopicBlockResponse> blocks,
	List<FAQResponse> relatedFaqs,
	List<DocumentResponse> relatedDocuments,
	List<TopicSummaryResponse> relatedGuides,
	List<TopicWorkflowHistoryResponse> workflowHistory,
	TopicPublishReadinessResponse publishReadiness
) {
}
