package com.rra.taxhandbook.auth.dto;

public record InvitePreviewResponse(
	boolean valid,
	boolean expired,
	String email,
	String fullName,
	String roleName,
	String preferredLocale,
	String expiresAt,
	String message
) {
}
