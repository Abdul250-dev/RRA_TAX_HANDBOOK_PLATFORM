package com.rra.taxhandbook.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import com.rra.taxhandbook.audit.entity.AuditLog;
import com.rra.taxhandbook.audit.repository.AuditLogRepository;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.SectionWorkflowActionRequest;
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
import com.rra.taxhandbook.role.dto.RoleRequest;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.role.service.RoleService;
import com.rra.taxhandbook.user.dto.AdminSetPasswordUserRequest;
import com.rra.taxhandbook.user.dto.UpdateUserProfileRequest;
import com.rra.taxhandbook.user.dto.UpdateUserRoleRequest;
import com.rra.taxhandbook.user.repository.UserRepository;
import com.rra.taxhandbook.user.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
class AuditCoverageIntegrationTests {

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private RoleService roleService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

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

	private Role adminRole;
	private Role editorRole;
	private Role publisherRole;

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
		publisherRole = roleRepository.save(new Role("PUBLISHER", "Publishing role"));
	}

	@Test
	void createRoleWritesAuditLog() {
		roleService.createRole(
			new RoleRequest("Content Steward", "Supports editorial operations"),
			"admin@rra.test"
		);

		AuditLog log = findAuditLog("ROLE_CREATED", "CONTENT_STEWARD");
		assertEquals("admin@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("Role created"));
	}

	@Test
	void createSectionWritesAuditLog() {
		contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Income Tax",
				"income-tax",
				"Income tax guidance"
			),
			"admin@rra.test"
		);

		AuditLog log = findAuditLog("CONTENT_SECTION_CREATED", "income-tax");
		assertEquals("admin@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("Section created"));
	}

	@Test
	void topicWorkflowTransitionWritesAuditLog() {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"VAT",
				"vat",
				"VAT guidance"
			),
			"admin@rra.test"
		);

		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"VAT Basics",
				"vat-basics",
				"VAT overview",
				"Intro"
			),
			"admin@rra.test"
		);

		var authentication = new UsernamePasswordAuthenticationToken(
			"editor@rra.test",
			"N/A",
			List.of(new SimpleGrantedAuthority("ROLE_EDITOR"))
		);

		var response = topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication
		);

		assertEquals("SUBMIT_FOR_REVIEW", response.data().action());
		assertEquals("REVIEW", response.data().status());

		AuditLog log = findAuditLog("CONTENT_TOPIC_WORKFLOW_UPDATED", "vat-basics");
		assertEquals("editor@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("SUBMIT_FOR_REVIEW"));
	}

	@Test
	void updateUserProfileWritesAuditLog() {
		var createdUser = userService.createUserWithPassword(
			new AdminSetPasswordUserRequest(
				"EMP-AUDIT-001",
				"Grace",
				"Audit",
				"grace.audit@rra.test",
				"grace.audit",
				editorRole.getName(),
				LanguageCode.EN,
				"+250788111111",
				"Taxpayer Services",
				"Officer",
				"StrongPass!123"
			),
			"admin@rra.test"
		);

		userService.updateUserProfile(
			createdUser.data().id(),
			new UpdateUserProfileRequest(
				"EMP-AUDIT-001X",
				"Grace",
				"Audited",
				"grace.audited@rra.test",
				"grace.audited",
				"+250788222222",
				"Legal",
				"Senior Officer",
				LanguageCode.FR
			),
			"admin@rra.test"
		);

		AuditLog log = findAuditLog("USER_PROFILE_UPDATED", "grace.audited@rra.test");
		assertEquals("admin@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("profile details updated"));
	}

	@Test
	void updateUserRoleWritesAuditLog() {
		var createdUser = userService.createUserWithPassword(
			new AdminSetPasswordUserRequest(
				"EMP-AUDIT-002",
				"Role",
				"Audit",
				"role.audit@rra.test",
				"role.audit",
				editorRole.getName(),
				LanguageCode.EN,
				null,
				"Taxpayer Services",
				"Officer",
				"StrongPass!123"
			),
			"admin@rra.test"
		);

		userService.updateUserRole(
			createdUser.data().id(),
			new UpdateUserRoleRequest(publisherRole.getName()),
			"admin@rra.test"
		);

		AuditLog log = findAuditLog("USER_ROLE_UPDATED", "role.audit@rra.test");
		assertEquals("admin@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("Role updated to PUBLISHER"));
	}

	@Test
	void sectionWorkflowTransitionWritesAuditLog() {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Tax Procedures",
				"tax-procedures",
				"Tax procedures guidance"
			),
			"admin@rra.test"
		);

		contentStructureService.transitionSection(
			sectionResponse.data().id(),
			new SectionWorkflowActionRequest("ARCHIVE"),
			"publisher@rra.test"
		);

		AuditLog log = findAuditLog("CONTENT_SECTION_WORKFLOW_UPDATED", "tax-procedures");
		assertEquals("publisher@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("ARCHIVE"));
	}

	@Test
	void scheduledPublishExecutionWritesAuditLog() {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Scheduled Section",
				"scheduled-section",
				"Scheduled section guidance"
			),
			"admin@rra.test"
		);

		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Scheduled Topic",
				"scheduled-topic",
				"Scheduled summary",
				"Scheduled intro"
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
				"Scheduled body"
			),
			"admin@rra.test"
		);

		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);
		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("SCHEDULE_PUBLISH", Instant.now().plusSeconds(300)),
			authentication("publisher@rra.test", "PUBLISHER")
		);
		var topic = topicRepository.findById(topicResponse.data().id()).orElseThrow();
		topic.schedulePublish(Instant.now().minusSeconds(60));
		topicRepository.save(topic);

		var adminAuthentication = authentication("admin@rra.test", "ADMIN");
		var response = topicWorkflowService.processScheduledPublishes(adminAuthentication);
		assertEquals(1, response.data().processedCount());

		AuditLog log = findAuditLog("CONTENT_TOPIC_SCHEDULED_PUBLISH_EXECUTED", "scheduled-topic");
		assertEquals("admin@rra.test", log.getActor());
		assertTrue(log.getDetails().contains("Scheduled topic published"));
	}

	private AuditLog findAuditLog(String action, String targetEmail) {
		return auditLogRepository.findAll().stream()
			.filter(log -> action.equals(log.getAction()))
			.filter(log -> targetEmail.equals(log.getTargetEmail()))
			.findFirst()
			.map(log -> {
				assertNotNull(log.getCreatedAt());
				return log;
			})
			.orElseThrow(() -> new AssertionError("Expected audit log for action " + action + " and target " + targetEmail));
	}

	private UsernamePasswordAuthenticationToken authentication(String username, String role) {
		return new UsernamePasswordAuthenticationToken(
			username,
			"N/A",
			List.of(new SimpleGrantedAuthority("ROLE_" + role))
		);
	}
}
