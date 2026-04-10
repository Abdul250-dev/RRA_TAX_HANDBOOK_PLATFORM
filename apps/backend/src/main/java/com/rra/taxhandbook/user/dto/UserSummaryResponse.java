package com.rra.taxhandbook.user.dto;

public record UserSummaryResponse(
	long totalUsers,
	long activeUsers,
	long invitedUsers,
	long suspendedUsers,
	long removedUsers
) {
}
