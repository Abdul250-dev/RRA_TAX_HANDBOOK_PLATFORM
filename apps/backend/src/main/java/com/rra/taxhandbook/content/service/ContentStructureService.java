package com.rra.taxhandbook.content.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.AdminSectionResponse;
import com.rra.taxhandbook.content.dto.AdminUpdateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateTopicRequest;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.SectionWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicBlockResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicSummaryResponse;
import com.rra.taxhandbook.content.section.entity.Section;
import com.rra.taxhandbook.content.section.entity.SectionTranslation;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlock;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockTranslation;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;

@Service
public class ContentStructureService {

	private final SectionRepository sectionRepository;
	private final SectionTranslationRepository sectionTranslationRepository;
	private final TopicRepository topicRepository;
	private final TopicTranslationRepository topicTranslationRepository;
	private final TopicBlockRepository topicBlockRepository;
	private final TopicBlockTranslationRepository topicBlockTranslationRepository;

	public ContentStructureService(
		SectionRepository sectionRepository,
		SectionTranslationRepository sectionTranslationRepository,
		TopicRepository topicRepository,
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		TopicBlockTranslationRepository topicBlockTranslationRepository
	) {
		this.sectionRepository = sectionRepository;
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.topicBlockTranslationRepository = topicBlockTranslationRepository;
	}

	public List<SectionSummaryResponse> getSections(LanguageCode locale) {
		return sectionTranslationRepository.findByLocaleAndSection_StatusOrderBySection_SortOrderAsc(locale, ContentStatus.PUBLISHED).stream()
			.map(translation -> new SectionSummaryResponse(
				translation.getSection().getId(),
				translation.getSection().getParent() == null ? null : translation.getSection().getParent().getId(),
				translation.getName(),
				translation.getSlug(),
				translation.getSummary(),
				translation.getSection().getType().name(),
				translation.getSection().getSortOrder()
			))
			.toList();
	}

	public List<AdminSectionResponse> getAdminSections(LanguageCode locale) {
		return sectionTranslationRepository.findByLocaleOrderBySection_SortOrderAsc(locale).stream()
			.map(translation -> new AdminSectionResponse(
				translation.getSection().getId(),
				translation.getSection().getParent() == null ? null : translation.getSection().getParent().getId(),
				translation.getName(),
				translation.getSlug(),
				translation.getSummary(),
				translation.getSection().getType().name(),
				translation.getSection().getSortOrder(),
				translation.getSection().getStatus().name()
			))
			.toList();
	}

	public List<TopicSummaryResponse> getAdminTopics(LanguageCode locale, ContentStatus status) {
		return topicTranslationRepository.findForAdminList(locale, status).stream()
			.map(translation -> new TopicSummaryResponse(
				translation.getTopic().getId(),
				translation.getTopic().getSection().getId(),
				translation.getTitle(),
				translation.getSlug(),
				translation.getSummary(),
				translation.getTopic().getTopicType().name(),
				translation.getTopic().getStatus().name(),
				translation.getTopic().getSortOrder()
			))
			.toList();
	}

	public TopicDetailResponse getTopicBySlug(String slug, LanguageCode locale) {
		TopicTranslation topicTranslation = topicTranslationRepository.findBySlugAndLocaleAndTopic_Status(slug, locale, ContentStatus.PUBLISHED)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found for slug: " + slug));

		List<TopicBlockResponse> blocks = topicBlockTranslationRepository
			.findByTopicBlock_Topic_IdAndLocaleAndTopicBlock_StatusOrderByTopicBlock_SortOrderAsc(
				topicTranslation.getTopic().getId(),
				locale,
				ContentStatus.PUBLISHED
			)
			.stream()
			.map(blockTranslation -> new TopicBlockResponse(
				blockTranslation.getTopicBlock().getId(),
				blockTranslation.getTitle(),
				blockTranslation.getBody(),
				blockTranslation.getTopicBlock().getBlockType().name(),
				blockTranslation.getTopicBlock().getAnchorKey(),
				blockTranslation.getTopicBlock().getSortOrder()
			))
			.toList();

		return new TopicDetailResponse(
			topicTranslation.getTopic().getId(),
			topicTranslation.getTopic().getSection().getId(),
			topicTranslation.getTitle(),
			topicTranslation.getSlug(),
			topicTranslation.getSummary(),
			topicTranslation.getIntroText(),
			topicTranslation.getTopic().getTopicType().name(),
			topicTranslation.getTopic().getStatus().name(),
			blocks
		);
	}

	public TopicDetailResponse getAdminTopic(Long topicId, LanguageCode locale) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		TopicTranslation topicTranslation = topicTranslationRepository.findByTopic_IdAndLocale(topicId, locale)
			.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found for locale: " + locale.name()));

		List<TopicBlockResponse> blocks = topicBlockTranslationRepository
			.findByTopicBlock_Topic_IdAndLocaleOrderByTopicBlock_SortOrderAsc(topicId, locale)
			.stream()
			.map(blockTranslation -> new TopicBlockResponse(
				blockTranslation.getTopicBlock().getId(),
				blockTranslation.getTitle(),
				blockTranslation.getBody(),
				blockTranslation.getTopicBlock().getBlockType().name(),
				blockTranslation.getTopicBlock().getAnchorKey(),
				blockTranslation.getTopicBlock().getSortOrder()
			))
			.toList();

