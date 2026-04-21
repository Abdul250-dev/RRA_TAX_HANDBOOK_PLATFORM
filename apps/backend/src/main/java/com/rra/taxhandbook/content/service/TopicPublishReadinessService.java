package com.rra.taxhandbook.content.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.TopicPublishReadinessResponse;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;

@Service
public class TopicPublishReadinessService {

	private final TopicTranslationRepository topicTranslationRepository;
	private final TopicBlockRepository topicBlockRepository;
	private final TopicBlockTranslationRepository topicBlockTranslationRepository;
	private final SectionTranslationRepository sectionTranslationRepository;
	private final Set<LanguageCode> requiredPublishLocales;

	public TopicPublishReadinessService(
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		TopicBlockTranslationRepository topicBlockTranslationRepository,
		SectionTranslationRepository sectionTranslationRepository,
		@Value("${app.content.required-publish-locales:EN}") String requiredPublishLocales
	) {
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.topicBlockTranslationRepository = topicBlockTranslationRepository;
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.requiredPublishLocales = parseRequiredPublishLocales(requiredPublishLocales);
	}

	public TopicPublishReadinessResponse getPublishReadiness(Topic topic) {
		List<String> issues = collectPublishReadinessIssues(topic);
		return new TopicPublishReadinessResponse(
			topic.getId(),
			topic.getStatus().name(),
			issues.isEmpty(),
			requiredPublishLocales.stream().map(LanguageCode::name).sorted().toList(),
			issues
		);
	}

	public List<String> collectPublishReadinessIssues(Topic topic) {
		List<String> issues = new ArrayList<>();
		var blocks = topicBlockRepository.findByTopic_IdOrderBySortOrderAsc(topic.getId());
		if (blocks.isEmpty()) {
			issues.add("Topic must contain at least one content block.");
		}
		long availableTopicLocales = topicTranslationRepository.countByTopic_IdAndLocaleIn(topic.getId(), requiredPublishLocales);
		if (availableTopicLocales != requiredPublishLocales.size()) {
			issues.add("Topic is missing one or more required publish locales: " + requiredPublishLocales + ".");
		}
		long availableSectionLocales = sectionTranslationRepository.countBySection_IdAndLocaleIn(topic.getSection().getId(), requiredPublishLocales);
		if (availableSectionLocales != requiredPublishLocales.size()) {
			issues.add("Section is missing one or more required publish locales: " + requiredPublishLocales + ".");
		}
		for (var block : blocks) {
			long availableBlockLocales = topicBlockTranslationRepository.countByTopicBlock_IdAndLocaleIn(block.getId(), requiredPublishLocales);
			if (availableBlockLocales != requiredPublishLocales.size()) {
				issues.add("Topic block " + block.getId() + " is missing one or more required publish locales: " + requiredPublishLocales + ".");
			}
		}
		return issues;
	}

	private Set<LanguageCode> parseRequiredPublishLocales(String configuredLocales) {
		if (configuredLocales == null || configuredLocales.isBlank()) {
			return EnumSet.of(LanguageCode.EN);
		}
		EnumSet<LanguageCode> locales = EnumSet.noneOf(LanguageCode.class);
		Arrays.stream(configuredLocales.split(","))
			.map(String::trim)
			.filter(value -> !value.isEmpty())
			.map(value -> LanguageCode.valueOf(value.toUpperCase(Locale.ROOT)))
			.forEach(locales::add);
		return locales.isEmpty() ? EnumSet.of(LanguageCode.EN) : locales;
	}
}
