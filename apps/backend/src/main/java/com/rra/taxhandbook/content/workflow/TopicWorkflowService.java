package com.rra.taxhandbook.content.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicWorkflowHistoryResponse;
import com.rra.taxhandbook.content.dto.TopicWorkflowResponse;
import com.rra.taxhandbook.content.dto.TopicPublishReadinessResponse;
import com.rra.taxhandbook.content.dto.ScheduledPublishProcessingResponse;
import com.rra.taxhandbook.content.dto.ScheduledPublishProcessingResponse.SkippedScheduledPublishResponse;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.service.TopicPublishReadinessService;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;

@Service
public class TopicWorkflowService {

	private final TopicRepository topicRepository;
	private final TopicTranslationRepository topicTranslationRepository;
	private final TopicBlockRepository topicBlockRepository;
	private final SectionRepository sectionRepository;
	private final TopicWorkflowHistoryRepository topicWorkflowHistoryRepository;
	private final TopicPublishReadinessService topicPublishReadinessService;
	private final AuditLogService auditLogService;

	public TopicWorkflowService(
		TopicRepository topicRepository,
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		SectionRepository sectionRepository,
		TopicWorkflowHistoryRepository topicWorkflowHistoryRepository,
		TopicPublishReadinessService topicPublishReadinessService,
		AuditLogService auditLogService
	) {
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.sectionRepository = sectionRepository;
		this.topicWorkflowHistoryRepository = topicWorkflowHistoryRepository;
		this.topicPublishReadinessService = topicPublishReadinessService;
		this.auditLogService = auditLogService;
	}

	public ApiResponse<TopicWorkflowResponse> transitionTopic(Long topicId, TopicWorkflowActionRequest request, Authentication authentication) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		TopicTranslation translation = topicTranslationRepository.findFirstByTopic_IdOrderByIdAsc(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found: " + topicId));
		TopicWorkflowAction action = parseAction(request.action());
		ContentStatus fromStatus = topic.getStatus();
		String comment = normalizeComment(request.comment());

		applyAction(topic, action, request, authentication, comment);
		ContentStatus toStatus = topic.getStatus();
		Instant now = Instant.now();
		topic.touch(now);
		topicRepository.save(topic);
		syncDependentContent(topic);
		saveWorkflowHistory(topic, action, fromStatus, toStatus, comment, authentication.getName(), now);
		auditLogService.log("CONTENT_TOPIC_WORKFLOW_UPDATED", authentication.getName(), translation.getSlug(), buildAuditDetails(action, comment));

		return new ApiResponse<>("Topic workflow updated", new TopicWorkflowResponse(
			topic.getId(),
			translation.getTitle(),
			translation.getSlug(),
			topic.getStatus().name(),
			action.name(),
			authentication.getName(),
			comment,
			topic.getScheduledPublishAt()
		));
	}

	public List<TopicWorkflowHistoryResponse> getWorkflowHistory(Long topicId) {
		if (!topicRepository.existsById(topicId)) {
			throw new ResourceNotFoundException("Topic not found: " + topicId);
		}
		return topicWorkflowHistoryRepository.findByTopic_IdOrderByCreatedAtDesc(topicId).stream()
			.map(history -> new TopicWorkflowHistoryResponse(
				history.getId(),
				history.getTopic().getId(),
				history.getAction().name(),
				history.getFromStatus().name(),
				history.getToStatus().name(),
				history.getComment(),
				history.getPerformedBy(),
				history.getCreatedAt()
			))
			.toList();
	}

	public TopicPublishReadinessResponse getPublishReadiness(Long topicId) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		return topicPublishReadinessService.getPublishReadiness(topic);
	}

	public ApiResponse<ScheduledPublishProcessingResponse> processScheduledPublishes(Authentication authentication) {
		Instant now = Instant.now();
		List<Long> processedTopicIds = new ArrayList<>();
		List<SkippedScheduledPublishResponse> skippedTopics = new ArrayList<>();
		for (Topic topic : topicRepository.findByStatusAndScheduledPublishAtLessThanEqualOrderByScheduledPublishAtAsc(ContentStatus.APPROVED, now)) {
			List<String> issues = topicPublishReadinessService.collectPublishReadinessIssues(topic);
			if (!issues.isEmpty()) {
				skippedTopics.add(new SkippedScheduledPublishResponse(topic.getId(), issues));
				continue;
			}
			topic.changeStatus(ContentStatus.PUBLISHED);
			topic.publishNow(now);
			topic.touch(now);
			topicRepository.save(topic);
			syncDependentContent(topic);
			saveWorkflowHistory(topic, TopicWorkflowAction.PUBLISH, ContentStatus.APPROVED, ContentStatus.PUBLISHED, "Scheduled publish executed.", authentication.getName(), now);
			TopicTranslation translation = topicTranslationRepository.findFirstByTopic_IdOrderByIdAsc(topic.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found: " + topic.getId()));
			auditLogService.log("CONTENT_TOPIC_SCHEDULED_PUBLISH_EXECUTED", authentication.getName(), translation.getSlug(), "Scheduled topic published");
			processedTopicIds.add(topic.getId());
		}
		return new ApiResponse<>(
			"Scheduled publish processing completed",
			new ScheduledPublishProcessingResponse(processedTopicIds.size(), skippedTopics.size(), processedTopicIds, skippedTopics)
		);
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

	private void applyAction(Topic topic, TopicWorkflowAction action, TopicWorkflowActionRequest request, Authentication authentication, String comment) {
		switch (action) {
			case SUBMIT_FOR_REVIEW -> {
				requireAnyRole(authentication, "EDITOR", "CONTENT_OFFICER", "ADMIN");
				requireStatus(topic, ContentStatus.DRAFT, ContentStatus.REVIEW);
				topic.clearScheduledPublish();
				topic.changeStatus(ContentStatus.REVIEW);
			}
			case REQUEST_CHANGES -> {
				requireAnyRole(authentication, "REVIEWER", "PUBLISHER", "ADMIN");
				requireStatus(topic, ContentStatus.REVIEW, ContentStatus.APPROVED);
				requireRequiredComment(action, comment);
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

	private void saveWorkflowHistory(Topic topic, TopicWorkflowAction action, ContentStatus fromStatus, ContentStatus toStatus, String comment, String performedBy, Instant createdAt) {
		topicWorkflowHistoryRepository.save(new TopicWorkflowHistory(topic, action, fromStatus, toStatus, comment, performedBy, createdAt));
	}

	private String normalizeComment(String comment) {
		if (comment == null || comment.isBlank()) {
			return null;
		}
		String normalized = comment.trim();
		if (normalized.length() > 4000) {
			throw new IllegalArgumentException("Workflow comment must not exceed 4000 characters.");
		}
		return normalized;
	}

	private void requireRequiredComment(TopicWorkflowAction action, String comment) {
		if (action == TopicWorkflowAction.REQUEST_CHANGES && comment == null) {
			throw new IllegalArgumentException("A comment is required when requesting changes so editors know what to fix.");
		}
	}

	private String buildAuditDetails(TopicWorkflowAction action, String comment) {
		if (comment == null) {
			return "Topic workflow action: " + action.name();
		}
		return "Topic workflow action: " + action.name() + ". Comment: " + comment;
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
		List<String> issues = topicPublishReadinessService.collectPublishReadinessIssues(topic);
		if (!issues.isEmpty()) {
			throw new IllegalArgumentException("Topic is not ready to publish: " + String.join(" ", issues));
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
