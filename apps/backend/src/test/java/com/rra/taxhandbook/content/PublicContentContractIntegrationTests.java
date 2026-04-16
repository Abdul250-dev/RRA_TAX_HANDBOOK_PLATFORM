package com.rra.taxhandbook.content;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.AdminCreateSectionRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicBlockRequest;
import com.rra.taxhandbook.content.dto.AdminCreateTopicRequest;
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
import com.rra.taxhandbook.content.dto.TopicWorkflowActionRequest;
import com.rra.taxhandbook.content.workflow.TopicWorkflowService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicContentContractIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

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

	@BeforeEach
	void setUp() {
		topicBlockTranslationRepository.deleteAll();
		topicBlockRepository.deleteAll();
		topicTranslationRepository.deleteAll();
		topicRepository.deleteAll();
		sectionTranslationRepository.deleteAll();
		sectionRepository.deleteAll();
	}

	@Test
	void publicSectionsEndpointReturnsPublishedSectionContract() throws Exception {
		seedPublishedTopic("public-contract-topic");
		createDraftOnlySection();

		mockMvc.perform(get("/api/public/sections").queryParam("locale", "EN"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].name").value("Public Contract Section"))
			.andExpect(jsonPath("$[0].slug").value("public-contract-section"))
			.andExpect(jsonPath("$[0].summary").value("Section for public API contract tests"))
			.andExpect(jsonPath("$[0].type").value("MAIN"))
			.andExpect(jsonPath("$[0].sortOrder").value(1));
	}

	@Test
	void publicSectionsEndpointReturnsEmptyArrayWhenNothingPublished() throws Exception {
		createDraftOnlySection();

		mockMvc.perform(get("/api/public/sections").queryParam("locale", "EN"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void publicTopicEndpointReturnsPublishedTopicContract() throws Exception {
		seedPublishedTopic("public-contract-topic");

		mockMvc.perform(get("/api/public/topics/{slug}", "public-contract-topic").queryParam("locale", "EN"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Public Contract Topic"))
			.andExpect(jsonPath("$.slug").value("public-contract-topic"))
			.andExpect(jsonPath("$.summary").value("Public topic summary"))
			.andExpect(jsonPath("$.introText").value("Public topic intro"))
			.andExpect(jsonPath("$.topicType").value("TAX_TOPIC"))
			.andExpect(jsonPath("$.status").value("PUBLISHED"))
			.andExpect(jsonPath("$.scheduledPublishAt").doesNotExist())
			.andExpect(jsonPath("$.blocks.length()").value(1))
			.andExpect(jsonPath("$.blocks[0].title").value("Overview"))
			.andExpect(jsonPath("$.blocks[0].body").value("Published contract body"))
			.andExpect(jsonPath("$.blocks[0].blockType").value("RICH_TEXT"))
			.andExpect(jsonPath("$.blocks[0].anchorKey").value("overview"))
			.andExpect(jsonPath("$.blocks[0].sortOrder").value(1));
	}

	@Test
	void publicTopicEndpointReturnsNotFoundForUnpublishedTopic() throws Exception {
		contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				2,
				LanguageCode.EN,
				"Draft Only Section",
				"draft-only-section",
				"Draft section"
			),
			"admin@rra.test"
		);

		contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionRepository.findAll().get(0).getId(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Draft Only Topic",
				"draft-only-topic",
				"Draft summary",
				"Draft intro"
			),
			"admin@rra.test"
		);

		mockMvc.perform(get("/api/public/topics/{slug}", "draft-only-topic").queryParam("locale", "EN"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Topic not found for slug: draft-only-topic"));
	}

	@Test
	void publicContentEndpointsRejectInvalidLocale() throws Exception {
		mockMvc.perform(get("/api/public/sections").queryParam("locale", "DE"))
			.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/public/topics/{slug}", "missing-topic").queryParam("locale", "DE"))
			.andExpect(status().isBadRequest());
	}

	private void seedPublishedTopic(String slug) {
		var sectionResponse = contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				1,
				LanguageCode.EN,
				"Public Contract Section",
				"public-contract-section",
				"Section for public API contract tests"
			),
			"admin@rra.test"
		);

		var topicResponse = contentStructureService.createTopic(
			new AdminCreateTopicRequest(
				sectionResponse.data().id(),
				TopicType.TAX_TOPIC,
				1,
				LanguageCode.EN,
				"Public Contract Topic",
				slug,
				"Public topic summary",
				"Public topic intro"
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
				"Published contract body"
			),
			"admin@rra.test"
		);

		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("SUBMIT_FOR_REVIEW"),
			TestAuth.authentication("editor@rra.test", "EDITOR")
		);
		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("APPROVE"),
			TestAuth.authentication("reviewer@rra.test", "REVIEWER")
		);
		topicWorkflowService.transitionTopic(
			topicResponse.data().id(),
			new TopicWorkflowActionRequest("PUBLISH"),
			TestAuth.authentication("publisher@rra.test", "PUBLISHER")
		);
	}

	private void createDraftOnlySection() {
		contentStructureService.createSection(
			new AdminCreateSectionRequest(
				null,
				SectionType.MAIN,
				3,
				LanguageCode.EN,
				"Hidden Draft Section",
				"hidden-draft-section",
				"Should not be public"
			),
			"admin@rra.test"
		);
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
