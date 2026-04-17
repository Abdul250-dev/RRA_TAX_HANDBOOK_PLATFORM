package com.rra.taxhandbook.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import com.rra.taxhandbook.notification.EmailDeliveryService;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedAdminMutationContractIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

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

	@MockitoBean
	private EmailDeliveryService emailDeliveryService;

	private Role adminRole;
	private Role editorRole;
	private Role reviewerRole;
	private Role publisherRole;

	@BeforeEach
	void setUp() {
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
		reviewerRole = roleRepository.save(new Role("REVIEWER", "Review role"));
		publisherRole = roleRepository.save(new Role("PUBLISHER", "Publisher role"));
	}

	@Test
	void adminCreateUserReturnsExpectedContract() throws Exception {
		mockMvc.perform(post("/api/users")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "employeeId": "EMP-MUT-001",
					  "firstName": "Grace",
					  "lastName": "Mutator",
					  "email": "grace.mutator@rra.test",
					  "roleName": "EDITOR",
					  "preferredLocale": "EN",
					  "phoneNumber": "+250788111111",
					  "department": "Taxpayer Services",
					  "position": "Officer",
					  "password": "StrongPass!123"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Local system user created successfully"))
			.andExpect(jsonPath("$.data.employeeId").value("EMP-MUT-001"))
			.andExpect(jsonPath("$.data.userCode").value("EMP-MUT-001"))
			.andExpect(jsonPath("$.data.username").value("graempmut001"))
			.andExpect(jsonPath("$.data.firstName").value("Grace"))
			.andExpect(jsonPath("$.data.lastName").value("Mutator"))
			.andExpect(jsonPath("$.data.fullName").value("Grace Mutator"))
			.andExpect(jsonPath("$.data.email").value("grace.mutator@rra.test"))
			.andExpect(jsonPath("$.data.department").value("Taxpayer Services"))
			.andExpect(jsonPath("$.data.position").value("Officer"))
			.andExpect(jsonPath("$.data.roleName").value("EDITOR"))
			.andExpect(jsonPath("$.data.status").value("ACTIVE"))
			.andExpect(jsonPath("$.data.isActive").value(true))
			.andExpect(jsonPath("$.data.isLocked").value(false))
			.andExpect(jsonPath("$.data.failedLoginAttempts").value(0));
	}

	@Test
	void adminCreateUserRejectsDuplicateEmail() throws Exception {
		userRepository.save(buildUser("EMP-DUP-001", "Existing", "User", "duplicate.user@rra.test", "duplicate.user", "ACTIVE", editorRole));

		mockMvc.perform(post("/api/users")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "employeeId": "EMP-DUP-002",
					  "firstName": "Another",
					  "lastName": "User",
					  "email": "duplicate.user@rra.test",
					  "roleName": "EDITOR",
					  "preferredLocale": "EN",
					  "password": "StrongPass!123"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("A system user already exists for email duplicate.user@rra.test"));
	}

	@Test
	void adminInviteUserReturnsExpectedContractWithoutSensitiveTokenFields() throws Exception {
		mockMvc.perform(post("/api/users/invite")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "employeeId": "EMP-INV-001",
					  "firstName": "Invite",
					  "lastName": "Only",
					  "email": "invite.only@rra.test",
					  "roleName": "EDITOR",
					  "preferredLocale": "EN",
					  "department": "Taxpayer Services",
					  "position": "Officer"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User invitation created successfully"))
			.andExpect(jsonPath("$.data.userId").isNumber())
			.andExpect(jsonPath("$.data.email").value("invite.only@rra.test"))
			.andExpect(jsonPath("$.data.inviteToken").doesNotExist())
			.andExpect(jsonPath("$.data.expiresAt").doesNotExist())
			.andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Test
	void adminUpdateUserRoleReturnsExpectedContract() throws Exception {
		User existingUser = userRepository.save(buildUser("EMP-ROLE-001", "Role", "Target", "role.target@rra.test", "role.target", "ACTIVE", editorRole));

		mockMvc.perform(post("/api/users/" + existingUser.getId() + "/role")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "roleName": "PUBLISHER"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User role updated successfully"))
			.andExpect(jsonPath("$.data.id").value(existingUser.getId()))
			.andExpect(jsonPath("$.data.email").value("role.target@rra.test"))
			.andExpect(jsonPath("$.data.roleName").value("PUBLISHER"))
			.andExpect(jsonPath("$.data.status").value("ACTIVE"));
	}

	@Test
	void adminUpdateUserRoleRejectsInvalidTargetRole() throws Exception {
		User existingUser = userRepository.save(buildUser("EMP-ROLE-002", "Role", "Public", "role.public@rra.test", "role.public", "ACTIVE", editorRole));
		roleRepository.save(new Role("PUBLIC", "Public role"));

		mockMvc.perform(post("/api/users/" + existingUser.getId() + "/role")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "roleName": "PUBLIC"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("PUBLIC cannot be assigned to a local authenticated system user."));
	}

	@Test
	void adminTopicWorkflowMutationReturnsExpectedContract() throws Exception {
		var ids = seedDraftTopic("workflow-contract-topic");

		mockMvc.perform(post("/api/admin/content/topics/" + ids.topicId() + "/workflow")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "SUBMIT_FOR_REVIEW"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic workflow updated"))
			.andExpect(jsonPath("$.data.topicId").value(ids.topicId()))
			.andExpect(jsonPath("$.data.title").value("Workflow Contract Topic"))
			.andExpect(jsonPath("$.data.slug").value("workflow-contract-topic"))
			.andExpect(jsonPath("$.data.status").value("REVIEW"))
			.andExpect(jsonPath("$.data.action").value("SUBMIT_FOR_REVIEW"))
			.andExpect(jsonPath("$.data.performedBy").value("editor@rra.test"))
			.andExpect(jsonPath("$.data.scheduledPublishAt").doesNotExist());
	}

	@Test
	void adminTopicWorkflowMutationRejectsUnsupportedAction() throws Exception {
		var ids = seedDraftTopic("workflow-invalid-action-topic");

		mockMvc.perform(post("/api/admin/content/topics/" + ids.topicId() + "/workflow")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "MAKE_MAGIC"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Unsupported workflow action: MAKE_MAGIC"));
	}

	@Test
	void scheduledPublishProcessingReturnsExpectedContract() throws Exception {
		var ids = seedDraftTopic("scheduled-process-topic");

		mockMvc.perform(post("/api/admin/content/topics/" + ids.topicId() + "/workflow")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "SUBMIT_FOR_REVIEW"
					}
					"""))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/admin/content/topics/" + ids.topicId() + "/workflow")
				.with(user("reviewer@rra.test").roles("REVIEWER"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "APPROVE"
					}
					"""))
			.andExpect(status().isOk());

		String scheduledAt = Instant.now().plusSeconds(120).toString();
		mockMvc.perform(post("/api/admin/content/topics/" + ids.topicId() + "/workflow")
				.with(user("publisher@rra.test").roles("PUBLISHER"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "SCHEDULE_PUBLISH",
					  "scheduledAt": "%s"
					}
					""".formatted(scheduledAt)))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/admin/content/topics/workflow/process-scheduled")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Scheduled publish processing completed"))
			.andExpect(jsonPath("$.data.processedCount").value(0));
	}

	private SeededTopicIds seedDraftTopic(String slug) {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Workflow Contract Section",
				"workflow-contract-section-" + slug,
				"Section for workflow mutation contract tests"
			),
			"admin@rra.test"
		);

		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Workflow Contract Topic",
				slug,
				"Workflow summary",
				"Workflow intro"
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
				"Workflow body"
			),
			"admin@rra.test"
		);

		return new SeededTopicIds(sectionResponse.data().id(), topicResponse.data().id());
	}

	private User buildUser(
		String employeeId,
		String firstName,
		String lastName,
		String email,
		String username,
		String status,
		Role role
	) {
		return new User(
			employeeId,
			firstName,
			lastName,
			email,
			username,
			passwordEncoder.encode("StrongPass!123"),
			LanguageCode.EN,
			UserSource.LOCAL,
			status,
			"ACTIVE".equalsIgnoreCase(status),
			"SUSPENDED".equalsIgnoreCase(status),
			0,
			null,
			null,
			null,
			null,
			null,
			"+250788111111",
			"Taxpayer Services",
			"Officer",
			Instant.now(),
			null,
			"DEACTIVATED".equalsIgnoreCase(status) ? Instant.now() : null,
			null,
			null,
			role
		);
	}

	private record SeededTopicIds(Long sectionId, Long topicId) {
	}
}
