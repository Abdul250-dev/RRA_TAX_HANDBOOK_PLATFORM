package com.rra.taxhandbook.category.repository;

import java.util.List;

import com.rra.taxhandbook.category.entity.Category;

public interface CategoryRepository {

	List<Category> findAll();
}
