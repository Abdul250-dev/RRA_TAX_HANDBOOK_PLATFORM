package com.rra.taxhandbook.faq.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.faq.dto.FAQRequest;
import com.rra.taxhandbook.faq.dto.FAQResponse;
import com.rra.taxhandbook.faq.service.FAQService;

@RestController
@RequestMapping("/api/admin/faqs")
public class AdminFAQController {

	private final FAQService faqService;

	public AdminFAQController(FAQService faqService) {
		this.faqService = faqService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
	public ApiResponse<FAQResponse> createFAQ(@RequestBody FAQRequest request) {
		return faqService.createFAQ(request);
	}
}
