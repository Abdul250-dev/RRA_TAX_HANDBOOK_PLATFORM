package com.rra.taxhandbook.user.dto;

public record UserResponse(
	Long id,
	String userCode,
	String fullName,
	String email,
	String roleName,
	String preferredLocale,
	String source,
	String status
) {
}
