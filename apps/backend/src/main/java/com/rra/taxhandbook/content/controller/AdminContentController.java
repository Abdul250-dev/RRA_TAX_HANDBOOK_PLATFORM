package com.rra.taxhandbook.content.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.TopicBlockResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
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

	@PostMapping("/sections")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<SectionSummaryResponse> createSection(@RequestBody AdminCreateSectionRequest request) {
		return contentStructureService.createSection(request);
	}

	@PostMapping("/topics")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicDetailResponse> createTopic(@RequestBody AdminCreateTopicRequest request) {
		return contentStructureService.createTopic(request);
	}

	@PostMapping("/topics/{topicId}/blocks")
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN','SUPER_ADMIN')")
	public ApiResponse<TopicBlockResponse> createTopicBlock(@PathVariable Long topicId, @RequestBody AdminCreateTopicBlockRequest request) {
		return contentStructureService.createTopicBlock(topicId, request);
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
