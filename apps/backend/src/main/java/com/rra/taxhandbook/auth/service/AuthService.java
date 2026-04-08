package com.rra.taxhandbook.auth.service;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.auth.dto.ForgotPasswordRequest;
import com.rra.taxhandbook.auth.dto.LoginRequest;
import com.rra.taxhandbook.auth.dto.LoginResponse;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.security.JwtService;

@Service
public class AuthService {

	private final JwtService jwtService;

	public AuthService(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	public LoginResponse login(LoginRequest request) {
		String username = request.username() == null || request.username().isBlank() ? "admin" : request.username();
		return new LoginResponse(username, jwtService.generateToken(username), "ADMIN");
	}

	public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
		return new ApiResponse<>("Password reset instructions prepared", request.email());
	}
}
