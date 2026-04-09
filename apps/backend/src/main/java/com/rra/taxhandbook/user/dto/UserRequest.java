package com.rra.taxhandbook.user.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record UserRequest(
	String fullName,
	String email,
	String roleName,
	LanguageCode preferredLocale
) {
}
