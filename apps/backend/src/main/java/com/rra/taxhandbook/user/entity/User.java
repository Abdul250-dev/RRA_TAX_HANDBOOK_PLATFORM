package com.rra.taxhandbook.user.entity;

import com.rra.taxhandbook.common.enums.UserRole;

public class User {

	private Long id;
	private String fullName;
	private String email;
	private String password;
	private UserRole role;

	public User(Long id, String fullName, String email, String password, UserRole role) {
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public UserRole getRole() {
		return role;
	}
}
