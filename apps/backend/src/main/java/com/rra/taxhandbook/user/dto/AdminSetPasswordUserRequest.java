package com.rra.taxhandbook.user.dto;

import com.rra.taxhandbook.common.enums.LanguageCode;

public record AdminSetPasswordUserRequest(
	String employeeId,
	String firstName,
	String lastName,
	String email,
	String username,
	String roleName,
	LanguageCode preferredLocale,
	String phoneNumber,
	String department,
	String position,
	String password
) {
}
