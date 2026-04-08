package com.rra.taxhandbook.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.faq.dto.FAQRequest;
import com.rra.taxhandbook.faq.dto.FAQResponse;

@Service
public class FAQService {

	public List<FAQResponse> getFAQs() {
		return List.of(new FAQResponse(1L, "How do I register for taxes?", "You start with TIN registration.", LanguageCode.EN));
	}

	public ApiResponse<FAQResponse> createFAQ(FAQRequest request) {
		FAQResponse response = new FAQResponse(2L, request.question(), request.answer(), request.language());
		return new ApiResponse<>("FAQ scaffold created", response);
	}
}
