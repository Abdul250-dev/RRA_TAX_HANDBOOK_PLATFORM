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
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
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
	@PreAuthorize("hasAnyRole('EDITOR','REVIEWER','PUBLISHER','ADMIN','SUPER_ADMIN')")
	public java.util.List<AdminSectionResponse> getSections(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminSections(locale);
	}

	@GetMapping("/topics")
	@PreAuthorize("hasAnyRole('EDITOR','REVIEWER','PUBLISHER','ADMIN','SUPER_ADMIN')")
	public java.util.List<TopicSummaryResponse> getTopics(
		@RequestParam(defaultValue = "EN") LanguageCode locale,
		@RequestParam(required = false) ContentStatus status
	) {
		return contentStructureService.getAdminTopics(locale, status);
	}

	@GetMapping("/topics/review-queue")
	@PreAuthorize("hasAnyRole('REVIEWER','ADMIN','SUPER_ADMIN')")
	public java.util.List<TopicSummaryResponse> getReviewQueue(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminTopics(locale, ContentStatus.REVIEW);
	}

	@GetMapping("/topics/publish-queue")
	@PreAuthorize("hasAnyRole('PUBLISHER','ADMIN','SUPER_ADMIN')")
	public java.util.List<TopicSummaryResponse> getPublishQueue(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminTopics(locale, ContentStatus.APPROVED);
	}

	@GetMapping("/topics/{topicId}")
	@PreAuthorize("hasAnyRole('EDITOR','REVIEWER','PUBLISHER','ADMIN','SUPER_ADMIN')")
	public TopicDetailResponse getTopic(@PathVariable Long topicId, @RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getAdminTopic(topicId, locale);
	}

	@PostMapping("/sections")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<SectionSummaryResponse> createSection(@RequestBody AdminCreateSectionRequest request) {
		return contentStructureService.createSection(request);
	}

	@PutMapping("/sections/{sectionId}")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<AdminSectionResponse> updateSection(@PathVariable Long sectionId, @RequestBody AdminUpdateSectionRequest request) {
		return contentStructureService.updateSection(sectionId, request);
	}

	@PostMapping("/sections/{sectionId}/workflow")
	@PreAuthorize("hasAnyRole('PUBLISHER','ADMIN','SUPER_ADMIN')")
	public ApiResponse<AdminSectionResponse> transitionSection(@PathVariable Long sectionId, @RequestBody SectionWorkflowActionRequest request) {
		return contentStructureService.transitionSection(sectionId, request);
	}

	@PostMapping("/topics")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicDetailResponse> createTopic(@RequestBody AdminCreateTopicRequest request) {
		return contentStructureService.createTopic(request);
	}

	@PutMapping("/topics/{topicId}")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicDetailResponse> updateTopic(@PathVariable Long topicId, @RequestBody AdminUpdateTopicRequest request) {
		return contentStructureService.updateTopic(topicId, request);
	}

	@PostMapping("/topics/{topicId}/blocks")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicBlockResponse> createTopicBlock(@PathVariable Long topicId, @RequestBody AdminCreateTopicBlockRequest request) {
		return contentStructureService.createTopicBlock(topicId, request);
	}

	@PutMapping("/blocks/{blockId}")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicBlockResponse> updateTopicBlock(@PathVariable Long blockId, @RequestBody AdminUpdateTopicBlockRequest request) {
		return contentStructureService.updateTopicBlock(blockId, request);
	}

	@org.springframework.web.bind.annotation.DeleteMapping("/topics/{topicId}")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<String> deleteTopic(@PathVariable Long topicId) {
		return contentStructureService.deleteTopic(topicId);
	}

	@org.springframework.web.bind.annotation.DeleteMapping("/blocks/{blockId}")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<String> deleteTopicBlock(@PathVariable Long blockId) {
		return contentStructureService.deleteTopicBlock(blockId);
	}

	@PostMapping("/topics/{topicId}/workflow")
	@PreAuthorize("hasAnyRole('EDITOR','REVIEWER','PUBLISHER','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicWorkflowResponse> transitionTopic(
		@PathVariable Long topicId,
		@RequestBody TopicWorkflowActionRequest request,
		Authentication authentication
	) {
		return topicWorkflowService.transitionTopic(topicId, request, authentication);
	}
}
