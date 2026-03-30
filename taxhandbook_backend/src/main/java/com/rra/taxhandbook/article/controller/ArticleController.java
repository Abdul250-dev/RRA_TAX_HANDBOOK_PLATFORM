package com.rra.taxhandbook.article.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.article.dto.ArticleRequest;
import com.rra.taxhandbook.article.dto.ArticleResponse;
import com.rra.taxhandbook.article.dto.ArticleSearchRequest;
import com.rra.taxhandbook.article.service.ArticleService;
import com.rra.taxhandbook.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

	private final ArticleService articleService;

	public ArticleController(ArticleService articleService) {
		this.articleService = articleService;
	}

	@GetMapping
	public List<ArticleResponse> getArticles() {
		return articleService.getArticles();
	}

	@PostMapping("/search")
	public List<ArticleResponse> searchArticles(@RequestBody ArticleSearchRequest request) {
		return articleService.searchArticles(request);
	}

	@PostMapping
	public ApiResponse<ArticleResponse> createArticle(@RequestBody ArticleRequest request) {
		return articleService.createArticle(request);
	}
}
