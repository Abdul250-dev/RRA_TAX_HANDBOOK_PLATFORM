package com.rra.taxhandbook.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_directory_snapshot")
public class EmployeeDirectorySnapshot {

	@Id
	@Column(name = "employee_id", nullable = false, unique = true)
	private String employeeId;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private boolean active;

	protected EmployeeDirectorySnapshot() {
	}

	public EmployeeDirectorySnapshot(String employeeId, String fullName, String email, boolean active) {
		this.employeeId = employeeId;
		this.fullName = fullName;
		this.email = email;
		this.active = active;
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

	public boolean isActive() {
		return active;
	}
}
