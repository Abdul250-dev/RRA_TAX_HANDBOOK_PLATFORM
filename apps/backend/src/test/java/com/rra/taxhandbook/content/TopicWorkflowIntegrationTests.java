package com.rra.taxhandbook.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.AdminSectionResponse;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.SectionWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicWorkflowResponse;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardTranslationRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentTranslationRepository;
import com.rra.taxhandbook.content.section.entity.Section;
import com.rra.taxhandbook.content.section.entity.SectionType;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.service.ContentStructureService;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicType;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlock;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockType;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;
import com.rra.taxhandbook.content.workflow.TopicWorkflowHistoryRepository;
import com.rra.taxhandbook.content.workflow.TopicWorkflowService;

@SpringBootTest
@ActiveProfiles("test")
class TopicWorkflowIntegrationTests {

	@Autowired
	private TopicWorkflowService topicWorkflowService;

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

	@Autowired
	private HomepageCardTranslationRepository homepageCardTranslationRepository;

	@Autowired
	private HomepageCardRepository homepageCardRepository;

	@Autowired
	private HomepageContentTranslationRepository homepageContentTranslationRepository;

	@Autowired
	private HomepageContentRepository homepageContentRepository;

	@Autowired
	private TopicWorkflowHistoryRepository topicWorkflowHistoryRepository;

	@BeforeEach
	void setUp() {
		homepageCardTranslationRepository.deleteAll();
		homepageCardRepository.deleteAll();
		homepageContentTranslationRepository.deleteAll();
		homepageContentRepository.deleteAll();
		topicWorkflowHistoryRepository.deleteAll();
		topicBlockTranslationRepository.deleteAll();
		topicBlockRepository.deleteAll();
		topicTranslationRepository.deleteAll();
		topicRepository.deleteAll();
		sectionTranslationRepository.deleteAll();
		sectionRepository.deleteAll();
	}

	@Test
	void workflowHappyPathTransitionsTopicFromDraftToArchived() {
		var topic = createDraftTopic("workflow-tax");
		var block = createBlock(topic, ContentStatus.DRAFT);

		ApiResponse<TopicWorkflowResponse> submitResponse = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		assertEquals("REVIEW", submitResponse.data().status());
		assertTopicAndBlockStatus(topic.getId(), block.getId(), ContentStatus.REVIEW);

		ApiResponse<TopicWorkflowResponse> approveResponse = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);
		assertEquals("APPROVED", approveResponse.data().status());
		assertTopicAndBlockStatus(topic.getId(), block.getId(), ContentStatus.APPROVED);

