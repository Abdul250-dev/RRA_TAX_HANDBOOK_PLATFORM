package com.rra.taxhandbook.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.category.dto.CategoryRequest;
import com.rra.taxhandbook.category.dto.CategoryResponse;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.util.SlugUtil;

@Service
public class CategoryService {

	public List<CategoryResponse> getCategories() {
		return List.of(new CategoryResponse(1L, "Income Tax", "income-tax", LanguageCode.EN));
	}

	public ApiResponse<CategoryResponse> createCategory(CategoryRequest request) {
		CategoryResponse response = new CategoryResponse(2L, request.name(), SlugUtil.toSlug(request.name()), request.language());
		return new ApiResponse<>("Category scaffold created", response);
	}
}
