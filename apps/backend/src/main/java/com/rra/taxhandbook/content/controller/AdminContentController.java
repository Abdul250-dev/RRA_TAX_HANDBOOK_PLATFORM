package com.rra.taxhandbook.content.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.AdminSectionResponse;
import com.rra.taxhandbook.content.dto.AdminUpdateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateTopicRequest;
import com.rra.taxhandbook.content.dto.ContentSummaryResponse;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.ScheduledPublishProcessingResponse;
import com.rra.taxhandbook.content.dto.SectionWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicBlockResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicSummaryResponse;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicWorkflowResponse;
import com.rra.taxhandbook.content.service.ContentStructureService;
import com.rra.taxhandbook.content.workflow.TopicWorkflowService;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

	private final ContentStructureService contentStructureService;
	private final TopicWorkflowService topicWorkflowService;

	public AdminContentController(ContentStructureService contentStructureService, TopicWorkflowService topicWorkflowService) {
		this.contentStructureService = contentStructureService;
		this.topicWorkflowService = topicWorkflowService;
	}

	@GetMapping("/sections")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','REVIEWER','PUBLISHER','ADMIN','AUDITOR','VIEWER')")
	public java.util.List<AdminSectionResponse> getSections(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminSections(locale);
	}

	@GetMapping("/summary")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','REVIEWER','PUBLISHER','ADMIN','AUDITOR','VIEWER')")
	public ContentSummaryResponse getSummary() {
		return contentStructureService.getContentSummary();
	}

	@GetMapping("/topics")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','REVIEWER','PUBLISHER','ADMIN','AUDITOR','VIEWER')")
	public java.util.List<TopicSummaryResponse> getTopics(
		@RequestParam(defaultValue = "EN") LanguageCode locale,
		@RequestParam(required = false) ContentStatus status
	) {
		return contentStructureService.getAdminTopics(locale, status);
	}

	@GetMapping("/topics/review-queue")
	@PreAuthorize("hasAnyRole('REVIEWER','ADMIN','AUDITOR','VIEWER')")
	public java.util.List<TopicSummaryResponse> getReviewQueue(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminTopics(locale, ContentStatus.REVIEW);
	}

	@GetMapping("/topics/publish-queue")
	@PreAuthorize("hasAnyRole('PUBLISHER','ADMIN','AUDITOR','VIEWER')")
	public java.util.List<TopicSummaryResponse> getPublishQueue(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminTopics(locale, ContentStatus.APPROVED);
	}

	@GetMapping("/topics/{topicId}")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','REVIEWER','PUBLISHER','ADMIN','AUDITOR','VIEWER')")
	public TopicDetailResponse getTopic(@PathVariable Long topicId, @RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminTopic(topicId, locale);
	}

	@PostMapping("/sections")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','ADMIN')")
	public ApiResponse<SectionSummaryResponse> createSection(@RequestBody AdminCreateSectionRequest request, Authentication authentication) {
		return contentStructureService.createSection(request, authentication.getName());
	}

	@PutMapping("/sections/{sectionId}")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','ADMIN')")
	public ApiResponse<AdminSectionResponse> updateSection(@PathVariable Long sectionId, @RequestBody AdminUpdateSectionRequest request, Authentication authentication) {
		return contentStructureService.updateSection(sectionId, request, authentication.getName());
	}

	@PostMapping("/sections/{sectionId}/workflow")
	@PreAuthorize("hasAnyRole('PUBLISHER','ADMIN')")
	public ApiResponse<AdminSectionResponse> transitionSection(@PathVariable Long sectionId, @RequestBody SectionWorkflowActionRequest request, Authentication authentication) {
		return contentStructureService.transitionSection(sectionId, request, authentication.getName());
	}

	@PostMapping("/topics")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','ADMIN')")
	public ApiResponse<TopicDetailResponse> createTopic(@RequestBody AdminCreateTopicRequest request, Authentication authentication) {
		return contentStructureService.createTopic(request, authentication.getName());
	}

	@PutMapping("/topics/{topicId}")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','ADMIN')")
	public ApiResponse<TopicDetailResponse> updateTopic(@PathVariable Long topicId, @RequestBody AdminUpdateTopicRequest request, Authentication authentication) {
		return contentStructureService.updateTopic(topicId, request, authentication.getName());
	}

	@PostMapping("/topics/{topicId}/blocks")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','ADMIN')")
	public ApiResponse<TopicBlockResponse> createTopicBlock(@PathVariable Long topicId, @RequestBody AdminCreateTopicBlockRequest request, Authentication authentication) {
		return contentStructureService.createTopicBlock(topicId, request, authentication.getName());
	}

	@PutMapping("/blocks/{blockId}")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','ADMIN')")
	public ApiResponse<TopicBlockResponse> updateTopicBlock(@PathVariable Long blockId, @RequestBody AdminUpdateTopicBlockRequest request, Authentication authentication) {
		return contentStructureService.updateTopicBlock(blockId, request, authentication.getName());
	}

	@org.springframework.web.bind.annotation.DeleteMapping("/topics/{topicId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> deleteTopic(@PathVariable Long topicId, Authentication authentication) {
		return contentStructureService.deleteTopic(topicId, authentication.getName());
	}

	@org.springframework.web.bind.annotation.DeleteMapping("/blocks/{blockId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> deleteTopicBlock(@PathVariable Long blockId, Authentication authentication) {
		return contentStructureService.deleteTopicBlock(blockId, authentication.getName());
	}

	@PostMapping("/topics/{topicId}/workflow")
	@PreAuthorize("hasAnyRole('EDITOR','CONTENT_OFFICER','REVIEWER','PUBLISHER','ADMIN')")
	public ApiResponse<TopicWorkflowResponse> transitionTopic(
		@PathVariable Long topicId,
		@RequestBody TopicWorkflowActionRequest request,
		Authentication authentication
	) {
		return topicWorkflowService.transitionTopic(topicId, request, authentication);
	}

	@PostMapping("/topics/workflow/process-scheduled")
	@PreAuthorize("hasAnyRole('PUBLISHER','ADMIN')")
	public ApiResponse<ScheduledPublishProcessingResponse> processScheduledPublishes(Authentication authentication) {
		return topicWorkflowService.processScheduledPublishes(authentication);
	}
}
