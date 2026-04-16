package com.rra.taxhandbook.user.dto;

public record UserResponse(
	Long id,
	String employeeId,
	String userCode,
	String username,
	String firstName,
	String lastName,
	String fullName,
	String email,
	String phoneNumber,
	String department,
	String position,
	String roleName,
	String preferredLocale,
	String source,
	String status,
	boolean isActive,
	boolean isLocked,
	int failedLoginAttempts,
	String lastLoginAt
) {
}
