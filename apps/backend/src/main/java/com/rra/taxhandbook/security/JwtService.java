package com.rra.taxhandbook.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

	@Value("${security.jwt.secret:change-this-jwt-secret-for-production}")
	private String jwtSecret;

	@Value("${security.jwt.expiration-seconds:3600}")
	private long expirationSeconds;

	public String generateToken(String subject, String role) {
		try {
			String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
			long issuedAt = Instant.now().getEpochSecond();
			long expiresAt = issuedAt + expirationSeconds;
			String payload = encode(
				"{\"sub\":\"" + escape(subject) + "\",\"role\":\"" + escape(role) + "\",\"iat\":" + issuedAt + ",\"exp\":" + expiresAt + "}"
			);
			String signature = sign(header + "." + payload);
			return header + "." + payload + "." + signature;
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to generate JWT token", ex);
		}
	}

	public String extractSubject(String token) {
		return extractClaim(token, "sub");
	}

	public String extractRole(String token) {
		return extractClaim(token, "role");
	}

	public boolean isTokenValid(String token, String username) {
		try {
			String subject = extractClaim(token, "sub");
			long exp = Long.parseLong(extractClaim(token, "exp"));
			return subject.equals(username) && exp > Instant.now().getEpochSecond() && hasValidSignature(token);
		}
		catch (Exception ex) {
			return false;
		}
	}

	private String extractClaim(String token, String claimName) {
		try {
			String payload = parsePayloadJson(token);
			Pattern quotedPattern = Pattern.compile("\"" + Pattern.quote(claimName) + "\"\\s*:\\s*\"([^\"]*)\"");
			Matcher quotedMatcher = quotedPattern.matcher(payload);
			if (quotedMatcher.find()) {
				return quotedMatcher.group(1);
			}

			Pattern numberPattern = Pattern.compile("\"" + Pattern.quote(claimName) + "\"\\s*:\\s*(\\d+)");
			Matcher numberMatcher = numberPattern.matcher(payload);
			if (numberMatcher.find()) {
				return numberMatcher.group(1);
			}

			throw new IllegalArgumentException("Claim not found: " + claimName);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Invalid JWT token", ex);
		}
	}

	private String parsePayloadJson(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("JWT token must have 3 parts.");
		}
		return new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
	}

	private boolean hasValidSignature(String token) throws Exception {
		String[] parts = token.split("\\.");
		String expected = sign(parts[0] + "." + parts[1]);
		return expected.equals(parts[2]);
	}

	private String encode(String value) {
		return URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private String sign(String content) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
