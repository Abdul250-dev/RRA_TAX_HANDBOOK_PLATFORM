package com.rra.taxhandbook.content.dto;

import java.util.List;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.document.dto.DocumentResponse;
import com.rra.taxhandbook.faq.dto.FAQResponse;

public record PublicSearchResponse(
	String query,
	LanguageCode locale,
	List<PublicSearchResultResponse> sections,
	List<PublicSearchResultResponse> topics,
	List<PublicSearchResultResponse> guides,
	List<FAQResponse> faqs,
	List<DocumentResponse> documents
) {
}
