package com.rra.taxhandbook.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
	"app.security.rate-limit.enabled=true",
	"app.security.rate-limit.max-requests=2",
	"app.security.rate-limit.window-seconds=60"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRateLimitIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginIsRateLimitedAfterTooManyRequests() throws Exception {
		String body = """
			{
			  "username": "unknown@rra.test",
			  "password": "wrong-password"
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType("application/json")
				.content(body))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/auth/login")
				.contentType("application/json")
				.content(body))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/auth/login")
				.contentType("application/json")
				.content(body))
			.andExpect(status().isTooManyRequests());
	}

	@Test
	void forgotPasswordIsRateLimitedAfterTooManyRequests() throws Exception {
		String body = """
			{
			  "email": "unknown@rra.test"
			}
			""";

		mockMvc.perform(post("/api/auth/forgot-password")
				.contentType("application/json")
				.content(body))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/forgot-password")
				.contentType("application/json")
				.content(body))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/forgot-password")
				.contentType("application/json")
				.content(body))
			.andExpect(status().isTooManyRequests());
	}
}
