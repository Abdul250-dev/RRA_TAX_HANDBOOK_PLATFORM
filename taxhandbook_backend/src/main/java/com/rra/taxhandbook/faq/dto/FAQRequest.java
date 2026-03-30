package com.rra.taxhandbook.faq.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record FAQRequest(
	String question,
	String answer,
	LanguageCode language
) {
}
