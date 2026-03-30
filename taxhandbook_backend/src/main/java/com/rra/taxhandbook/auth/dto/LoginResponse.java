package com.rra.taxhandbook.auth.dto;

public record LoginResponse(
	String username,
	String token,
	String role
) {
}
