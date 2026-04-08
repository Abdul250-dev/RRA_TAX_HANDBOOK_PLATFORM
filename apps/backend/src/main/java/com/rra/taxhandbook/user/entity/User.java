package com.rra.taxhandbook.user.entity;

import java.time.Instant;

import com.rra.taxhandbook.role.entity.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	@Column(name = "employee_id", nullable = false, unique = true)
	private String employeeId;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	protected User() {
	}

	public User(String employeeId, String fullName, String email, String status, Instant createdAt, Role role) {
		this.employeeId = employeeId;
		this.fullName = fullName;
		this.email = email;
		this.status = status;
		this.createdAt = createdAt;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
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
