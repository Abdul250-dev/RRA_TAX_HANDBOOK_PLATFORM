package com.rra.taxhandbook.article.repository;

import java.util.List;

import com.rra.taxhandbook.article.entity.ArticleTranslation;

public interface ArticleTranslationRepository {

	List<ArticleTranslation> findAll();
}