		return new TopicDetailResponse(
			topic.getId(),
			topic.getSection().getId(),
			topicTranslation.getTitle(),
			topicTranslation.getSlug(),
			topicTranslation.getSummary(),
			topicTranslation.getIntroText(),
			topic.getTopicType().name(),
			topic.getStatus().name(),
			blocks
		);
	}

	public ApiResponse<SectionSummaryResponse> createSection(AdminCreateSectionRequest request) {
		validateSectionSlug(request.slug(), request.locale(), null);
		Section parent = request.parentId() == null ? null : sectionRepository.findById(request.parentId())
			.orElseThrow(() -> new ResourceNotFoundException("Parent section not found: " + request.parentId()));
		Instant now = Instant.now();
		Section section = sectionRepository.save(new Section(parent, request.type(), request.sortOrder(), ContentStatus.DRAFT, null, false, now, now));
		SectionTranslation translation = sectionTranslationRepository.save(new SectionTranslation(section, request.locale(), request.name(), request.slug(), request.summary()));

		return new ApiResponse<>("Section created", new SectionSummaryResponse(
			section.getId(),
			parent == null ? null : parent.getId(),
			translation.getName(),
			translation.getSlug(),
			translation.getSummary(),
			section.getType().name(),
			section.getSortOrder()
		));
	}

	public ApiResponse<TopicDetailResponse> createTopic(AdminCreateTopicRequest request) {
		validateTopicSlug(request.slug(), request.locale(), null);
		Section section = sectionRepository.findById(request.sectionId())
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + request.sectionId()));
		Instant now = Instant.now();
		Topic topic = topicRepository.save(new Topic(section, request.topicType(), ContentStatus.DRAFT, request.sortOrder(), false, true, null, now, now));
		TopicTranslation translation = topicTranslationRepository.save(
			new TopicTranslation(topic, request.locale(), request.title(), request.slug(), request.summary(), request.introText())
		);

		return new ApiResponse<>("Topic created", new TopicDetailResponse(
			topic.getId(),
			section.getId(),
			translation.getTitle(),
			translation.getSlug(),
			translation.getSummary(),
			translation.getIntroText(),
			topic.getTopicType().name(),
			topic.getStatus().name(),
			List.of()
		));
	}

	public ApiResponse<TopicBlockResponse> createTopicBlock(Long topicId, AdminCreateTopicBlockRequest request) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		Instant now = Instant.now();
		TopicBlock topicBlock = topicBlockRepository.save(
			new TopicBlock(topic, request.blockType(), request.sortOrder(), ContentStatus.DRAFT, request.anchorKey(), false, now, now)
		);
		TopicBlockTranslation translation = topicBlockTranslationRepository.save(
			new TopicBlockTranslation(topicBlock, request.locale(), request.title(), request.body())
		);

		return new ApiResponse<>("Topic block created", new TopicBlockResponse(
			topicBlock.getId(),
			translation.getTitle(),
			translation.getBody(),
			topicBlock.getBlockType().name(),
			topicBlock.getAnchorKey(),
			topicBlock.getSortOrder()
		));
	}

	public ApiResponse<AdminSectionResponse> updateSection(Long sectionId, AdminUpdateSectionRequest request) {
		Section section = sectionRepository.findById(sectionId)
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
		validateSectionSlug(request.slug(), request.locale(), sectionId);
		Section parent = request.parentId() == null ? null : sectionRepository.findById(request.parentId())
			.orElseThrow(() -> new ResourceNotFoundException("Parent section not found: " + request.parentId()));
		SectionTranslation translation = sectionTranslationRepository.findBySection_IdAndLocale(sectionId, request.locale())
			.orElseThrow(() -> new ResourceNotFoundException("Section translation not found for locale: " + request.locale().name()));
		section.updateStructure(parent, request.type(), request.sortOrder());
		section.touch(Instant.now());
		translation.update(request.name(), request.slug(), request.summary());
		sectionRepository.save(section);
		sectionTranslationRepository.save(translation);
		return new ApiResponse<>("Section updated", new AdminSectionResponse(
			section.getId(),
			parent == null ? null : parent.getId(),
			translation.getName(),
			translation.getSlug(),
			translation.getSummary(),
			section.getType().name(),
			section.getSortOrder(),
			section.getStatus().name()
		));
	}

	public ApiResponse<TopicDetailResponse> updateTopic(Long topicId, AdminUpdateTopicRequest request) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		validateTopicSlug(request.slug(), request.locale(), topicId);
		Section section = sectionRepository.findById(request.sectionId())
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + request.sectionId()));
		TopicTranslation translation = topicTranslationRepository.findByTopic_IdAndLocale(topicId, request.locale())
			.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found for locale: " + request.locale().name()));
		topic.updateStructure(section, request.topicType(), request.sortOrder());
		topic.touch(Instant.now());
		translation.update(request.title(), request.slug(), request.summary(), request.introText());
		topicRepository.save(topic);
		topicTranslationRepository.save(translation);
		return new ApiResponse<>("Topic updated", getAdminTopic(topicId, request.locale()));
	}

	public ApiResponse<TopicBlockResponse> updateTopicBlock(Long blockId, AdminUpdateTopicBlockRequest request) {
		TopicBlock topicBlock = topicBlockRepository.findById(blockId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic block not found: " + blockId));
		TopicBlockTranslation translation = topicBlockTranslationRepository.findByTopicBlock_IdAndLocale(blockId, request.locale())
			.orElseThrow(() -> new ResourceNotFoundException("Topic block translation not found for locale: " + request.locale().name()));
		topicBlock.updateStructure(request.blockType(), request.sortOrder(), request.anchorKey());
		topicBlock.touch(Instant.now());
		translation.update(request.title(), request.body());
		topicBlockRepository.save(topicBlock);
		topicBlockTranslationRepository.save(translation);
		return new ApiResponse<>("Topic block updated", new TopicBlockResponse(
			topicBlock.getId(),
			translation.getTitle(),
			translation.getBody(),
			topicBlock.getBlockType().name(),
			topicBlock.getAnchorKey(),
			topicBlock.getSortOrder()
		));
	}

	public ApiResponse<String> deleteTopic(Long topicId) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		if (!(topic.getStatus() == ContentStatus.DRAFT || topic.getStatus() == ContentStatus.ARCHIVED)) {
			throw new IllegalArgumentException("Only draft or archived topics can be deleted.");
		}
		topicBlockTranslationRepository.deleteByTopicBlock_Topic_Id(topicId);
		topicBlockRepository.deleteByTopic_Id(topicId);
		topicTranslationRepository.deleteByTopic_Id(topicId);
		topicRepository.delete(topic);
		return new ApiResponse<>("Topic deleted", topicId.toString());
	}

	public ApiResponse<String> deleteTopicBlock(Long blockId) {
		TopicBlock topicBlock = topicBlockRepository.findById(blockId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic block not found: " + blockId));
		if (!(topicBlock.getStatus() == ContentStatus.DRAFT || topicBlock.getStatus() == ContentStatus.ARCHIVED)) {
			throw new IllegalArgumentException("Only draft or archived topic blocks can be deleted.");
		}
		topicBlockTranslationRepository.deleteByTopicBlock_Id(blockId);
		topicBlockRepository.delete(topicBlock);
		return new ApiResponse<>("Topic block deleted", blockId.toString());
	}

	public ApiResponse<AdminSectionResponse> transitionSection(Long sectionId, SectionWorkflowActionRequest request) {
		Section section = sectionRepository.findById(sectionId)
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
		String action = request.action() == null ? "" : request.action().trim().toUpperCase();
		switch (action) {
			case "PUBLISH" -> section.changeStatus(ContentStatus.PUBLISHED);
			case "ARCHIVE" -> section.changeStatus(ContentStatus.ARCHIVED);
			default -> throw new IllegalArgumentException("Unsupported section workflow action: " + request.action());
		}
		section.touch(Instant.now());
		Section savedSection = sectionRepository.save(section);
		SectionTranslation translation = sectionTranslationRepository.findBySection_IdAndLocale(savedSection.getId(), LanguageCode.EN)
			.orElseGet(() -> sectionTranslationRepository.findBySection_IdAndLocale(savedSection.getId(), LanguageCode.FR)
				.orElseGet(() -> sectionTranslationRepository.findBySection_IdAndLocale(savedSection.getId(), LanguageCode.KIN)
					.orElseThrow(() -> new ResourceNotFoundException("Section translation not found: " + sectionId))));
		return new ApiResponse<>("Section workflow updated", new AdminSectionResponse(
			savedSection.getId(),
			savedSection.getParent() == null ? null : savedSection.getParent().getId(),
			translation.getName(),
			translation.getSlug(),
			translation.getSummary(),
			savedSection.getType().name(),
			savedSection.getSortOrder(),
			savedSection.getStatus().name()
		));
	}

	private void validateSectionSlug(String slug, LanguageCode locale, Long sectionId) {
		if (slug == null || slug.isBlank()) {
			throw new IllegalArgumentException("Section slug is required.");
		}
		boolean exists = sectionId == null
			? sectionTranslationRepository.existsBySlugAndLocale(slug, locale)
			: sectionTranslationRepository.existsBySlugAndLocaleAndSection_IdNot(slug, locale, sectionId);
		if (exists) {
			throw new IllegalArgumentException("A section already exists for slug '" + slug + "' and locale " + locale.name());
		}
	}

	private void validateTopicSlug(String slug, LanguageCode locale, Long topicId) {
		if (slug == null || slug.isBlank()) {
			throw new IllegalArgumentException("Topic slug is required.");
		}
		boolean exists = topicId == null
			? topicTranslationRepository.existsBySlugAndLocale(slug, locale)
			: topicTranslationRepository.existsBySlugAndLocaleAndTopic_IdNot(slug, locale, topicId);
		if (exists) {
			throw new IllegalArgumentException("A topic already exists for slug '" + slug + "' and locale " + locale.name());
		}
	}
}
