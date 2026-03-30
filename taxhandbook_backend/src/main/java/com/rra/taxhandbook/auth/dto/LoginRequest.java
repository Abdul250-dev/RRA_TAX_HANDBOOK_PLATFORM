package com.rra.taxhandbook.auth.dto;

public record LoginRequest(
	String username,
	String password
) {
}
