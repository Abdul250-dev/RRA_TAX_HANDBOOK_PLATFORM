package com.rra.taxhandbook.employee.dto;

public record EmployeeDirectoryResponse(
	String employeeId,
	String fullName,
	String email,
	boolean active
) {
}
