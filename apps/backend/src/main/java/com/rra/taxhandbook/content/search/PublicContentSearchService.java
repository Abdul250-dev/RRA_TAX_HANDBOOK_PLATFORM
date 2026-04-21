package com.rra.taxhandbook.content.search;

import java.util.List;
import java.util.Locale;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.PublicSearchResponse;
import com.rra.taxhandbook.content.dto.PublicSearchResultResponse;
import com.rra.taxhandbook.content.section.entity.SectionTranslation;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;
import com.rra.taxhandbook.content.topic.entity.TopicType;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.document.dto.DocumentResponse;
import com.rra.taxhandbook.document.service.DocumentService;
import com.rra.taxhandbook.faq.dto.FAQResponse;
import com.rra.taxhandbook.faq.service.FAQService;

@Service
public class PublicContentSearchService {

	private static final int RESULT_LIMIT = 10;

	private final SectionTranslationRepository sectionTranslationRepository;
	private final TopicTranslationRepository topicTranslationRepository;
	private final FAQService faqService;
	private final DocumentService documentService;

	public PublicContentSearchService(
		SectionTranslationRepository sectionTranslationRepository,
		TopicTranslationRepository topicTranslationRepository,
		FAQService faqService,
		DocumentService documentService
	) {
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.faqService = faqService;
		this.documentService = documentService;
	}

	public PublicSearchResponse search(String query, LanguageCode locale) {
		String normalizedQuery = normalizeQuery(query);
		List<PublicSearchResultResponse> sections = sectionTranslationRepository
			.findByLocaleAndSection_StatusAndNameContainingIgnoreCaseOrderBySection_SortOrderAsc(
				locale,
				ContentStatus.PUBLISHED,
				normalizedQuery
			)
			.stream()
			.limit(RESULT_LIMIT)
			.map(this::toSectionResult)
			.toList();

		List<TopicTranslation> matchingTopics = distinctByTopicId(topicTranslationRepository
			.searchPublishedByLocale(locale, ContentStatus.PUBLISHED, normalizedQuery));

		List<PublicSearchResultResponse> topics = matchingTopics.stream()
			.filter(translation -> translation.getTopic().getTopicType() != TopicType.GUIDE)
			.limit(RESULT_LIMIT)
			.map(this::toTopicResult)
			.toList();

		List<PublicSearchResultResponse> guides = matchingTopics.stream()
			.filter(translation -> translation.getTopic().getTopicType() == TopicType.GUIDE)
			.limit(RESULT_LIMIT)
			.map(this::toTopicResult)
			.toList();

		String comparableQuery = normalizedQuery.toLowerCase(Locale.ROOT);
		List<FAQResponse> faqs = faqService.getFAQs().stream()
			.filter(faq -> faq.language() == locale)
			.filter(faq -> contains(faq.question(), comparableQuery) || contains(faq.answer(), comparableQuery))
			.limit(RESULT_LIMIT)
			.toList();
		List<DocumentResponse> documents = documentService.getDocuments().stream()
			.filter(document -> contains(document.title(), comparableQuery) || contains(document.fileName(), comparableQuery))
			.limit(RESULT_LIMIT)
			.toList();

		return new PublicSearchResponse(normalizedQuery, locale, sections, topics, guides, faqs, documents);
	}

	private String normalizeQuery(String query) {
		if (query == null || query.isBlank()) {
			throw new IllegalArgumentException("Search query is required.");
		}
		String normalized = query.trim();
		if (normalized.length() < 2) {
			throw new IllegalArgumentException("Search query must contain at least 2 characters.");
		}
		return normalized;
	}

	private PublicSearchResultResponse toSectionResult(SectionTranslation translation) {
		return new PublicSearchResultResponse(
			translation.getSection().getId(),
			translation.getName(),
			translation.getSlug(),
			translation.getSummary(),
			"SECTION",
			translation.getSection().getId(),
			"/sections/" + translation.getSlug()
		);
	}

	private PublicSearchResultResponse toTopicResult(TopicTranslation translation) {
		String pathPrefix = translation.getTopic().getTopicType() == TopicType.GUIDE ? "/guides/" : "/topics/";
		return new PublicSearchResultResponse(
			translation.getTopic().getId(),
			translation.getTitle(),
			translation.getSlug(),
			translation.getSummary(),
			translation.getTopic().getTopicType().name(),
			translation.getTopic().getSection().getId(),
			pathPrefix + translation.getSlug()
		);
	}

	private List<TopicTranslation> distinctByTopicId(List<TopicTranslation> translations) {
		return translations.stream()
			.collect(java.util.stream.Collectors.toMap(
				translation -> translation.getTopic().getId(),
				translation -> translation,
				(first, ignored) -> first
			))
			.values()
			.stream()
			.sorted(Comparator.comparing(translation -> translation.getTopic().getSortOrder()))
			.toList();
	}

	private boolean contains(String value, String comparableQuery) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(comparableQuery);
	}
}
