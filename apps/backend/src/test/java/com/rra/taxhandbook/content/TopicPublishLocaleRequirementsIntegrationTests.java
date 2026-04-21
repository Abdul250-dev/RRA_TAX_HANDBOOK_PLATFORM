package com.rra.taxhandbook.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminUpdateTopicRequest;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardTranslationRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentTranslationRepository;
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
import com.rra.taxhandbook.content.workflow.TopicWorkflowHistoryRepository;
import com.rra.taxhandbook.content.workflow.TopicWorkflowService;

@SpringBootTest(properties = "app.content.required-publish-locales=EN,FR")
@ActiveProfiles("test")
class TopicPublishLocaleRequirementsIntegrationTests {

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
	void publishRequiresConfiguredLocalesAndUpdateEndpointsCanAddMissingTranslations() {
		ApiResponse<SectionSummaryResponse> sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Tax Section",
				"tax-section",
				"English section summary"
			),
			"admin@rra.test"
		);

		ApiResponse<TopicDetailResponse> topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Tax Topic",
				"tax-topic",
				"English topic summary",
				"English topic intro"
			),
			"admin@rra.test"
		);

		var blockResponse = contentStructureService.createTopicBlock(
			topicResponse.data().id(),
			new AdminCreateTopicBlockRequest(
				TopicBlockType.RICH_TEXT,
				1,
				"overview",
				LanguageCode.EN,
				"Overview",
				"English body"
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

		IllegalArgumentException topicLocaleException = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topicResponse.data().id(),
				new TopicWorkflowActionRequest("PUBLISH"),
				authentication("publisher@rra.test", "PUBLISHER")
			)
		);
		assertEquals(
			"Topic is not ready to publish: Topic is missing one or more required publish locales: [EN, FR]. Section is missing one or more required publish locales: [EN, FR]. Topic block " + blockResponse.data().id() + " is missing one or more required publish locales: [EN, FR].",
			topicLocaleException.getMessage()
		);

		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("REQUEST_CHANGES", null, "Add the missing French translations before publishing."),
			authentication("reviewer@rra.test", "REVIEWER")
		);

		contentStructureService.updateSection(
			sectionResponse.data().id(),
			new AdminUpdateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.FR,
				"Section fiscale",
				"section-fiscale",
				"Resume en francais"
			),
			"admin@rra.test"
		);
		contentStructureService.updateTopic(
			topicResponse.data().id(),
			new AdminUpdateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.FR,
				"Sujet fiscal",
				"sujet-fiscal",
				"Resume du sujet",
				"Introduction francaise"
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

		IllegalArgumentException blockLocaleException = assertThrows(
			IllegalArgumentException.class,
			() -> topicWorkflowService.transitionTopic(
				topicResponse.data().id(),
				new TopicWorkflowActionRequest("PUBLISH"),
				authentication("publisher@rra.test", "PUBLISHER")
			)
		);
		assertEquals(
			"Topic is not ready to publish: Topic block " + blockResponse.data().id() + " is missing one or more required publish locales: [EN, FR].",
			blockLocaleException.getMessage()
		);

		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("REQUEST_CHANGES", null, "Add the missing French block translation before publishing."),
			authentication("reviewer@rra.test", "REVIEWER")
		);

		contentStructureService.updateTopicBlock(
			blockResponse.data().id(),
			new AdminUpdateTopicBlockRequest(
				TopicBlockType.RICH_TEXT,
				1,
				"overview-fr",
				LanguageCode.FR,
				"Apercu",
				"Contenu francais"
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

		var publishResponse = topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("PUBLISH"),
			authentication("publisher@rra.test", "PUBLISHER")
		);

		assertEquals("PUBLISHED", publishResponse.data().status());
	}

	private UsernamePasswordAuthenticationToken authentication(String username, String role) {
		return new UsernamePasswordAuthenticationToken(
			username,
			"N/A",
			List.of(new SimpleGrantedAuthority("ROLE_" + role))
		);
	}
}
