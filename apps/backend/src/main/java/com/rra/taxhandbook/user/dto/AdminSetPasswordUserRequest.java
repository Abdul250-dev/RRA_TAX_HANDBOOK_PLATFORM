package com.rra.taxhandbook.user.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record AdminSetPasswordUserRequest(
	String fullName,
	String email,
	String roleName,
	LanguageCode preferredLocale,
	String password
) {
}
