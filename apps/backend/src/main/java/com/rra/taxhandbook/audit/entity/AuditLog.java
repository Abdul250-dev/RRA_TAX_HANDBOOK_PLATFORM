package com.rra.taxhandbook.audit.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String action;

	@Column(nullable = false)
	private String actor;

	@Column(name = "target_email")
	private String targetEmail;

	@Column(name = "details", length = 2000)
	private String details;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AuditLog() {
	}

	public AuditLog(String action, String actor, String targetEmail, String details, Instant createdAt) {
		this.action = action;
		this.actor = actor;
		this.targetEmail = targetEmail;
		this.details = details;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getAction() {
		return action;
	}

	public String getActor() {
		return actor;
	}

	public String getTargetEmail() {
		return targetEmail;
	}

	public String getDetails() {
		return details;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
