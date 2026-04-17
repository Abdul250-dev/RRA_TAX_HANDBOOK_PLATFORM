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

	private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "employee_id", nullable = false, unique = true)
	private String employeeId;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(unique = true)
	private String username;

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

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Column(name = "is_locked", nullable = false)
	private boolean isLocked;

	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	@Column(name = "invite_token", unique = true)
	private String inviteToken;

	@Column(name = "invite_expires_at")
	private Instant inviteExpiresAt;

	@Column(name = "password_reset_token", unique = true)
	private String passwordResetToken;

	@Column(name = "password_reset_expires_at")
	private Instant passwordResetExpiresAt;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "department")
	private String department;

	@Column(name = "position")
	private String position;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	protected User() {
	}

	public User(
		String employeeId,
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
		this(
			employeeId,
			extractFirstName(fullName),
			extractLastName(fullName),
			email,
			null,
			passwordHash,
			preferredLocale,
			source,
			status,
			"ACTIVE".equalsIgnoreCase(status),
			false,
			0,
			null,
			inviteToken,
			inviteExpiresAt,
			passwordResetToken,
			passwordResetExpiresAt,
			null,
			null,
			null,
			createdAt,
			null,
			"DEACTIVATED".equalsIgnoreCase(status) ? createdAt : null,
			null,
			null,
			role
		);
	}

	public User(
		String employeeId,
		String firstName,
		String lastName,
		String email,
		String username,
		String passwordHash,
		LanguageCode preferredLocale,
		UserSource source,
		String status,
		boolean isActive,
		boolean isLocked,
		int failedLoginAttempts,
		Instant lastLoginAt,
		String inviteToken,
		Instant inviteExpiresAt,
		String passwordResetToken,
		Instant passwordResetExpiresAt,
		String phoneNumber,
		String department,
		String position,
		Instant createdAt,
		Instant updatedAt,
		Instant deletedAt,
		Long createdBy,
		Long updatedBy,
		Role role
	) {
		this.employeeId = employeeId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
		this.passwordHash = passwordHash;
		this.preferredLocale = preferredLocale;
		this.source = source;
		this.status = status;
		this.isActive = isActive;
		this.isLocked = isLocked;
		this.failedLoginAttempts = failedLoginAttempts;
		this.lastLoginAt = lastLoginAt;
		this.inviteToken = inviteToken;
		this.inviteExpiresAt = inviteExpiresAt;
		this.passwordResetToken = passwordResetToken;
		this.passwordResetExpiresAt = passwordResetExpiresAt;
		this.phoneNumber = phoneNumber;
		this.department = department;
		this.position = position;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public String getUserCode() {
		return employeeId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFullName() {
		return (firstName + " " + lastName).trim();
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
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

	public boolean isActive() {
		return isActive;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getDepartment() {
		return department;
	}

	public String getPosition() {
		return position;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public Role getRole() {
		return role;
	}

	public void assignRole(Role role) {
		this.role = role;
		this.updatedAt = Instant.now();
	}

	public void updateProfile(
		String employeeId,
		String firstName,
		String lastName,
		String email,
		String username,
		String phoneNumber,
		String department,
		String position,
		LanguageCode preferredLocale,
		Long updatedBy
	) {
		this.employeeId = employeeId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
		this.phoneNumber = phoneNumber;
		this.department = department;
		this.position = position;
		this.preferredLocale = preferredLocale;
		this.updatedAt = Instant.now();
		this.updatedBy = updatedBy;
	}

	public void activateWithPassword(String passwordHash) {
		this.passwordHash = passwordHash;
		this.status = "ACTIVE";
		this.isActive = true;
		this.isLocked = false;
		this.failedLoginAttempts = 0;
		this.inviteToken = null;
		this.inviteExpiresAt = null;
		this.updatedAt = Instant.now();
	}

	public void issuePasswordReset(String token, Instant expiresAt) {
		this.passwordResetToken = token;
		this.passwordResetExpiresAt = expiresAt;
		this.updatedAt = Instant.now();
	}

	public void resetPassword(String passwordHash) {
		this.passwordHash = passwordHash;
		this.isLocked = false;
		this.failedLoginAttempts = 0;
		this.passwordResetToken = null;
		this.passwordResetExpiresAt = null;
		this.updatedAt = Instant.now();
	}

	public void removeAccess() {
		this.status = "DEACTIVATED";
		this.isActive = false;
		this.isLocked = false;
		this.failedLoginAttempts = 0;
		this.passwordHash = null;
		this.inviteToken = null;
		this.inviteExpiresAt = null;
		this.passwordResetToken = null;
		this.passwordResetExpiresAt = null;
		this.deletedAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	public void suspendAccess() {
		this.status = "SUSPENDED";
		this.isActive = false;
		this.inviteToken = null;
		this.inviteExpiresAt = null;
		this.passwordResetToken = null;
		this.passwordResetExpiresAt = null;
		this.updatedAt = Instant.now();
	}

	public void reactivateAccess() {
		this.status = "ACTIVE";
		this.isActive = true;
		this.isLocked = false;
		this.failedLoginAttempts = 0;
		this.inviteToken = null;
		this.inviteExpiresAt = null;
		this.passwordResetToken = null;
		this.passwordResetExpiresAt = null;
		this.deletedAt = null;
		this.updatedAt = Instant.now();
	}

	public void reissueInvite(String inviteToken, Instant inviteExpiresAt) {
		this.status = "PENDING";
		this.isActive = false;
		this.isLocked = false;
		this.failedLoginAttempts = 0;
		this.deletedAt = null;
		this.inviteToken = inviteToken;
		this.inviteExpiresAt = inviteExpiresAt;
		this.updatedAt = Instant.now();
	}

	public void cancelInvite() {
		this.status = "DEACTIVATED";
		this.isActive = false;
		this.inviteToken = null;
		this.inviteExpiresAt = null;
		this.deletedAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	public void recordSuccessfulLogin() {
		this.failedLoginAttempts = 0;
		this.isLocked = false;
		this.lastLoginAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	public void recordFailedLoginAttempt() {
		this.failedLoginAttempts = this.failedLoginAttempts + 1;
		if (this.failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
			this.isLocked = true;
		}
		this.updatedAt = Instant.now();
	}

	private static String extractFirstName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			return "";
		}
		String trimmed = fullName.trim();
		int firstSpace = trimmed.indexOf(' ');
		return firstSpace < 0 ? trimmed : trimmed.substring(0, firstSpace);
	}

	private static String extractLastName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			return "";
		}
		String trimmed = fullName.trim();
		int firstSpace = trimmed.indexOf(' ');
		return firstSpace < 0 ? "" : trimmed.substring(firstSpace + 1).trim();
	}
}
