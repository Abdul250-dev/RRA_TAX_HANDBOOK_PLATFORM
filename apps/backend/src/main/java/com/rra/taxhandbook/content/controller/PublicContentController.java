package com.rra.taxhandbook.content.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.dto.HomepageResponse;
import com.rra.taxhandbook.content.dto.PublicSearchResponse;
import com.rra.taxhandbook.content.dto.PublicSectionDetailResponse;
import com.rra.taxhandbook.content.dto.SectionSummaryResponse;
import com.rra.taxhandbook.content.dto.TopicDetailResponse;
import com.rra.taxhandbook.content.dto.TopicSummaryResponse;
import com.rra.taxhandbook.content.search.PublicContentSearchService;
import com.rra.taxhandbook.content.service.ContentStructureService;

@RestController
@RequestMapping("/api/public")
public class PublicContentController {

	private final ContentStructureService contentStructureService;
	private final PublicContentSearchService publicContentSearchService;

	public PublicContentController(ContentStructureService contentStructureService, PublicContentSearchService publicContentSearchService) {
		this.contentStructureService = contentStructureService;
		this.publicContentSearchService = publicContentSearchService;
	}

	@GetMapping("/homepage")
	public HomepageResponse getHomepage(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getHomepage(locale);
	}

	@GetMapping("/search")
	public PublicSearchResponse search(@RequestParam String q, @RequestParam(defaultValue = "EN") LanguageCode locale) {
		return publicContentSearchService.search(q, locale);
	}

	@GetMapping("/sections")
	public List<SectionSummaryResponse> getSections(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getSections(locale);
	}

	@GetMapping("/sections/{slug}")
	public PublicSectionDetailResponse getSection(@PathVariable String slug, @RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getSectionBySlug(slug, locale);
	}

	@GetMapping("/topics/{slug}")
	public TopicDetailResponse getTopic(@PathVariable String slug, @RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getTopicBySlug(slug, locale);
	}

	@GetMapping("/guides")
	public List<TopicSummaryResponse> getGuides(@RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getGuides(locale);
	}

	@GetMapping("/guides/{slug}")
	public TopicDetailResponse getGuide(@PathVariable String slug, @RequestParam(defaultValue = "EN") LanguageCode locale) {
		return contentStructureService.getGuideBySlug(slug, locale);
	}
}
