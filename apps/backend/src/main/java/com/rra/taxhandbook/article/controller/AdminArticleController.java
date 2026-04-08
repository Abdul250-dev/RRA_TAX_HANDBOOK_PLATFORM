package com.rra.taxhandbook.article.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.article.dto.ArticleRequest;
import com.rra.taxhandbook.article.dto.ArticleResponse;
import com.rra.taxhandbook.article.service.ArticleService;
import com.rra.taxhandbook.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

	private final ArticleService articleService;

	public AdminArticleController(ArticleService articleService) {
		this.articleService = articleService;
	}

	@PostMapping
	public ApiResponse<ArticleResponse> createArticle(@RequestBody ArticleRequest request) {
		return articleService.createArticle(request);
	}
}