		ApiResponse<TopicWorkflowResponse> publishResponse = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);
		assertEquals("PUBLISHED", publishResponse.data().status());
		Topic publishedTopic = topicRepository.findById(topic.getId()).orElseThrow();
		assertEquals(ContentStatus.PUBLISHED, publishedTopic.getStatus());
		assertNotNull(publishedTopic.getPublishedAt());
		assertEquals(ContentStatus.PUBLISHED, publishedTopic.getSection().getStatus());
		assertTopicAndBlockStatus(topic.getId(), block.getId(), ContentStatus.PUBLISHED);

		ApiResponse<TopicWorkflowResponse> unpublishResponse = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("UNPUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);
		assertEquals("APPROVED", unpublishResponse.data().status());
		assertTopicAndBlockStatus(topic.getId(), block.getId(), ContentStatus.APPROVED);
		assertEquals(ContentStatus.APPROVED, topicRepository.findById(topic.getId()).orElseThrow().getSection().getStatus());

		ApiResponse<TopicWorkflowResponse> republishResponse = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);
		assertEquals("PUBLISHED", republishResponse.data().status());
		assertTopicAndBlockStatus(topic.getId(), block.getId(), ContentStatus.PUBLISHED);

		ApiResponse<TopicWorkflowResponse> archiveResponse = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("ARCHIVE"),
			authentication("publisher@rra.test", "PUBLISHER")
		);
		assertEquals("ARCHIVED", archiveResponse.data().status());
		assertTopicAndBlockStatus(topic.getId(), block.getId(), ContentStatus.ARCHIVED);
	}

	@Test
	void requestChangesMovesTopicBackToDraft() {
		var topic = createDraftTopic("request-changes-tax");
		createBlock(topic, ContentStatus.DRAFT);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);

		var response = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("REQUEST_CHANGES", null, "Clarify the filing steps before approval."),
			authentication("reviewer@rra.test", "REVIEWER")
		);

		assertEquals("DRAFT", response.data().status());
		assertEquals("Clarify the filing steps before approval.", response.data().comment());
		Topic savedTopic = topicRepository.findById(topic.getId()).orElseThrow();
		assertEquals(ContentStatus.DRAFT, savedTopic.getStatus());
		var history = topicWorkflowService.getWorkflowHistory(topic.getId());
		assertEquals("REQUEST_CHANGES", history.get(0).action());
		assertEquals("REVIEW", history.get(0).fromStatus());
		assertEquals("DRAFT", history.get(0).toStatus());
		assertEquals("Clarify the filing steps before approval.", history.get(0).comment());
	}

	@Test
	void requestChangesRequiresComment() {
		var topic = createDraftTopic("request-changes-comment-required");
		createBlock(topic, ContentStatus.DRAFT);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("REQUEST_CHANGES"),
				authentication("reviewer@rra.test", "REVIEWER")
			)
		);

		assertEquals("A comment is required when requesting changes so editors know what to fix.", exception.getMessage());
	}

	@Test
	void editorCannotApproveTopic() {
		var topic = createDraftTopic("editor-cannot-approve");

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);

		var exception = assertThrows(
			UnauthorizedException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("APPROVE"),
				authentication("editor@rra.test", "EDITOR")
			)
		);

		assertEquals("You do not have permission to perform this workflow action.", exception.getMessage());
	}

	@Test
	void publisherCannotPublishTopicBeforeApproval() {
		var topic = createDraftTopic("publish-before-approval");

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("PUBLISH"),
				authentication("publisher@rra.test", "PUBLISHER")
			)
		);

		assertTrue(exception.getMessage().contains("DRAFT"));
	}

	@Test
	void publisherCannotPublishTopicWithoutBlocks() {
		var topic = createDraftTopic("publish-without-blocks");

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("PUBLISH"),
				authentication("publisher@rra.test", "PUBLISHER")
			)
		);

		assertEquals(
			"Topic is not ready to publish: Topic must contain at least one content block.",
			exception.getMessage()
		);
	}

	@Test
	void reviewerCanRequestChangesAfterApproval() {
		var topic = createDraftTopic("request-after-approval");

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);

		var response = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("REQUEST_CHANGES", null, "Resolve publish readiness issues before release."),
			authentication("reviewer@rra.test", "REVIEWER")
		);

		assertEquals("DRAFT", response.data().status());
		assertEquals(ContentStatus.DRAFT, topicRepository.findById(topic.getId()).orElseThrow().getStatus());
	}

	@Test
	void deleteTopicRejectsReviewStatus() {
		var topic = createDraftTopic("delete-review-topic");

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> contentStructureService.deleteTopic(topic.getId(), "admin@rra.test")
		);

		assertEquals("Only draft or archived topics can be deleted.", exception.getMessage());
	}

	@Test
	void deleteTopicBlockRejectsPublishedStatus() {
		var topic = createDraftTopic("delete-published-block");
		var block = createBlock(topic, ContentStatus.DRAFT);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> contentStructureService.deleteTopicBlock(block.getId(), "admin@rra.test")
		);

		assertEquals("Only draft or archived topic blocks can be deleted.", exception.getMessage());
	}

	@Test
	void publicReadOnlyReturnsPublishedTopicContent() {
		var topic = createDraftTopic("public-topic-visibility");

		contentStructureService.createTopicBlock(
			topic.getId(),
			new AdminCreateTopicBlockRequest(
				TopicBlockType.RICH_TEXT,
				1,
				"overview",
				LanguageCode.EN,
				"Overview",
				"Published body"
			),
			"admin@rra.test"
		);

		assertThrows(
			ResourceNotFoundException.class,
			() -> contentStructureService.getTopicBySlug("public-topic-visibility", LanguageCode.EN)
		);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		TopicDetailResponse publishedResponse = contentStructureService.getTopicBySlug("public-topic-visibility", LanguageCode.EN);
		assertEquals("PUBLISHED", publishedResponse.status());
		assertEquals(1, publishedResponse.blocks().size());

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("UNPUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		assertThrows(
			ResourceNotFoundException.class,
			() -> contentStructureService.getTopicBySlug("public-topic-visibility", LanguageCode.EN)
		);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("ARCHIVE"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		assertThrows(
			ResourceNotFoundException.class,
			() -> contentStructureService.getTopicBySlug("public-topic-visibility", LanguageCode.EN)
		);
	}

	@Test
	void publicSectionsOnlyIncludePublishedSections() {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Visibility Section",
				"visibility-section",
				"Section summary"
			),
			"admin@rra.test"
		);

		java.util.List<SectionSummaryResponse> draftSections = contentStructureService.getSections(LanguageCode.EN);
		assertTrue(draftSections.isEmpty());
		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Visibility Topic",
				"visibility-topic",
				"Topic summary",
				"Topic intro"
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
				"Body"
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
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		java.util.List<SectionSummaryResponse> visibleSections = contentStructureService.getSections(LanguageCode.EN);
		assertEquals(1, visibleSections.size());
		assertEquals("visibility-section", visibleSections.get(0).slug());

		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("ARCHIVE"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		ApiResponse<AdminSectionResponse> archivedSection = contentStructureService.transitionSection(
			sectionResponse.data().id(),
			new SectionWorkflowActionRequest("ARCHIVE"),
			"publisher@rra.test"
		);
		assertEquals("ARCHIVED", archivedSection.data().status());

		assertTrue(contentStructureService.getSections(LanguageCode.EN).isEmpty());
	}

	@Test
	void sectionCannotPublishWithoutPublishedTopics() {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Draft Section",
				"draft-section",
				"Section summary"
			),
			"admin@rra.test"
		);

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> contentStructureService.transitionSection(
				sectionResponse.data().id(),
				new SectionWorkflowActionRequest("PUBLISH"),
				"publisher@rra.test"
			)
		);

		assertEquals("Section must contain at least one published topic before it can be published.", exception.getMessage());
	}

	@Test
	void sectionCannotArchiveWhilePublishedTopicsExist() {
		var topic = createDraftTopic("section-archive-block");
		createBlock(topic, ContentStatus.DRAFT);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		var exception = assertThrows(
			IllegalArgumentException.class,
			() -> contentStructureService.transitionSection(
				topic.getSection().getId(),
				new SectionWorkflowActionRequest("ARCHIVE"),
				"publisher@rra.test"
			)
		);

		assertEquals("Section cannot be archived while it still contains published topics.", exception.getMessage());
	}

	@Test
	void reviewerCannotUnpublishTopic() {
		var topic = createDraftTopic("reviewer-cannot-unpublish");
		createBlock(topic, ContentStatus.DRAFT);

		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("APPROVE"),
			authentication("reviewer@rra.test", "REVIEWER")
		);
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		var exception = assertThrows(
			UnauthorizedException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("UNPUBLISH"),
				authentication("reviewer@rra.test", "REVIEWER")
			)
		);

		assertEquals("You do not have permission to perform this workflow action.", exception.getMessage());
	}

	private Topic createDraftTopic(String slug) {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Workflow Section " + slug,
				"section-" + slug,
				"Workflow section summary"
			),
			"admin@rra.test"
		);

		ApiResponse<TopicDetailResponse> topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Workflow Topic " + slug,
				slug,
				"Workflow topic summary",
				"Workflow topic intro"
			),
			"admin@rra.test"
		);

		return topicRepository.findById(topicResponse.data().id()).orElseThrow();
	}

	private TopicBlock createBlock(Topic topic, ContentStatus status) {
		Section section = topic.getSection();
		section.changeStatus(ContentStatus.DRAFT);
		section.touch(Instant.now());
		sectionRepository.save(section);

		TopicBlock block = new TopicBlock(
			topic,
			TopicBlockType.RICH_TEXT,
			1,
			status,
			"overview",
			false,
			Instant.now(),
			Instant.now()
		);
		TopicBlock savedBlock = topicBlockRepository.save(block);
		topicBlockTranslationRepository.save(
			new com.rra.taxhandbook.content.topicblock.entity.TopicBlockTranslation(
				savedBlock,
				LanguageCode.EN,
				"Overview",
				"Body"
			)
		);
		return savedBlock;
	}

	private void assertTopicAndBlockStatus(Long topicId, Long blockId, ContentStatus expectedStatus) {
		Topic savedTopic = topicRepository.findById(topicId).orElseThrow();
		TopicBlock savedBlock = topicBlockRepository.findById(blockId).orElseThrow();
		assertEquals(expectedStatus, savedTopic.getStatus());
		assertEquals(expectedStatus, savedBlock.getStatus());
	}

	private UsernamePasswordAuthenticationToken authentication(String username, String role) {
		return new UsernamePasswordAuthenticationToken(
			username,
			"N/A",
			List.of(new SimpleGrantedAuthority("ROLE_" + role))
		);
	}
}
