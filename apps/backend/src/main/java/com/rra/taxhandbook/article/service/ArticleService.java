package com.rra.taxhandbook.article.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.article.dto.ArticleRequest;
import com.rra.taxhandbook.article.dto.ArticleResponse;
import com.rra.taxhandbook.article.dto.ArticleSearchRequest;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.util.SlugUtil;

@Service
public class ArticleService {

	public List<ArticleResponse> getArticles() {
		return List.of(new ArticleResponse(1L, "VAT Basics", "vat-basics", LanguageCode.EN, ContentStatus.PUBLISHED));
	}

	public List<ArticleResponse> searchArticles(ArticleSearchRequest request) {
		return getArticles();
	}

	public ApiResponse<ArticleResponse> createArticle(ArticleRequest request) {
		ArticleResponse response = new ArticleResponse(2L, request.title(), SlugUtil.toSlug(request.title()), request.language(), request.status());
		return new ApiResponse<>("Article scaffold created", response);
	}
}
