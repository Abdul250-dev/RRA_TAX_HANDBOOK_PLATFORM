package com.rra.taxhandbook.category.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.category.dto.CategoryRequest;
import com.rra.taxhandbook.category.dto.CategoryResponse;
import com.rra.taxhandbook.category.service.CategoryService;
import com.rra.taxhandbook.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

	private final CategoryService categoryService;

	public AdminCategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
		return categoryService.createCategory(request);
	}
}
