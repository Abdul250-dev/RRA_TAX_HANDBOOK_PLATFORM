package com.rra.taxhandbook.user.dto;

public record UserInviteResponse(
	Long userId,
	String email,
	String inviteToken,
	String expiresAt,
	String status
) {
}
