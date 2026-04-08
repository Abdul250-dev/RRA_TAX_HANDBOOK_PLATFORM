package com.rra.taxhandbook.user.dto;

public record UserResponse(
	Long id,
	String employeeId,
	String fullName,
	String email,
	String roleName
) {
}
