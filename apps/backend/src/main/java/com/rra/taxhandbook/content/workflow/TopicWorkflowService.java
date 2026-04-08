package com.rra.taxhandbook.content.workflow;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicWorkflowResponse;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;

@Service
public class TopicWorkflowService {

	private final TopicRepository topicRepository;
	private final TopicTranslationRepository topicTranslationRepository;

	public TopicWorkflowService(TopicRepository topicRepository, TopicTranslationRepository topicTranslationRepository) {
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
	}

	public ApiResponse<TopicWorkflowResponse> transitionTopic(Long topicId, TopicWorkflowActionRequest request, Authentication authentication) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
		TopicTranslation translation = topicTranslationRepository.findFirstByTopic_IdOrderByIdAsc(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic translation not found: " + topicId));
		TopicWorkflowAction action = parseAction(request.action());

		applyAction(topic, action, authentication);
		topic.touch(Instant.now());
		topicRepository.save(topic);

		return new ApiResponse<>("Topic workflow updated", new TopicWorkflowResponse(
			topic.getId(),
			translation.getTitle(),
			translation.getSlug(),
			topic.getStatus().name(),
			action.name(),
			authentication.getName()
		));
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

	private void applyAction(Topic topic, TopicWorkflowAction action, Authentication authentication) {
		switch (action) {
			case SUBMIT_FOR_REVIEW -> {
				requireAnyRole(authentication, "EDITOR", "ADMIN", "SUPER_ADMIN");
				requireStatus(topic, ContentStatus.DRAFT, ContentStatus.REVIEW);
				topic.changeStatus(ContentStatus.REVIEW);
			}
			case REQUEST_CHANGES -> {
				requireAnyRole(authentication, "REVIEWER", "ADMIN", "SUPER_ADMIN");
				requireStatus(topic, ContentStatus.REVIEW);
				topic.changeStatus(ContentStatus.DRAFT);
			}
			case APPROVE -> {
				requireAnyRole(authentication, "REVIEWER", "ADMIN", "SUPER_ADMIN");
				requireStatus(topic, ContentStatus.REVIEW);
				topic.changeStatus(ContentStatus.APPROVED);
			}
			case PUBLISH -> {
				requireAnyRole(authentication, "PUBLISHER", "ADMIN", "SUPER_ADMIN");
				requireStatus(topic, ContentStatus.APPROVED, ContentStatus.PUBLISHED);
				topic.changeStatus(ContentStatus.PUBLISHED);
				topic.publishNow(Instant.now());
			}
			case ARCHIVE -> {
				requireAnyRole(authentication, "PUBLISHER", "ADMIN", "SUPER_ADMIN");
				requireStatus(topic, ContentStatus.APPROVED, ContentStatus.PUBLISHED, ContentStatus.ARCHIVED);
				topic.changeStatus(ContentStatus.ARCHIVED);
			}
		}
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
