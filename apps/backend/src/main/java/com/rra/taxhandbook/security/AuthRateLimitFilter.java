package com.rra.taxhandbook.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

	private final Map<String, RateLimitWindow> requestWindows = new ConcurrentHashMap<>();

	private static final Set<String> RATE_LIMITED_PATHS = Set.of(
		"/api/auth/login",
		"/api/auth/forgot-password",
		"/api/auth/reset-password"
	);

	@Value("${app.security.rate-limit.enabled:true}")
	private boolean rateLimitEnabled;

	@Value("${app.security.rate-limit.max-requests:5}")
	private int maxRequests;

	@Value("${app.security.rate-limit.window-seconds:60}")
	private long windowSeconds;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (!shouldRateLimit(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String clientKey = buildClientKey(request);
		long now = Instant.now().getEpochSecond();

		RateLimitWindow currentWindow = requestWindows.compute(clientKey, (key, existing) -> {
			if (existing == null || existing.windowStartedAt() + windowSeconds <= now) {
				return new RateLimitWindow(now, 1);
			}
			return new RateLimitWindow(existing.windowStartedAt(), existing.requestCount() + 1);
		});

		if (currentWindow.requestCount() > maxRequests) {
			writeRateLimitResponse(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean shouldRateLimit(HttpServletRequest request) {
		return rateLimitEnabled
			&& "POST".equalsIgnoreCase(request.getMethod())
			&& RATE_LIMITED_PATHS.contains(request.getRequestURI());
	}

	private String buildClientKey(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		String clientIp = (forwardedFor == null || forwardedFor.isBlank())
			? request.getRemoteAddr()
			: forwardedFor.split(",")[0].trim();
		return request.getRequestURI() + "|" + clientIp;
	}

	private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("""
			{"status":429,"error":"Too Many Requests","message":"Too many authentication requests. Please try again later."}
			""");
	}

	private record RateLimitWindow(long windowStartedAt, int requestCount) {
	}
}
