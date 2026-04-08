package com.rra.taxhandbook.faq.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record FAQResponse(
	Long id,
	String question,
	String answer,
	LanguageCode language
) {
}
