package com.rra.taxhandbook.security;

import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

	public String generateToken(String subject) {
		return Base64.getEncoder().encodeToString((subject + ":token").getBytes());
	}

	public String extractSubject(String token) {
		return new String(Base64.getDecoder().decode(token)).split(":")[0];
	}
}
