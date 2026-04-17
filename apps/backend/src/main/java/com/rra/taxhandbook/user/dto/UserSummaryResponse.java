package com.rra.taxhandbook.user.dto;

public record UserSummaryResponse(
	long totalUsers,
	long activeUsers,
	long pendingUsers,
	long suspendedUsers,
	long deactivatedUsers
) {
}
