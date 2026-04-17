package com.rra.taxhandbook.user.dto;

public record PendingInviteResponse(
	Long userId,
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
	String inviteToken,
	String expiresAt,
	boolean expired,
	String status
) {
}
