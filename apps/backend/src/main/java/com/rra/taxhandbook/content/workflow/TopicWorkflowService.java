package com.rra.taxhandbook.content.workflow;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicWorkflowResponse;
import com.rra.taxhandbook.content.dto.ScheduledPublishProcessingResponse;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;

@Service
public class TopicWorkflowService {

	private final TopicRepository topicRepository;
	private final TopicTranslationRepository topicTranslationRepository;
	private final TopicBlockRepository topicBlockRepository;
	private final TopicBlockTranslationRepository topicBlockTranslationRepository;
	private final SectionRepository sectionRepository;
	private final SectionTranslationRepository sectionTranslationRepository;
	private final AuditLogService auditLogService;
	private final Set<LanguageCode> requiredPublishLocales;

	public TopicWorkflowService(
		TopicRepository topicRepository,
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		SectionRepository sectionRepository,
		TopicBlockTranslationRepository topicBlockTranslationRepository,
		SectionTranslationRepository sectionTranslationRepository,
		AuditLogService auditLogService,
		@Value("${app.content.required-publish-locales:EN}") String requiredPublishLocales
	) {
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.sectionRepository = sectionRepository;
		this.topicBlockTranslationRepository = topicBlockTranslationRepository;
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.auditLogService = auditLogService;
		this.requiredPublishLocales = parseRequiredPublishLocales(requiredPublishLocales);
	}

	public ApiResponse<TopicWorkflowResponse> transitionTopic(Long topicId, TopicWorkflowActionRequest request, Authentication authentication) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		TopicTranslation translation = topicTranslationRepository.findFirstByTopic_IdOrderByIdAsc(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found: " + topicId));
		TopicWorkflowAction action = parseAction(request.action());

		applyAction(topic, action, request, authentication);
		topic.touch(Instant.now());
		topicRepository.save(topic);
		syncDependentContent(topic);
		auditLogService.log("CONTENT_TOPIC_WORKFLOW_UPDATED", authentication.getName(), translation.getSlug(), "Topic workflow action: " + action.name());

		return new ApiResponse<>("Topic workflow updated", new TopicWorkflowResponse(
			topic.getId(),
			translation.getTitle(),
			translation.getSlug(),
			topic.getStatus().name(),
			action.name(),
			authentication.getName(),
			topic.getScheduledPublishAt()
		));
	}

