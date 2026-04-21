package com.rra.taxhandbook.content.dto;

import java.util.List;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record AdminHomepageRequest(
	LanguageCode locale,
	String kicker,
	String title,
	String subtitle,
	String searchLabel,
	String helpLabel,
	List<AdminHomepageCardRequest> cards
) {}
