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

	@Enumerated(EnumType.STRING)
	@Column(name = "preferred_locale", nullable = false)
	private LanguageCode preferredLocale;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserSource source;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	protected User() {
	}

	public User(String userCode, String fullName, String email, LanguageCode preferredLocale, UserSource source, String status, Instant createdAt, Role role) {
		this.userCode = userCode;
		this.fullName = fullName;
		this.email = email;
		this.preferredLocale = preferredLocale;
		this.source = source;
		this.status = status;
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

	public LanguageCode getPreferredLocale() {
		return preferredLocale;
	}

	public UserSource getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Role getRole() {
		return role;
	}
}
