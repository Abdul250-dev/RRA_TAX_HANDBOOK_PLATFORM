package com.rra.taxhandbook.notification;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_notifications")
public class EmailNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmailNotificationType type;

	@Column(name = "recipient_email", nullable = false)
	private String recipientEmail;

	@Column(name = "recipient_name", nullable = false)
	private String recipientName;

	@Column(name = "recipient_username")
	private String recipientUsername;

	@Column(nullable = false, length = 512)
	private String token;

	@Column(name = "token_expires_at", nullable = false)
	private Instant tokenExpiresAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmailNotificationStatus status;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "max_attempts", nullable = false)
	private int maxAttempts;

	@Column(name = "next_attempt_at", nullable = false)
	private Instant nextAttemptAt;

	@Column(name = "last_attempt_at")
	private Instant lastAttemptAt;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "last_error", length = 2000)
	private String lastError;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected EmailNotification() {
	}

	public EmailNotification(
		EmailNotificationType type,
		String recipientEmail,
		String recipientName,
		String recipientUsername,
		String token,
		Instant tokenExpiresAt,
		int maxAttempts,
		Instant nextAttemptAt
	) {
		this.type = type;
		this.recipientEmail = recipientEmail;
		this.recipientName = recipientName;
		this.recipientUsername = recipientUsername;
		this.token = token;
		this.tokenExpiresAt = tokenExpiresAt;
		this.status = EmailNotificationStatus.PENDING;
		this.attempts = 0;
		this.maxAttempts = maxAttempts;
		this.nextAttemptAt = nextAttemptAt;
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
	}

	public Long getId() {
		return id;
	}

	public EmailNotificationType getType() {
		return type;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public String getRecipientUsername() {
		return recipientUsername;
	}

	public String getToken() {
		return token;
	}

	public Instant getTokenExpiresAt() {
		return tokenExpiresAt;
	}

	public EmailNotificationStatus getStatus() {
		return status;
	}

	public int getAttempts() {
		return attempts;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public Instant getNextAttemptAt() {
		return nextAttemptAt;
	}

	public Instant getLastAttemptAt() {
		return lastAttemptAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public String getLastError() {
		return lastError;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void markSendingAttempt() {
		this.attempts = this.attempts + 1;
		this.lastAttemptAt = Instant.now();
		this.updatedAt = this.lastAttemptAt;
	}

	public void markSent() {
		Instant now = Instant.now();
		this.status = EmailNotificationStatus.SENT;
		this.sentAt = now;
		this.nextAttemptAt = now;
		this.lastError = null;
		this.updatedAt = now;
	}

	public void scheduleRetry(String errorMessage, Instant nextAttemptAt) {
		this.status = this.attempts >= this.maxAttempts ? EmailNotificationStatus.FAILED : EmailNotificationStatus.PENDING;
		this.lastError = truncate(errorMessage);
		this.nextAttemptAt = nextAttemptAt;
		this.updatedAt = Instant.now();
	}

	private String truncate(String value) {
		if (value == null) {
			return null;
		}
		return value.length() <= 2000 ? value : value.substring(0, 2000);
	}
}
