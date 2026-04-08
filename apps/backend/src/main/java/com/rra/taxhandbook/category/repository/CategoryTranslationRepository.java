package com.rra.taxhandbook.category.repository;

import java.util.List;

import com.rra.taxhandbook.category.entity.CategoryTranslation;

public interface CategoryTranslationRepository {

	List<CategoryTranslation> findAll();
}
