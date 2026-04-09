package com.rra.taxhandbook.audit.dto;

public record UserActivityResponse(
	Long id,
	String action,
	String actor,
	String targetEmail,
	String details,
	String createdAt
) {
}
