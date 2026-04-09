package com.rra.taxhandbook.auth.dto;

public record PasswordResetResponse(
	String email,
	String resetToken,
	String expiresAt
) {
}