	public ApiResponse<ScheduledPublishProcessingResponse> processScheduledPublishes(Authentication authentication) {
		Instant now = Instant.now();
		int processedCount = 0;
		for (Topic topic : topicRepository.findByStatusAndScheduledPublishAtLessThanEqualOrderByScheduledPublishAtAsc(ContentStatus.APPROVED, now)) {
			if (!isPublishable(topic)) {
				continue;
			}
			try {
				validateRequiredPublishLocales(topic);
			}
			catch (IllegalArgumentException ex) {
				continue;
			}
			topic.changeStatus(ContentStatus.PUBLISHED);
			topic.publishNow(now);
			topic.touch(now);
			topicRepository.save(topic);
			syncDependentContent(topic);
			TopicTranslation translation = topicTranslationRepository.findFirstByTopic_IdOrderByIdAsc(topic.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found: " + topic.getId()));
			auditLogService.log("CONTENT_TOPIC_SCHEDULED_PUBLISH_EXECUTED", authentication.getName(), translation.getSlug(), "Scheduled topic published");
			processedCount++;
		}
		return new ApiResponse<>("Scheduled publish processing completed", new ScheduledPublishProcessingResponse(processedCount));
	}

	private TopicWorkflowAction parseAction(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Workflow action is required.");
		}
		try {
			return TopicWorkflowAction.valueOf(value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Unsupported workflow action: " + value);
		}
	}

	private void applyAction(Topic topic, TopicWorkflowAction action, TopicWorkflowActionRequest request, Authentication authentication) {
		switch (action) {
			case SUBMIT_FOR_REVIEW -> {
				requireAnyRole(authentication, "EDITOR", "ADMIN");
				requireStatus(topic, ContentStatus.DRAFT, ContentStatus.REVIEW);
				topic.clearScheduledPublish();
				topic.changeStatus(ContentStatus.REVIEW);
			}
			case REQUEST_CHANGES -> {
				requireAnyRole(authentication, "REVIEWER", "ADMIN");
				requireStatus(topic, ContentStatus.REVIEW);
				topic.clearScheduledPublish();
				topic.changeStatus(ContentStatus.DRAFT);
			}
			case APPROVE -> {
				requireAnyRole(authentication, "REVIEWER", "ADMIN");
				requireStatus(topic, ContentStatus.REVIEW);
				topic.changeStatus(ContentStatus.APPROVED);
			}
			case SCHEDULE_PUBLISH -> {
				requireAnyRole(authentication, "PUBLISHER", "ADMIN");
				requireStatus(topic, ContentStatus.APPROVED);
				requirePublishableTopic(topic);
				Instant scheduledAt = requireFutureScheduledAt(request.scheduledAt());
				topic.schedulePublish(scheduledAt);
			}
			case PUBLISH -> {
				requireAnyRole(authentication, "PUBLISHER", "ADMIN");
				requireStatus(topic, ContentStatus.APPROVED, ContentStatus.PUBLISHED);
				requirePublishableTopic(topic);
				topic.changeStatus(ContentStatus.PUBLISHED);
				topic.publishNow(Instant.now());
			}
			case UNPUBLISH -> {
				requireAnyRole(authentication, "PUBLISHER", "ADMIN");
				requireStatus(topic, ContentStatus.PUBLISHED);
				topic.clearScheduledPublish();
				topic.changeStatus(ContentStatus.APPROVED);
			}
			case ARCHIVE -> {
				requireAnyRole(authentication, "PUBLISHER", "ADMIN");
				requireStatus(topic, ContentStatus.APPROVED, ContentStatus.PUBLISHED, ContentStatus.ARCHIVED);
				topic.clearScheduledPublish();
				topic.changeStatus(ContentStatus.ARCHIVED);
			}
		}
	}

	private void syncDependentContent(Topic topic) {
		ContentStatus topicStatus = topic.getStatus();
		var blocks = topicBlockRepository.findByTopic_IdOrderBySortOrderAsc(topic.getId());
		blocks.forEach(block -> {
			block.changeStatus(topicStatus);
			block.touch(Instant.now());
		});
		topicBlockRepository.saveAll(blocks);
		if (topicStatus == ContentStatus.PUBLISHED && topic.getSection().getStatus() != ContentStatus.PUBLISHED) {
			topic.getSection().changeStatus(ContentStatus.PUBLISHED);
			topic.getSection().touch(Instant.now());
			sectionRepository.save(topic.getSection());
		}
		if (topicStatus != ContentStatus.PUBLISHED && topic.getSection().getStatus() == ContentStatus.PUBLISHED) {
			long publishedTopics = topicRepository.countBySection_IdAndStatus(topic.getSection().getId(), ContentStatus.PUBLISHED);
			if (publishedTopics == 0) {
				topic.getSection().changeStatus(ContentStatus.APPROVED);
				topic.getSection().touch(Instant.now());
				sectionRepository.save(topic.getSection());
			}
		}
	}

	private void requirePublishableTopic(Topic topic) {
		if (!isPublishable(topic)) {
			throw new IllegalArgumentException("Topic must contain at least one content block before it can be published.");
		}
		validateRequiredPublishLocales(topic);
	}

	private boolean isPublishable(Topic topic) {
		var blocks = topicBlockRepository.findByTopic_IdOrderBySortOrderAsc(topic.getId());
		return !blocks.isEmpty();
	}

	private void validateRequiredPublishLocales(Topic topic) {
		var blocks = topicBlockRepository.findByTopic_IdOrderBySortOrderAsc(topic.getId());
		long availableTopicLocales = topicTranslationRepository.countByTopic_IdAndLocaleIn(topic.getId(), requiredPublishLocales);
		if (availableTopicLocales != requiredPublishLocales.size()) {
			throw new IllegalArgumentException("Topic is missing one or more required publish locales: " + requiredPublishLocales + ".");
		}
		long availableSectionLocales = sectionTranslationRepository.countBySection_IdAndLocaleIn(topic.getSection().getId(), requiredPublishLocales);
		if (availableSectionLocales != requiredPublishLocales.size()) {
			throw new IllegalArgumentException("Section is missing one or more required publish locales: " + requiredPublishLocales + ".");
		}
		for (var block : blocks) {
			long availableBlockLocales = topicBlockTranslationRepository.countByTopicBlock_IdAndLocaleIn(block.getId(), requiredPublishLocales);
			if (availableBlockLocales != requiredPublishLocales.size()) {
				throw new IllegalArgumentException("Topic block " + block.getId() + " is missing one or more required publish locales: " + requiredPublishLocales + ".");
			}
		}
	}

	private Instant requireFutureScheduledAt(Instant scheduledAt) {
		if (scheduledAt == null) {
			throw new IllegalArgumentException("scheduledAt is required for SCHEDULE_PUBLISH.");
		}
		if (!scheduledAt.isAfter(Instant.now())) {
			throw new IllegalArgumentException("scheduledAt must be a future timestamp.");
		}
		return scheduledAt;
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

	private void requireAnyRole(Authentication authentication, String... roles) {
		for (String role : roles) {
			boolean hasRole = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
			if (hasRole) {
				return;
			}
		}
		throw new UnauthorizedException("You do not have permission to perform this workflow action.");
	}

	private void requireStatus(Topic topic, ContentStatus... allowedStatuses) {
		for (ContentStatus allowedStatus : allowedStatuses) {
			if (topic.getStatus() == allowedStatus) {
				return;
			}
		}
		throw new IllegalArgumentException("Workflow action is not allowed when topic status is " + topic.getStatus().name() + ".");
	}
}
