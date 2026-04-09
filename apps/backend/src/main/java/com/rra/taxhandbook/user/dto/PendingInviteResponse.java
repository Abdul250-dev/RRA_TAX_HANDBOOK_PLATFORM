package com.rra.taxhandbook.user.dto;

public record PendingInviteResponse(
	Long userId,
	String userCode,
	String fullName,
	String email,
	String roleName,
	String preferredLocale,
	String inviteToken,
	String expiresAt,
	boolean expired,
	String status
) {
}
