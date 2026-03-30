package com.rra.taxhandbook.faq.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.faq.dto.FAQRequest;
import com.rra.taxhandbook.faq.dto.FAQResponse;
import com.rra.taxhandbook.faq.service.FAQService;

@RestController
@RequestMapping("/api/faqs")
public class FAQController {

	private final FAQService faqService;

	public FAQController(FAQService faqService) {
		this.faqService = faqService;
	}

	@GetMapping
	public List<FAQResponse> getFAQs() {
		return faqService.getFAQs();
	}

	@PostMapping
	public ApiResponse<FAQResponse> createFAQ(@RequestBody FAQRequest request) {
		return faqService.createFAQ(request);
	}
}
