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
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.TopicBlockResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
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

	public ApiResponse<SectionSummaryResponse> createSection(AdminCreateSectionRequest request) {
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
}
