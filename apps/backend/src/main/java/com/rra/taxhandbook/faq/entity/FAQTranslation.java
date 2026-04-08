package com.rra.taxhandbook.faq.entity;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record FAQTranslation(
	Long faqId,
	LanguageCode language,
	String question,
	String answer
) {
}
