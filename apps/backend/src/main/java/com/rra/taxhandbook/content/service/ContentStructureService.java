package com.rra.taxhandbook.content.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rra.taxhandbook.audit.service.AuditLogService;
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
import com.rra.taxhandbook.content.dto.ContentSummaryResponse;
import com.rra.taxhandbook.content.dto.HomepageCardResponse;
import com.rra.taxhandbook.content.dto.HomepageResponse;
import com.rra.taxhandbook.content.dto.PublicSectionDetailResponse;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.SectionWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicBlockResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicSummaryResponse;
import com.rra.taxhandbook.content.homepage.entity.HomepageContent;
import com.rra.taxhandbook.content.homepage.entity.HomepageContentTranslation;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardTranslationRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentTranslationRepository;
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
	private final HomepageContentRepository homepageContentRepository;
	private final HomepageContentTranslationRepository homepageContentTranslationRepository;
	private final HomepageCardTranslationRepository homepageCardTranslationRepository;
	private final AuditLogService auditLogService;

	public ContentStructureService(
		SectionRepository sectionRepository,
		SectionTranslationRepository sectionTranslationRepository,
		TopicRepository topicRepository,
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		TopicBlockTranslationRepository topicBlockTranslationRepository,
		HomepageContentRepository homepageContentRepository,
		HomepageContentTranslationRepository homepageContentTranslationRepository,
		HomepageCardTranslationRepository homepageCardTranslationRepository,
		AuditLogService auditLogService
	) {
		this.sectionRepository = sectionRepository;
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.topicBlockTranslationRepository = topicBlockTranslationRepository;
		this.homepageContentRepository = homepageContentRepository;
		this.homepageContentTranslationRepository = homepageContentTranslationRepository;
		this.homepageCardTranslationRepository = homepageCardTranslationRepository;
		this.auditLogService = auditLogService;
	}

	public HomepageResponse getHomepage(LanguageCode locale) {
		HomepageContent homepageContent = homepageContentRepository.findFirstByStatusOrderByUpdatedAtDesc(ContentStatus.PUBLISHED)
			.orElseThrow(() -> new ResourceNotFoundException("Published homepage content not found."));
		HomepageContentTranslation translation = homepageContentTranslationRepository
			.findByHomepageContent_IdAndLocale(homepageContent.getId(), locale)
			.orElseGet(() -> homepageContentTranslationRepository
				.findByHomepageContent_IdAndLocale(homepageContent.getId(), LanguageCode.EN)
				.orElseThrow(() -> new ResourceNotFoundException("Homepage translation not found for locale: " + locale.name())));

		List<HomepageCardResponse> cards = homepageCardTranslationRepository
			.findByHomepageCard_HomepageContent_IdAndLocaleOrderByHomepageCard_SortOrderAsc(homepageContent.getId(), locale)
			.stream()
			.map(cardTranslation -> new HomepageCardResponse(
				cardTranslation.getHomepageCard().getSection().getId(),
				cardTranslation.getTitle(),
				getSectionSlug(cardTranslation.getHomepageCard().getSection().getId(), locale),
				cardTranslation.getDescription(),
				cardTranslation.getHomepageCard().getSortOrder()
			))
			.toList();

		return new HomepageResponse(
			translation.getKicker(),
			translation.getTitle(),
			translation.getSubtitle(),
			translation.getSearchLabel(),
			translation.getHelpLabel(),
			homepageContent.getUpdatedAt(),
			cards
		);
	}

	public List<SectionSummaryResponse> getSections(LanguageCode locale) {
		return sectionTranslationRepository.findByLocaleAndSection_StatusOrderBySection_SortOrderAsc(locale, ContentStatus.PUBLISHED).stream()
			.map(this::toSectionSummary)
			.toList();
	}

	public PublicSectionDetailResponse getSectionBySlug(String slug, LanguageCode locale) {
		SectionTranslation sectionTranslation = sectionTranslationRepository
			.findBySlugAndLocaleAndSection_Status(slug, locale, ContentStatus.PUBLISHED)
			.orElseThrow(() -> new ResourceNotFoundException("Section not found for slug: " + slug));

		List<SectionSummaryResponse> children = sectionTranslationRepository
			.findBySection_Parent_IdAndLocaleAndSection_StatusOrderBySection_SortOrderAsc(
				sectionTranslation.getSection().getId(),
				locale,
				ContentStatus.PUBLISHED
			)
			.stream()
			.map(this::toSectionSummary)
			.toList();

		List<TopicSummaryResponse> topics = topicTranslationRepository
			.findByTopic_Section_IdAndLocaleAndTopic_StatusOrderByTopic_SortOrderAsc(
				sectionTranslation.getSection().getId(),
				locale,
				ContentStatus.PUBLISHED
			)
			.stream()
			.map(translation -> new TopicSummaryResponse(
				translation.getTopic().getId(),
				translation.getTopic().getSection().getId(),
				translation.getTitle(),
				translation.getSlug(),
				translation.getSummary(),
				translation.getTopic().getTopicType().name(),
				translation.getTopic().getStatus().name(),
				translation.getTopic().getSortOrder(),
				translation.getTopic().getScheduledPublishAt()
			))
			.toList();

		return new PublicSectionDetailResponse(
			sectionTranslation.getSection().getId(),
			sectionTranslation.getSection().getParent() == null ? null : sectionTranslation.getSection().getParent().getId(),
			sectionTranslation.getName(),
			sectionTranslation.getSlug(),
			sectionTranslation.getSummary(),
			sectionTranslation.getSection().getType().name(),
			sectionTranslation.getSection().getSortOrder(),
			children,
			topics
		);
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

	public ContentSummaryResponse getContentSummary() {
		return new ContentSummaryResponse(
			topicRepository.count(),
			topicRepository.countByStatus(ContentStatus.DRAFT),
			topicRepository.countByStatus(ContentStatus.REVIEW),
			topicRepository.countByStatus(ContentStatus.APPROVED),
			topicRepository.countByStatus(ContentStatus.PUBLISHED),
			topicRepository.countByStatus(ContentStatus.ARCHIVED),
			sectionRepository.count(),
			sectionRepository.countByStatus(ContentStatus.DRAFT),
			sectionRepository.countByStatus(ContentStatus.PUBLISHED),
			sectionRepository.countByStatus(ContentStatus.ARCHIVED)
		);
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
				translation.getTopic().getSortOrder(),
				translation.getTopic().getScheduledPublishAt()
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
			topicTranslation.getTopic().getScheduledPublishAt(),
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
			topic.getScheduledPublishAt(),
			blocks
		);
	}

	public ApiResponse<SectionSummaryResponse> createSection(AdminCreateSectionRequest request, String actor) {
		validateSectionSlug(request.slug(), request.locale(), null);
		Section parent = request.parentId() == null ? null : sectionRepository.findById(request.parentId())
			.orElseThrow(() -> new ResourceNotFoundException("Parent section not found: " + request.parentId()));
		Instant now = Instant.now();
		Section section = sectionRepository.save(new Section(parent, request.type(), request.sortOrder(), ContentStatus.DRAFT, null, false, now, now));
		SectionTranslation translation = sectionTranslationRepository.save(new SectionTranslation(section, request.locale(), request.name(), request.slug(), request.summary()));
		auditLogService.log("CONTENT_SECTION_CREATED", actor, translation.getSlug(), "Section created");

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

	public ApiResponse<TopicDetailResponse> createTopic(AdminCreateTopicRequest request, String actor) {
		validateTopicSlug(request.slug(), request.locale(), null);
		Section section = sectionRepository.findById(request.sectionId())
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + request.sectionId()));
		Instant now = Instant.now();
		Topic topic = topicRepository.save(new Topic(section, request.topicType(), ContentStatus.DRAFT, request.sortOrder(), false, true, null, null, now, now));
		TopicTranslation translation = topicTranslationRepository.save(
			new TopicTranslation(topic, request.locale(), request.title(), request.slug(), request.summary(), request.introText())
		);
		auditLogService.log("CONTENT_TOPIC_CREATED", actor, translation.getSlug(), "Topic created");

		return new ApiResponse<>("Topic created", new TopicDetailResponse(
			topic.getId(),
			section.getId(),
			translation.getTitle(),
			translation.getSlug(),
			translation.getSummary(),
			translation.getIntroText(),
			topic.getTopicType().name(),
			topic.getStatus().name(),
			topic.getScheduledPublishAt(),
			List.of()
		));
	}

	public ApiResponse<TopicBlockResponse> createTopicBlock(Long topicId, AdminCreateTopicBlockRequest request, String actor) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		Instant now = Instant.now();
		TopicBlock topicBlock = topicBlockRepository.save(
			new TopicBlock(topic, request.blockType(), request.sortOrder(), ContentStatus.DRAFT, request.anchorKey(), false, now, now)
		);
		TopicBlockTranslation translation = topicBlockTranslationRepository.save(
			new TopicBlockTranslation(topicBlock, request.locale(), request.title(), request.body())
		);
		auditLogService.log("CONTENT_BLOCK_CREATED", actor, String.valueOf(topicBlock.getId()), "Topic block created");

		return new ApiResponse<>("Topic block created", new TopicBlockResponse(
			topicBlock.getId(),
			translation.getTitle(),
			translation.getBody(),
			topicBlock.getBlockType().name(),
			topicBlock.getAnchorKey(),
			topicBlock.getSortOrder()
		));
	}

	public ApiResponse<AdminSectionResponse> updateSection(Long sectionId, AdminUpdateSectionRequest request, String actor) {
		Section section = sectionRepository.findById(sectionId)
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
		validateSectionSlug(request.slug(), request.locale(), sectionId);
		Section parent = request.parentId() == null ? null : sectionRepository.findById(request.parentId())
			.orElseThrow(() -> new ResourceNotFoundException("Parent section not found: " + request.parentId()));
		SectionTranslation translation = sectionTranslationRepository.findBySection_IdAndLocale(sectionId, request.locale())
			.orElseGet(() -> new SectionTranslation(section, request.locale(), request.name(), request.slug(), request.summary()));
		section.updateStructure(parent, request.type(), request.sortOrder());
		section.touch(Instant.now());
		translation.update(request.name(), request.slug(), request.summary());
		sectionRepository.save(section);
		sectionTranslationRepository.save(translation);
		auditLogService.log("CONTENT_SECTION_UPDATED", actor, translation.getSlug(), "Section updated");
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

	public ApiResponse<TopicDetailResponse> updateTopic(Long topicId, AdminUpdateTopicRequest request, String actor) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		validateTopicSlug(request.slug(), request.locale(), topicId);
		Section section = sectionRepository.findById(request.sectionId())
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + request.sectionId()));
		TopicTranslation translation = topicTranslationRepository.findByTopic_IdAndLocale(topicId, request.locale())
			.orElseGet(() -> new TopicTranslation(topic, request.locale(), request.title(), request.slug(), request.summary(), request.introText()));
		topic.updateStructure(section, request.topicType(), request.sortOrder());
		topic.touch(Instant.now());
		translation.update(request.title(), request.slug(), request.summary(), request.introText());
		topicRepository.save(topic);
		topicTranslationRepository.save(translation);
		auditLogService.log("CONTENT_TOPIC_UPDATED", actor, translation.getSlug(), "Topic updated");
		return new ApiResponse<>("Topic updated", getAdminTopic(topicId, request.locale()));
	}

	public ApiResponse<TopicBlockResponse> updateTopicBlock(Long blockId, AdminUpdateTopicBlockRequest request, String actor) {
		TopicBlock topicBlock = topicBlockRepository.findById(blockId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic block not found: " + blockId));
		TopicBlockTranslation translation = topicBlockTranslationRepository.findByTopicBlock_IdAndLocale(blockId, request.locale())
			.orElseGet(() -> new TopicBlockTranslation(topicBlock, request.locale(), request.title(), request.body()));
		topicBlock.updateStructure(request.blockType(), request.sortOrder(), request.anchorKey());
		topicBlock.touch(Instant.now());
		translation.update(request.title(), request.body());
		topicBlockRepository.save(topicBlock);
		topicBlockTranslationRepository.save(translation);
		auditLogService.log("CONTENT_BLOCK_UPDATED", actor, String.valueOf(topicBlock.getId()), "Topic block updated");
		return new ApiResponse<>("Topic block updated", new TopicBlockResponse(
			topicBlock.getId(),
			translation.getTitle(),
			translation.getBody(),
			topicBlock.getBlockType().name(),
			topicBlock.getAnchorKey(),
			topicBlock.getSortOrder()
		));
	}

	@Transactional
	public ApiResponse<String> deleteTopic(Long topicId, String actor) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		if (!(topic.getStatus() == ContentStatus.DRAFT || topic.getStatus() == ContentStatus.ARCHIVED)) {
			throw new IllegalArgumentException("Only draft or archived topics can be deleted.");
		}
		topicBlockTranslationRepository.deleteByTopicBlock_Topic_Id(topicId);
		topicBlockRepository.deleteByTopic_Id(topicId);
		topicTranslationRepository.deleteByTopic_Id(topicId);
		topicRepository.delete(topic);
		auditLogService.log("CONTENT_TOPIC_DELETED", actor, String.valueOf(topicId), "Topic deleted");
		return new ApiResponse<>("Topic deleted", topicId.toString());
	}

	@Transactional
	public ApiResponse<String> deleteTopicBlock(Long blockId, String actor) {
		TopicBlock topicBlock = topicBlockRepository.findById(blockId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic block not found: " + blockId));
		if (!(topicBlock.getStatus() == ContentStatus.DRAFT || topicBlock.getStatus() == ContentStatus.ARCHIVED)) {
			throw new IllegalArgumentException("Only draft or archived topic blocks can be deleted.");
		}
		topicBlockTranslationRepository.deleteByTopicBlock_Id(blockId);
		topicBlockRepository.delete(topicBlock);
		auditLogService.log("CONTENT_BLOCK_DELETED", actor, String.valueOf(blockId), "Topic block deleted");
		return new ApiResponse<>("Topic block deleted", blockId.toString());
	}

	public ApiResponse<AdminSectionResponse> transitionSection(Long sectionId, SectionWorkflowActionRequest request, String actor) {
		Section section = sectionRepository.findById(sectionId)
			.orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
		String action = request.action() == null ? "" : request.action().trim().toUpperCase();
		switch (action) {
			case "PUBLISH" -> {
				if (topicRepository.countBySection_IdAndStatus(sectionId, ContentStatus.PUBLISHED) == 0) {
					throw new IllegalArgumentException("Section must contain at least one published topic before it can be published.");
				}
				section.changeStatus(ContentStatus.PUBLISHED);
			}
			case "ARCHIVE" -> {
				if (topicRepository.countBySection_IdAndStatus(sectionId, ContentStatus.PUBLISHED) > 0) {
					throw new IllegalArgumentException("Section cannot be archived while it still contains published topics.");
				}
				section.changeStatus(ContentStatus.ARCHIVED);
			}
			default -> throw new IllegalArgumentException("Unsupported section workflow action: " + request.action());
		}
		section.touch(Instant.now());
		Section savedSection = sectionRepository.save(section);
		SectionTranslation translation = sectionTranslationRepository.findBySection_IdAndLocale(savedSection.getId(), LanguageCode.EN)
			.orElseGet(() -> sectionTranslationRepository.findBySection_IdAndLocale(savedSection.getId(), LanguageCode.FR)
				.orElseGet(() -> sectionTranslationRepository.findBySection_IdAndLocale(savedSection.getId(), LanguageCode.KIN)
					.orElseThrow(() -> new ResourceNotFoundException("Section translation not found: " + sectionId))));
		auditLogService.log("CONTENT_SECTION_WORKFLOW_UPDATED", actor, translation.getSlug(), "Section workflow action: " + action);
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

	private SectionSummaryResponse toSectionSummary(SectionTranslation translation) {
		return new SectionSummaryResponse(
			translation.getSection().getId(),
			translation.getSection().getParent() == null ? null : translation.getSection().getParent().getId(),
			translation.getName(),
			translation.getSlug(),
			translation.getSummary(),
			translation.getSection().getType().name(),
			translation.getSection().getSortOrder()
		);
	}

	private String getSectionSlug(Long sectionId, LanguageCode locale) {
		return sectionTranslationRepository.findBySection_IdAndLocale(sectionId, locale)
			.orElseGet(() -> sectionTranslationRepository.findBySection_IdAndLocale(sectionId, LanguageCode.EN)
				.orElseThrow(() -> new ResourceNotFoundException("Section translation not found for section: " + sectionId)))
			.getSlug();
	}
}
