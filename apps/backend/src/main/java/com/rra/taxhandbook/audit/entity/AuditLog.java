package com.rra.taxhandbook.audit.entity;

public record AuditLog(
	Long id,
	String action,
	String actor,
	String createdAt
) {
}
