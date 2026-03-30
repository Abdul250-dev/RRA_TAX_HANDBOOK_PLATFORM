package com.rra.taxhandbook.category.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.category.dto.CategoryRequest;
import com.rra.taxhandbook.category.dto.CategoryResponse;
import com.rra.taxhandbook.category.service.CategoryService;
import com.rra.taxhandbook.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public List<CategoryResponse> getCategories() {
		return categoryService.getCategories();
	}

	@PostMapping
	public ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
		return categoryService.createCategory(request);
	}
}
