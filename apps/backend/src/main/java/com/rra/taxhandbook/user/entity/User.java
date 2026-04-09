package com.rra.taxhandbook.user.entity;

import java.time.Instant;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.role.entity.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Keep the legacy column for compatibility while treating it as a local user code.
	@Column(name = "employee_id", nullable = false, unique = true)
	private String userCode;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "preferred_locale", nullable = false)
	private LanguageCode preferredLocale;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserSource source;

	@Column(nullable = false)
	private String status;

	@Column(name = "invite_token", unique = true)
	private String inviteToken;

	@Column(name = "invite_expires_at")
	private Instant inviteExpiresAt;

	@Column(name = "password_reset_token", unique = true)
	private String passwordResetToken;

	@Column(name = "password_reset_expires_at")
	private Instant passwordResetExpiresAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	protected User() {
	}

	public User(
		String userCode,
		String fullName,
		String email,
		String passwordHash,
		LanguageCode preferredLocale,
		UserSource source,
		String status,
		String inviteToken,
		Instant inviteExpiresAt,
		String passwordResetToken,
		Instant passwordResetExpiresAt,
		Instant createdAt,
		Role role
	) {
		this.userCode = userCode;
		this.fullName = fullName;
		this.email = email;
		this.passwordHash = passwordHash;
		this.preferredLocale = preferredLocale;
		this.source = source;
		this.status = status;
		this.inviteToken = inviteToken;
		this.inviteExpiresAt = inviteExpiresAt;
		this.passwordResetToken = passwordResetToken;
		this.passwordResetExpiresAt = passwordResetExpiresAt;
		this.createdAt = createdAt;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getUserCode() {
		return userCode;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public LanguageCode getPreferredLocale() {
		return preferredLocale;
	}

	public UserSource getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public String getInviteToken() {
		return inviteToken;
	}

	public Instant getInviteExpiresAt() {
		return inviteExpiresAt;
	}

	public String getPasswordResetToken() {
		return passwordResetToken;
	}

	public Instant getPasswordResetExpiresAt() {
		return passwordResetExpiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Role getRole() {
		return role;
	}

	public void activateWithPassword(String passwordHash) {
		this.passwordHash = passwordHash;
		this.status = "ACTIVE";
		this.inviteToken = null;
		this.inviteExpiresAt = null;
	}

	public void issuePasswordReset(String token, Instant expiresAt) {
		this.passwordResetToken = token;
		this.passwordResetExpiresAt = expiresAt;
	}

	public void resetPassword(String passwordHash) {
		this.passwordHash = passwordHash;
		this.passwordResetToken = null;
		this.passwordResetExpiresAt = null;
	}

	public void removeAccess() {
		this.status = "REMOVED";
		this.passwordHash = null;
		this.inviteToken = null;
		this.inviteExpiresAt = null;
		this.passwordResetToken = null;
		this.passwordResetExpiresAt = null;
	}

	public void reissueInvite(String inviteToken, Instant inviteExpiresAt) {
		this.status = "INVITED";
		this.inviteToken = inviteToken;
		this.inviteExpiresAt = inviteExpiresAt;
	}

	public void cancelInvite() {
		this.status = "REMOVED";
		this.inviteToken = null;
		this.inviteExpiresAt = null;
	}
}
