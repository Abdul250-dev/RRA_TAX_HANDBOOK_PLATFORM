package com.rra.taxhandbook.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndpointAuthorizationIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void anonymousUserCannotCreateCategory() throws Exception {
		mockMvc.perform(post("/api/admin/categories")
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "name": "Income Tax",
					  "language": "EN"
					}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void editorCannotCreateCategory() throws Exception {
		mockMvc.perform(post("/api/admin/categories")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "name": "Income Tax",
					  "language": "EN"
					}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanCreateCategory() throws Exception {
		mockMvc.perform(post("/api/admin/categories")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "name": "Income Tax",
					  "language": "EN"
					}
					"""))
			.andExpect(status().isOk());
	}

	@Test
	void editorCanCreateArticle() throws Exception {
		mockMvc.perform(post("/api/admin/articles")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "title": "VAT Basics",
					  "summary": "Starter guide",
					  "content": "Article body",
					  "language": "EN",
					  "status": "DRAFT"
					}
					"""))
			.andExpect(status().isOk());
	}

	@Test
	void reviewerCannotCreateArticle() throws Exception {
		mockMvc.perform(post("/api/admin/articles")
				.with(user("reviewer@rra.test").roles("REVIEWER"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "title": "VAT Basics",
					  "summary": "Starter guide",
					  "content": "Article body",
					  "language": "EN",
					  "status": "DRAFT"
					}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void reviewerCanAccessContentSummary() throws Exception {
		mockMvc.perform(get("/api/admin/content/summary")
				.with(user("reviewer@rra.test").roles("REVIEWER")))
			.andExpect(status().isOk());
	}

	@Test
	void contentOfficerCanAccessContentSummary() throws Exception {
		mockMvc.perform(get("/api/admin/content/summary")
				.with(user("content.officer@rra.test").roles("CONTENT_OFFICER")))
			.andExpect(status().isOk());
	}

	@Test
	void viewerCanAccessContentSummary() throws Exception {
		mockMvc.perform(get("/api/admin/content/summary")
				.with(user("viewer@rra.test").roles("VIEWER")))
			.andExpect(status().isOk());
	}

	@Test
	void auditorCanAccessContentSummary() throws Exception {
		mockMvc.perform(get("/api/admin/content/summary")
				.with(user("auditor@rra.test").roles("AUDITOR")))
			.andExpect(status().isOk());
	}

	@Test
	void contentOfficerCanCreateSection() throws Exception {
		mockMvc.perform(post("/api/admin/content/sections")
				.with(user("content.officer@rra.test").roles("CONTENT_OFFICER"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "type": "MAIN",
					  "sortOrder": 77,
					  "locale": "EN",
					  "name": "Content Officer Section",
					  "slug": "content-officer-section",
					  "summary": "Section created by a content officer."
					}
					"""))
			.andExpect(status().isOk());
	}

	@Test
	void viewerCannotCreateSection() throws Exception {
		mockMvc.perform(post("/api/admin/content/sections")
				.with(user("viewer@rra.test").roles("VIEWER"))
				.with(csrf())
				.contentType("application/json")
				.content("""
					{
					  "type": "MAIN",
					  "sortOrder": 78,
					  "locale": "EN",
					  "name": "Viewer Section",
					  "slug": "viewer-section",
					  "summary": "Viewer should not be able to create this."
					}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void publisherCannotAccessReviewQueue() throws Exception {
		mockMvc.perform(get("/api/admin/content/topics/review-queue")
				.with(user("publisher@rra.test").roles("PUBLISHER")))
			.andExpect(status().isForbidden());
	}

	@Test
	void reviewerCanAccessReviewQueue() throws Exception {
		mockMvc.perform(get("/api/admin/content/topics/review-queue")
				.with(user("reviewer@rra.test").roles("REVIEWER")))
			.andExpect(status().isOk());
	}

	@Test
	void viewerCanReadQueuesButCannotPublish() throws Exception {
		mockMvc.perform(get("/api/admin/content/topics/publish-queue")
				.with(user("viewer@rra.test").roles("VIEWER")))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/admin/content/topics/workflow/process-scheduled")
				.with(user("viewer@rra.test").roles("VIEWER"))
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanAccessAuditLogs() throws Exception {
		mockMvc.perform(get("/api/audit-logs")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isOk());
	}

	@Test
	void auditorCanAccessAuditLogs() throws Exception {
		mockMvc.perform(get("/api/audit-logs")
				.with(user("auditor@rra.test").roles("AUDITOR")))
			.andExpect(status().isOk());
	}

	@Test
	void editorCannotAccessAuditLogs() throws Exception {
		mockMvc.perform(get("/api/audit-logs")
				.with(user("editor@rra.test").roles("EDITOR")))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanAccessUserSummary() throws Exception {
		mockMvc.perform(get("/api/users/summary")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isOk());
	}

	@Test
	void auditorCannotAccessUserSummary() throws Exception {
		mockMvc.perform(get("/api/users/summary")
				.with(user("auditor@rra.test").roles("AUDITOR")))
			.andExpect(status().isForbidden());
	}

	@Test
	void publicEndpointRemainsOpen() throws Exception {
		mockMvc.perform(get("/api/health"))
			.andExpect(status().isOk());
	}

	@Test
	void adminCanUseDeactivateAlias() throws Exception {
		mockMvc.perform(post("/api/users/1/deactivate")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}

	@Test
	void editorCannotUseDeactivateAlias() throws Exception {
		mockMvc.perform(post("/api/users/1/deactivate")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanUseRemoveAlias() throws Exception {
		mockMvc.perform(post("/api/users/1/remove")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk());
	}

	@Test
	void auditorCannotUseRemoveAlias() throws Exception {
		mockMvc.perform(post("/api/users/1/remove")
				.with(user("auditor@rra.test").roles("AUDITOR"))
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanUseCancelAlias() throws Exception {
		mockMvc.perform(post("/api/users/1/cancel")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}

	@Test
	void adminCanUseResendAlias() throws Exception {
		mockMvc.perform(post("/api/users/1/resend")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}
}
