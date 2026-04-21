package com.rra.taxhandbook.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.ScheduledPublishProcessingResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.dto.TopicWorkflowResponse;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardTranslationRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentTranslationRepository;
import com.rra.taxhandbook.content.section.entity.SectionType;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.service.ContentStructureService;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicType;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockType;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;
import com.rra.taxhandbook.content.workflow.TopicWorkflowHistoryRepository;
import com.rra.taxhandbook.content.workflow.TopicWorkflowService;

@SpringBootTest
@ActiveProfiles("test")
class TopicScheduledPublishIntegrationTests {

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
	void schedulePublishKeepsTopicApprovedUntilProcessed() {
		Topic topic = createApprovedTopicWithBlock("scheduled-approved");
		Instant scheduledAt = Instant.now().plusSeconds(3600);

		var response = topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SCHEDULE_PUBLISH", scheduledAt),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		assertEquals("APPROVED", response.data().status());
		Topic savedTopic = topicRepository.findById(topic.getId()).orElseThrow();
		assertEquals(ContentStatus.APPROVED, savedTopic.getStatus());
		assertTrue(Math.abs(savedTopic.getScheduledPublishAt().toEpochMilli() - scheduledAt.toEpochMilli()) < 1000);
		assertTrue(Math.abs(savedTopic.getScheduledPublishAt().toEpochMilli() - response.data().scheduledPublishAt().toEpochMilli()) < 1000);
		assertThrows(
			ResourceNotFoundException.class,
			() -> contentStructureService.getTopicBySlug("scheduled-approved", LanguageCode.EN)
		);
	}

	@Test
	void schedulePublishRejectsMissingOrPastTimestamp() {
		Topic topic = createApprovedTopicWithBlock("schedule-requires-future");

		var missingException = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("SCHEDULE_PUBLISH", null),
				authentication("publisher@rra.test", "PUBLISHER")
			)
		);
		assertEquals("scheduledAt is required for SCHEDULE_PUBLISH.", missingException.getMessage());

		var pastException = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("SCHEDULE_PUBLISH", Instant.now().minusSeconds(60)),
				authentication("publisher@rra.test", "PUBLISHER")
			)
		);
		assertEquals("scheduledAt must be a future timestamp.", pastException.getMessage());
	}

	@Test
	void reviewerCannotSchedulePublish() {
		Topic topic = createApprovedTopicWithBlock("reviewer-cannot-schedule");

		var exception = assertThrows(
			UnauthorizedException.class,
			() -> topicWorkflowService.transitionTopic(
				topic.getId(),
				new TopicWorkflowActionRequest("SCHEDULE_PUBLISH", Instant.now().plusSeconds(600)),
				authentication("reviewer@rra.test", "REVIEWER")
			)
		);

		assertEquals("You do not have permission to perform this workflow action.", exception.getMessage());
	}

	@Test
	void processScheduledPublishesPromotesDueTopics() {
		Topic topic = createApprovedTopicWithBlock("scheduled-goes-live");
		topicWorkflowService.transitionTopic(
			topic.getId(),
			new TopicWorkflowActionRequest("SCHEDULE_PUBLISH", Instant.now().plusSeconds(600)),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		Topic scheduledTopic = topicRepository.findById(topic.getId()).orElseThrow();
		scheduledTopic.schedulePublish(Instant.now().minusSeconds(5));
		scheduledTopic.touch(Instant.now());
		topicRepository.save(scheduledTopic);

		var response = topicWorkflowService.processScheduledPublishes(authentication("publisher@rra.test", "PUBLISHER"));

		assertEquals(1, response.data().processedCount());
		assertEquals(0, response.data().skippedCount());
		assertEquals(List.of(topic.getId()), response.data().processedTopicIds());
		assertTrue(response.data().skippedTopics().isEmpty());
		Topic publishedTopic = topicRepository.findById(topic.getId()).orElseThrow();
		assertEquals(ContentStatus.PUBLISHED, publishedTopic.getStatus());
		assertNotNull(publishedTopic.getPublishedAt());
		assertNull(publishedTopic.getScheduledPublishAt());
		assertEquals(ContentStatus.PUBLISHED, publishedTopic.getSection().getStatus());

		TopicDetailResponse publicTopic = contentStructureService.getTopicBySlug("scheduled-goes-live", LanguageCode.EN);
		assertEquals("PUBLISHED", publicTopic.status());
	}

	@Test
	void processScheduledPublishesReportsSkippedTopicsWithReasons() {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Skipped Scheduled Section",
				"skipped-scheduled-section",
				"Scheduled section summary"
			),
			"admin@rra.test"
		);
		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Skipped Scheduled Topic",
				"skipped-scheduled-topic",
				"Scheduled topic summary",
				"Scheduled topic intro"
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

		Topic approvedTopic = topicRepository.findById(topicResponse.data().id()).orElseThrow();
		approvedTopic.schedulePublish(Instant.now().minusSeconds(5));
		approvedTopic.touch(Instant.now());
		topicRepository.save(approvedTopic);

		var response = topicWorkflowService.processScheduledPublishes(authentication("publisher@rra.test", "PUBLISHER"));

		assertEquals(0, response.data().processedCount());
		assertEquals(1, response.data().skippedCount());
		assertTrue(response.data().processedTopicIds().isEmpty());
		assertEquals(topicResponse.data().id(), response.data().skippedTopics().get(0).topicId());
		assertEquals(List.of("Topic must contain at least one content block."), response.data().skippedTopics().get(0).reasons());
		assertEquals(ContentStatus.APPROVED, topicRepository.findById(topicResponse.data().id()).orElseThrow().getStatus());
	}

	private Topic createApprovedTopicWithBlock(String slug) {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Scheduled Section " + slug,
				"section-" + slug,
				"Scheduled section summary"
			),
			"admin@rra.test"
		);

		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Scheduled Topic " + slug,
				slug,
				"Scheduled topic summary",
				"Scheduled topic intro"
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
		return topicRepository.findById(topicResponse.data().id()).orElseThrow();
	}

	private UsernamePasswordAuthenticationToken authentication(String username, String role) {
		return new UsernamePasswordAuthenticationToken(
			username,
			"N/A",
			List.of(new SimpleGrantedAuthority("ROLE_" + role))
		);
	}
}
