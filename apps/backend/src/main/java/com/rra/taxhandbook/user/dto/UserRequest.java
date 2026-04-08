package com.rra.taxhandbook.user.dto;

import com.rra.taxhandbook.common.enums.UserRole;

public record UserRequest(
	String fullName,
	String email,
	UserRole role
) {
}
