package com.rra.taxhandbook.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.auth.dto.ForgotPasswordRequest;
import com.rra.taxhandbook.auth.dto.InvitePreviewResponse;
import com.rra.taxhandbook.auth.dto.LoginRequest;
import com.rra.taxhandbook.auth.dto.LoginResponse;
import com.rra.taxhandbook.auth.dto.PasswordResetResponse;
import com.rra.taxhandbook.auth.dto.ResetPasswordRequest;
import com.rra.taxhandbook.auth.service.AuthService;
import com.rra.taxhandbook.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/forgot-password")
	public ApiResponse<PasswordResetResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		return authService.forgotPassword(request);
	}

	@PostMapping("/reset-password")
	public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
		return authService.resetPassword(request);
	}

	@GetMapping("/invite-preview")
	public InvitePreviewResponse previewInviteToken(@RequestParam String token) {
		return authService.previewInviteToken(token);
	}
}
