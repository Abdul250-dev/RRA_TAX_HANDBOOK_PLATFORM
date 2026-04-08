package com.rra.taxhandbook.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rra.taxhandbook.auth.dto.ForgotPasswordRequest;
import com.rra.taxhandbook.auth.dto.LoginRequest;
import com.rra.taxhandbook.auth.dto.LoginResponse;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.security.JwtService;

@Service
public class AuthService {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	public AuthService(JwtService jwtService, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	public LoginResponse login(LoginRequest request) {
		String username = request.username() == null || request.username().isBlank() ? "admin" : request.username();
		String password = request.password() == null ? "" : request.password();

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new UnauthorizedException("Invalid username or password.");
		}

		String role = userDetails.getAuthorities().stream()
			.findFirst()
			.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
			.orElse("ADMIN");

		return new LoginResponse(username, jwtService.generateToken(username, role), role);
	}

	public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
		return new ApiResponse<>("Password reset instructions prepared", request.email());
	}
}
