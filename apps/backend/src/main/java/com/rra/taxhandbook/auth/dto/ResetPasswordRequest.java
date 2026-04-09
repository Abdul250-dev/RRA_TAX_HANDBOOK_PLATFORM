package com.rra.taxhandbook.auth.dto;

public record ResetPasswordRequest(
	String token,
	String newPassword
) {
}
