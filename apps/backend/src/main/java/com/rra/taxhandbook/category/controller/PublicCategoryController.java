package com.rra.taxhandbook.category.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.category.dto.CategoryResponse;
import com.rra.taxhandbook.category.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class PublicCategoryController {

	private final CategoryService categoryService;

	public PublicCategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public List<CategoryResponse> getCategories() {
		return categoryService.getCategories();
	}
}
