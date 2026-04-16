package com.rra.taxhandbook.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminContentMutationContractIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

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
	void createSectionReturnsExpectedContract() throws Exception {
		mockMvc.perform(post("/api/admin/content/sections")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "parentId": null,
					  "type": "MAIN",
					  "sortOrder": 1,
					  "locale": "EN",
					  "name": "Corporate Tax",
					  "slug": "corporate-tax",
					  "summary": "Corporate tax guidance"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Section created"))
			.andExpect(jsonPath("$.data.id").isNumber())
			.andExpect(jsonPath("$.data.parentId").doesNotExist())
			.andExpect(jsonPath("$.data.name").value("Corporate Tax"))
			.andExpect(jsonPath("$.data.slug").value("corporate-tax"))
			.andExpect(jsonPath("$.data.summary").value("Corporate tax guidance"))
			.andExpect(jsonPath("$.data.type").value("MAIN"))
			.andExpect(jsonPath("$.data.sortOrder").value(1));
	}

	@Test
	void createSectionRejectsDuplicateSlugForLocale() throws Exception {
		contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Existing Section", "duplicate-section", "Existing summary"),
			"admin@rra.test"
		);

		mockMvc.perform(post("/api/admin/content/sections")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "parentId": null,
					  "type": "MAIN",
					  "sortOrder": 2,
					  "locale": "EN",
					  "name": "Duplicate Section",
					  "slug": "duplicate-section",
					  "summary": "Duplicate slug attempt"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("A section already exists for slug 'duplicate-section' and locale EN"));
	}

	@Test
	void updateSectionReturnsExpectedContract() throws Exception {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Original Section", "original-section", "Original summary"),
			"admin@rra.test"
		);

		mockMvc.perform(put("/api/admin/content/sections/" + section.data().id())
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "parentId": null,
					  "type": "SUBGROUP",
					  "sortOrder": 3,
					  "locale": "EN",
					  "name": "Updated Section",
					  "slug": "updated-section",
					  "summary": "Updated summary"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Section updated"))
			.andExpect(jsonPath("$.data.id").value(section.data().id()))
			.andExpect(jsonPath("$.data.name").value("Updated Section"))
			.andExpect(jsonPath("$.data.slug").value("updated-section"))
			.andExpect(jsonPath("$.data.summary").value("Updated summary"))
			.andExpect(jsonPath("$.data.type").value("SUBGROUP"))
			.andExpect(jsonPath("$.data.sortOrder").value(3))
			.andExpect(jsonPath("$.data.status").value("DRAFT"));
	}

	@Test
	void createTopicReturnsExpectedContract() throws Exception {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Topic Parent", "topic-parent", "Topic parent summary"),
			"admin@rra.test"
		);

		mockMvc.perform(post("/api/admin/content/topics")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sectionId": %d,
					  "topicType": "TAX_TOPIC",
					  "sortOrder": 1,
					  "locale": "EN",
					  "title": "VAT Returns",
					  "slug": "vat-returns",
					  "summary": "VAT filing summary",
					  "introText": "VAT filing intro"
					}
					""".formatted(section.data().id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic created"))
			.andExpect(jsonPath("$.data.id").isNumber())
			.andExpect(jsonPath("$.data.sectionId").value(section.data().id()))
			.andExpect(jsonPath("$.data.title").value("VAT Returns"))
			.andExpect(jsonPath("$.data.slug").value("vat-returns"))
			.andExpect(jsonPath("$.data.summary").value("VAT filing summary"))
			.andExpect(jsonPath("$.data.introText").value("VAT filing intro"))
			.andExpect(jsonPath("$.data.topicType").value("TAX_TOPIC"))
			.andExpect(jsonPath("$.data.status").value("DRAFT"))
			.andExpect(jsonPath("$.data.blocks.length()").value(0));
	}

	@Test
	void updateTopicReturnsExpectedContract() throws Exception {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Update Topic Parent", "update-topic-parent", "Parent summary"),
			"admin@rra.test"
		);
		var topic = contentStructureService.createTopic(
			new AdminCreateTopicRequest(section.data().id(), TopicType.TAX_TOPIC, 1, LanguageCode.EN, "Original Topic", "original-topic", "Original topic summary", "Original intro"),
			"admin@rra.test"
		);

		mockMvc.perform(put("/api/admin/content/topics/" + topic.data().id())
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sectionId": %d,
					  "topicType": "GUIDE",
					  "sortOrder": 5,
					  "locale": "EN",
					  "title": "Updated Topic",
					  "slug": "updated-topic",
					  "summary": "Updated topic summary",
					  "introText": "Updated intro"
					}
					""".formatted(section.data().id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic updated"))
			.andExpect(jsonPath("$.data.id").value(topic.data().id()))
			.andExpect(jsonPath("$.data.sectionId").value(section.data().id()))
			.andExpect(jsonPath("$.data.title").value("Updated Topic"))
			.andExpect(jsonPath("$.data.slug").value("updated-topic"))
			.andExpect(jsonPath("$.data.summary").value("Updated topic summary"))
			.andExpect(jsonPath("$.data.introText").value("Updated intro"))
			.andExpect(jsonPath("$.data.topicType").value("GUIDE"))
			.andExpect(jsonPath("$.data.status").value("DRAFT"));
	}

	@Test
	void createTopicRejectsMissingSection() throws Exception {
		mockMvc.perform(post("/api/admin/content/topics")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sectionId": 999999,
					  "topicType": "TAX_TOPIC",
					  "sortOrder": 1,
					  "locale": "EN",
					  "title": "Missing Parent Topic",
					  "slug": "missing-parent-topic",
					  "summary": "Summary",
					  "introText": "Intro"
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Section not found: 999999"));
	}

	@Test
	void createTopicBlockReturnsExpectedContract() throws Exception {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Block Parent", "block-parent", "Block parent summary"),
			"admin@rra.test"
		);
		var topic = contentStructureService.createTopic(
			new AdminCreateTopicRequest(section.data().id(), TopicType.TAX_TOPIC, 1, LanguageCode.EN, "Block Topic", "block-topic", "Block topic summary", "Block intro"),
			"admin@rra.test"
		);

		mockMvc.perform(post("/api/admin/content/topics/" + topic.data().id() + "/blocks")
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "blockType": "RICH_TEXT",
					  "sortOrder": 1,
					  "anchorKey": "overview",
					  "locale": "EN",
					  "title": "Overview",
					  "body": "Block contract body"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic block created"))
			.andExpect(jsonPath("$.data.id").isNumber())
			.andExpect(jsonPath("$.data.title").value("Overview"))
			.andExpect(jsonPath("$.data.body").value("Block contract body"))
			.andExpect(jsonPath("$.data.blockType").value("RICH_TEXT"))
			.andExpect(jsonPath("$.data.anchorKey").value("overview"))
			.andExpect(jsonPath("$.data.sortOrder").value(1));
	}

	@Test
	void updateTopicBlockReturnsExpectedContract() throws Exception {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Update Block Parent", "update-block-parent", "Parent summary"),
			"admin@rra.test"
		);
		var topic = contentStructureService.createTopic(
			new AdminCreateTopicRequest(section.data().id(), TopicType.TAX_TOPIC, 1, LanguageCode.EN, "Update Block Topic", "update-block-topic", "Topic summary", "Topic intro"),
			"admin@rra.test"
		);
		var block = contentStructureService.createTopicBlock(
			topic.data().id(),
			new AdminCreateTopicBlockRequest(TopicBlockType.RICH_TEXT, 1, "overview", LanguageCode.EN, "Overview", "Original block body"),
			"admin@rra.test"
		);

		mockMvc.perform(put("/api/admin/content/blocks/" + block.data().id())
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "blockType": "INFO_CARD",
					  "sortOrder": 2,
					  "anchorKey": "quick-facts",
					  "locale": "EN",
					  "title": "Quick Facts",
					  "body": "Updated block body"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Topic block updated"))
			.andExpect(jsonPath("$.data.id").value(block.data().id()))
			.andExpect(jsonPath("$.data.title").value("Quick Facts"))
			.andExpect(jsonPath("$.data.body").value("Updated block body"))
			.andExpect(jsonPath("$.data.blockType").value("INFO_CARD"))
			.andExpect(jsonPath("$.data.anchorKey").value("quick-facts"))
			.andExpect(jsonPath("$.data.sortOrder").value(2));
	}

	@Test
	void updateTopicBlockRejectsInvalidLocale() throws Exception {
		var section = contentStructureService.createSection(
			new AdminCreateSectionRequest(null, SectionType.MAIN, 1, LanguageCode.EN, "Invalid Locale Parent", "invalid-locale-parent", "Parent summary"),
			"admin@rra.test"
		);
		var topic = contentStructureService.createTopic(
			new AdminCreateTopicRequest(section.data().id(), TopicType.TAX_TOPIC, 1, LanguageCode.EN, "Invalid Locale Topic", "invalid-locale-topic", "Topic summary", "Topic intro"),
			"admin@rra.test"
		);
		var block = contentStructureService.createTopicBlock(
			topic.data().id(),
			new AdminCreateTopicBlockRequest(TopicBlockType.RICH_TEXT, 1, "overview", LanguageCode.EN, "Overview", "Original block body"),
			"admin@rra.test"
		);

		mockMvc.perform(put("/api/admin/content/blocks/" + block.data().id())
				.with(user("editor@rra.test").roles("EDITOR"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "blockType": "INFO_CARD",
					  "sortOrder": 2,
					  "anchorKey": "quick-facts",
					  "locale": "DE",
					  "title": "Quick Facts",
					  "body": "Updated block body"
					}
					"""))
			.andExpect(status().isBadRequest());
	}
}
