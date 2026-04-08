package com.rra.taxhandbook.user.dto;

public record UserRequest(
	String employeeId,
	String fullName,
	String email,
	String roleName
) {
}
