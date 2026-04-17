package com.rra.taxhandbook.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
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
import com.rra.taxhandbook.content.workflow.TopicWorkflowService;
import com.rra.taxhandbook.notification.EmailDeliveryService;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedAdminEdgeMutationIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ContentStructureService contentStructureService;

	@Autowired
	private TopicWorkflowService topicWorkflowService;

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

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private EmailDeliveryService emailDeliveryService;

	private Role adminRole;
	private Role editorRole;

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
	}

	@Test
	void deleteTopicReturnsExpectedContractForDraftTopic() throws Exception {
		var ids = seedDraftTopicWithBlock("delete-draft-topic");

		mockMvc.perform(delete("/api/admin/content/topics/" + ids.topicId())
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic deleted"))
			.andExpect(jsonPath("$.data").value(String.valueOf(ids.topicId())));
	}

	@Test
	void deleteTopicRejectsPublishedTopic() throws Exception {
		var ids = seedDraftTopicWithBlock("delete-published-topic");
		transitionTopicToPublished(ids.topicId());

		mockMvc.perform(delete("/api/admin/content/topics/" + ids.topicId())
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Only draft or archived topics can be deleted."));
	}

	@Test
	void deleteTopicBlockReturnsExpectedContractForDraftBlock() throws Exception {
		var ids = seedDraftTopicWithBlock("delete-draft-block-topic");

		mockMvc.perform(delete("/api/admin/content/blocks/" + ids.blockId())
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic block deleted"))
			.andExpect(jsonPath("$.data").value(String.valueOf(ids.blockId())));
	}

	@Test
	void sectionWorkflowPublishReturnsExpectedContract() throws Exception {
		var ids = seedDraftTopicWithBlock("section-publish-topic");
		transitionTopicToPublished(ids.topicId());

		mockMvc.perform(post("/api/admin/content/sections/" + ids.sectionId() + "/workflow")
				.with(user("publisher@rra.test").roles("PUBLISHER"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "PUBLISH"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Section workflow updated"))
			.andExpect(jsonPath("$.data.id").value(ids.sectionId()))
			.andExpect(jsonPath("$.data.slug").value("section-publish-topic-section"))
			.andExpect(jsonPath("$.data.status").value("PUBLISHED"));
	}

	@Test
	void sectionWorkflowRejectsUnsupportedAction() throws Exception {
		var ids = seedDraftTopicWithBlock("section-invalid-action-topic");

		mockMvc.perform(post("/api/admin/content/sections/" + ids.sectionId() + "/workflow")
				.with(user("publisher@rra.test").roles("PUBLISHER"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "action": "UNPUBLISH"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Unsupported section workflow action: UNPUBLISH"));
	}

	@Test
	void updateUserProfileReturnsExpectedContract() throws Exception {
		User existingUser = userRepository.save(buildUser(
			"EMP-PROFILE-001",
			"Grace",
			"Original",
			"grace.original@rra.test",
			"grace.original",
			"ACTIVE",
			editorRole
		));

		mockMvc.perform(post("/api/users/" + existingUser.getId() + "/profile")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "employeeId": "EMP-PROFILE-UPDATED",
					  "firstName": "Grace",
					  "lastName": "Updated",
					  "email": "grace.updated@rra.test",
					  "phoneNumber": "+250788222222",
					  "department": "Legal",
					  "position": "Senior Officer",
					  "preferredLocale": "FR"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User profile updated successfully"))
			.andExpect(jsonPath("$.data.employeeId").value("EMP-PROFILE-UPDATED"))
			.andExpect(jsonPath("$.data.username").value("graempprofileupdated"))
			.andExpect(jsonPath("$.data.fullName").value("Grace Updated"))
			.andExpect(jsonPath("$.data.email").value("grace.updated@rra.test"))
			.andExpect(jsonPath("$.data.department").value("Legal"))
			.andExpect(jsonPath("$.data.position").value("Senior Officer"))
			.andExpect(jsonPath("$.data.preferredLocale").value("FR"));
	}

	@Test
	void updateUserProfileRejectsDuplicateGeneratedUsername() throws Exception {
		userRepository.save(buildUser(
			"EMP001",
			"Target",
			"User",
			"other.user@rra.test",
			"taremp001",
			"ACTIVE",
			editorRole
		));
		User targetUser = userRepository.save(buildUser(
			"EMP-PROFILE-TARGET",
			"Target",
			"User",
			"target.user@rra.test",
			"target.user",
			"ACTIVE",
			editorRole
		));

		mockMvc.perform(post("/api/users/" + targetUser.getId() + "/profile")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "employeeId": "EMP-001",
					  "firstName": "Target",
					  "lastName": "User",
					  "email": "target.user@rra.test",
					  "phoneNumber": "+250788333333",
					  "department": "Legal",
					  "position": "Officer",
					  "preferredLocale": "EN"
					}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("A system user already exists for username taremp001"));
	}

	@Test
	void reactivateUserReturnsExpectedContract() throws Exception {
		User suspendedUser = userRepository.save(buildUser(
			"EMP-REACTIVATE-001",
			"Reactivate",
			"User",
			"reactivate.user@rra.test",
			"reactivate.user",
			"SUSPENDED",
			editorRole
		));

		mockMvc.perform(post("/api/users/" + suspendedUser.getId() + "/reactivate")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User reactivated successfully"))
			.andExpect(jsonPath("$.data.id").value(suspendedUser.getId()))
			.andExpect(jsonPath("$.data.status").value("ACTIVE"))
			.andExpect(jsonPath("$.data.isActive").value(true))
			.andExpect(jsonPath("$.data.isLocked").value(false))
			.andExpect(jsonPath("$.data.failedLoginAttempts").value(0));
	}

	@Test
	void restoreUserReturnsExpectedContractWithoutSensitiveTokenFields() throws Exception {
		User deactivatedUser = userRepository.save(buildUser(
			"EMP-RESTORE-001",
			"Restore",
			"User",
			"restore.user@rra.test",
			"restore.user",
			"DEACTIVATED",
			editorRole
		));

		mockMvc.perform(post("/api/users/" + deactivatedUser.getId() + "/restore")
				.with(user("admin@rra.test").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User restored and re-invited successfully"))
			.andExpect(jsonPath("$.data.userId").value(deactivatedUser.getId()))
			.andExpect(jsonPath("$.data.email").value("restore.user@rra.test"))
			.andExpect(jsonPath("$.data.inviteToken").doesNotExist())
			.andExpect(jsonPath("$.data.expiresAt").doesNotExist())
			.andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	private SeededIds seedDraftTopicWithBlock(String slugPrefix) {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Section " + slugPrefix,
				slugPrefix + "-section",
				"Section summary"
			),
			"admin@rra.test"
		);
		var topic = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				section.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Topic " + slugPrefix,
				slugPrefix,
				"Topic summary",
				"Topic intro"
			),
			"admin@rra.test"
		);
		var block = contentStructureService.createTopicBlock(
			topic.data().id(),
			new AdminCreateTopicBlockRequest(
				TopicBlockType.RICH_TEXT,
				1,
				"overview",
				LanguageCode.EN,
				"Overview",
				"Block body"
			),
			"admin@rra.test"
		);
		return new SeededIds(section.data().id(), topic.data().id(), block.data().id());
	}

	private void transitionTopicToPublished(Long topicId) {
		topicWorkflowService.transitionTopic(topicId, new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"), TestAuth.authentication("editor@rra.test", "EDITOR"));
		topicWorkflowService.transitionTopic(topicId, new TopicWorkflowActionRequest("APPROVE"), TestAuth.authentication("reviewer@rra.test", "REVIEWER"));
		topicWorkflowService.transitionTopic(topicId, new TopicWorkflowActionRequest("PUBLISH"), TestAuth.authentication("publisher@rra.test", "PUBLISHER"));
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
		boolean active = "ACTIVE".equalsIgnoreCase(status);
		boolean locked = "SUSPENDED".equalsIgnoreCase(status);
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
			active,
			locked,
			locked ? 2 : 0,
			null,
			null,
			null,
			null,
			null,
			"+250788444444",
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

	private record SeededIds(Long sectionId, Long topicId, Long blockId) {
	}

	private static final class TestAuth {
		private TestAuth() {
		}

		private static org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication(
			String username,
			String role
		) {
			return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				username,
				"N/A",
				java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
			);
		}
	}
}
