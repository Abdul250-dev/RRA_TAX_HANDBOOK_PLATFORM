package com.rra.taxhandbook.user.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record UpdateUserProfileRequest(
	String fullName,
	String email,
	LanguageCode preferredLocale
) {
}
