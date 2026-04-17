package com.rra.taxhandbook.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.rra.taxhandbook.audit.entity.AuditLog;
import com.rra.taxhandbook.audit.repository.AuditLogRepository;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.section.entity.SectionType;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.service.ContentStructureService;
import com.rra.taxhandbook.content.topic.entity.TopicType;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockType;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedAdminApiContractIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private ContentStructureService contentStructureService;

	@Autowired
	private TopicBlockTranslationRepository topicBlockTranslationRepository;

	@Autowired
	private TopicBlockRepository topicBlockRepository;

	@Autowired
	private TopicTranslationRepository topicTranslationRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private SectionTranslationRepository sectionTranslationRepository;

	@Autowired
	private SectionRepository sectionRepository;

	private Role adminRole;
	private Role editorRole;

	@BeforeEach
	void setUp() {
		auditLogRepository.deleteAll();
		userRepository.deleteAll();
		topicBlockTranslationRepository.deleteAll();
		topicBlockRepository.deleteAll();
		topicTranslationRepository.deleteAll();
		topicRepository.deleteAll();
		sectionTranslationRepository.deleteAll();
		sectionRepository.deleteAll();
		roleRepository.deleteAll();

		adminRole = roleRepository.save(new Role("ADMIN", "Administrative role"));
		editorRole = roleRepository.save(new Role("EDITOR", "Editorial role"));
	}

	@Test
	void adminUserSummaryEndpointReturnsExpectedContract() throws Exception {
		userRepository.save(buildUser("EMP-ACTIVE-1", "Alice", "Active", "alice.active@rra.test", "alice.active", "ACTIVE", true, false, 0, adminRole));
		userRepository.save(buildUser("EMP-PENDING-1", "Peter", "Pending", "peter.pending@rra.test", "peter.pending", "PENDING", false, false, 0, editorRole));
		userRepository.save(buildUser("EMP-SUSP-1", "Susan", "Suspended", "susan.suspended@rra.test", "susan.suspended", "SUSPENDED", false, true, 3, editorRole));
		userRepository.save(buildUser("EMP-DEACT-1", "Diane", "Deactivated", "diane.deactivated@rra.test", "diane.deactivated", "DEACTIVATED", false, false, 0, editorRole));

		mockMvc.perform(get("/api/users/summary")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalUsers").value(4))
			.andExpect(jsonPath("$.activeUsers").value(1))
			.andExpect(jsonPath("$.pendingUsers").value(1))
			.andExpect(jsonPath("$.suspendedUsers").value(1))
			.andExpect(jsonPath("$.deactivatedUsers").value(1));
	}

	@Test
	void adminUsersEndpointReturnsFilteredUserContract() throws Exception {
		userRepository.save(buildUser("EMP-ACTIVE-2", "Alice", "Filtered", "alice.filtered@rra.test", "alice.filtered", "ACTIVE", true, false, 0, editorRole));
		userRepository.save(buildUser("EMP-SUSP-2", "Bob", "Blocked", "bob.blocked@rra.test", "bob.blocked", "SUSPENDED", false, true, 2, editorRole));

		mockMvc.perform(get("/api/users")
				.with(user("admin@rra.test").roles("ADMIN"))
				.queryParam("status", "ACTIVE")
				.queryParam("search", "alice"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].employeeId").value("EMP-ACTIVE-2"))
			.andExpect(jsonPath("$[0].userCode").value("EMP-ACTIVE-2"))
			.andExpect(jsonPath("$[0].username").value("alice.filtered"))
			.andExpect(jsonPath("$[0].firstName").value("Alice"))
			.andExpect(jsonPath("$[0].lastName").value("Filtered"))
			.andExpect(jsonPath("$[0].fullName").value("Alice Filtered"))
			.andExpect(jsonPath("$[0].email").value("alice.filtered@rra.test"))
			.andExpect(jsonPath("$[0].department").value("Taxpayer Services"))
			.andExpect(jsonPath("$[0].position").value("Officer"))
			.andExpect(jsonPath("$[0].roleName").value("EDITOR"))
			.andExpect(jsonPath("$[0].preferredLocale").value("EN"))
			.andExpect(jsonPath("$[0].source").value("LOCAL"))
			.andExpect(jsonPath("$[0].status").value("ACTIVE"))
			.andExpect(jsonPath("$[0].isActive").value(true))
			.andExpect(jsonPath("$[0].isLocked").value(false))
			.andExpect(jsonPath("$[0].failedLoginAttempts").value(0));
	}

	@Test
	void adminPendingInvitesEndpointReturnsGeneratedUsername() throws Exception {
		userRepository.save(buildUser("EMP-PEND-USER-1", "Pending", "Invite", "pending.invite@rra.test", "penemppenduser1", "PENDING", false, false, 0, editorRole));

		mockMvc.perform(get("/api/users/invited")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].employeeId").value("EMP-PEND-USER-1"))
			.andExpect(jsonPath("$[0].username").value("penemppenduser1"))
			.andExpect(jsonPath("$[0].fullName").value("Pending Invite"))
			.andExpect(jsonPath("$[0].email").value("pending.invite@rra.test"))
			.andExpect(jsonPath("$[0].status").value("PENDING"));
	}

	@Test
	void adminUserDetailEndpointReturnsNotFoundForMissingUser() throws Exception {
		mockMvc.perform(get("/api/users/999999")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("User not found: 999999"));
	}

	@Test
	void adminCreateUserRejectsInvalidLocale() throws Exception {
		mockMvc.perform(post("/api/users")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "employeeId": "EMP-NEW-1",
					  "firstName": "New",
					  "lastName": "User",
					  "email": "new.user@rra.test",
					  "roleName": "EDITOR",
					  "preferredLocale": "DE",
					  "department": "Taxpayer Services",
					  "position": "Officer",
					  "password": "StrongPass!123"
					}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void adminContentSummaryEndpointReturnsExpectedContract() throws Exception {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Admin Content Section",
				"admin-content-section",
				"Section for admin contract tests"
			),
			"admin@rra.test"
		);

		contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Admin Content Topic",
				"admin-content-topic",
				"Admin topic summary",
				"Admin topic intro"
			),
			"admin@rra.test"
		);

		mockMvc.perform(get("/api/admin/content/summary")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalTopics").value(1))
			.andExpect(jsonPath("$.draftTopics").value(1))
			.andExpect(jsonPath("$.reviewTopics").value(0))
			.andExpect(jsonPath("$.approvedTopics").value(0))
			.andExpect(jsonPath("$.publishedTopics").value(0))
			.andExpect(jsonPath("$.archivedTopics").value(0))
			.andExpect(jsonPath("$.totalSections").value(1))
			.andExpect(jsonPath("$.draftSections").value(1))
			.andExpect(jsonPath("$.publishedSections").value(0))
			.andExpect(jsonPath("$.archivedSections").value(0));
	}

	@Test
	void adminTopicDetailEndpointReturnsExpectedContract() throws Exception {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				2,
				LanguageCode.EN,
				"Topic Detail Section",
				"topic-detail-section",
				"Section for topic detail contract tests"
			),
			"admin@rra.test"
		);

		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Topic Detail Topic",
				"topic-detail-topic",
				"Topic detail summary",
				"Topic detail intro"
			),
			"admin@rra.test"
		);

		contentStructureService.createTopicBlock(
			topicResponse.data().id(),
			new AdminCreateTopicBlockRequest(
				TopicBlockType.RICH_TEXT,
				1,
				"overview",
				LanguageCode.EN,
				"Overview",
				"Admin block body"
			),
			"admin@rra.test"
		);

		mockMvc.perform(get("/api/admin/content/topics/{topicId}", topicResponse.data().id())
				.with(user("admin@rra.test").roles("ADMIN"))
				.queryParam("locale", "EN"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(topicResponse.data().id()))
			.andExpect(jsonPath("$.sectionId").value(sectionResponse.data().id()))
			.andExpect(jsonPath("$.title").value("Topic Detail Topic"))
			.andExpect(jsonPath("$.slug").value("topic-detail-topic"))
			.andExpect(jsonPath("$.summary").value("Topic detail summary"))
			.andExpect(jsonPath("$.introText").value("Topic detail intro"))
			.andExpect(jsonPath("$.topicType").value("TAX_TOPIC"))
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andExpect(jsonPath("$.scheduledPublishAt").doesNotExist())
			.andExpect(jsonPath("$.blocks.length()").value(1))
			.andExpect(jsonPath("$.blocks[0].title").value("Overview"))
			.andExpect(jsonPath("$.blocks[0].body").value("Admin block body"))
			.andExpect(jsonPath("$.blocks[0].blockType").value("RICH_TEXT"))
			.andExpect(jsonPath("$.blocks[0].anchorKey").value("overview"));
	}

	@Test
	void adminContentEndpointsRejectInvalidLocale() throws Exception {
		mockMvc.perform(get("/api/admin/content/sections")
				.with(user("admin@rra.test").roles("ADMIN"))
				.queryParam("locale", "DE"))
			.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/admin/content/topics")
				.with(user("admin@rra.test").roles("ADMIN"))
				.queryParam("locale", "DE"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void auditLogsEndpointReturnsExpectedContract() throws Exception {
		auditLogRepository.save(new AuditLog(
			"USER_CREATED",
			"admin@rra.test",
			"new.user@rra.test",
			"Local system user created",
			Instant.parse("2026-04-16T10:15:30Z")
		));

		mockMvc.perform(get("/api/audit-logs")
				.with(user("admin@rra.test").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].action").value("USER_CREATED"))
			.andExpect(jsonPath("$[0].actor").value("admin@rra.test"))
			.andExpect(jsonPath("$[0].targetEmail").value("new.user@rra.test"))
			.andExpect(jsonPath("$[0].details").value("Local system user created"))
			.andExpect(jsonPath("$[0].createdAt").value("2026-04-16T10:15:30Z"));
	}

	private User buildUser(
		String employeeId,
		String firstName,
		String lastName,
		String email,
		String username,
		String status,
		boolean isActive,
		boolean isLocked,
		int failedLoginAttempts,
		Role role
	) {
		return new User(
			employeeId,
			firstName,
			lastName,
			email,
			username,
			"StrongPass!123",
			LanguageCode.EN,
			UserSource.LOCAL,
			status,
			isActive,
			isLocked,
			failedLoginAttempts,
			null,
			null,
			null,
			null,
			null,
			"+250700000000",
			"Taxpayer Services",
			"Officer",
			Instant.now(),
			null,
			"DEACTIVATED".equals(status) ? Instant.now() : null,
			null,
			null,
			role
		);
	}
}
