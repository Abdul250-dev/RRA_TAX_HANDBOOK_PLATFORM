package com.rra.taxhandbook.document.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.document.dto.DocumentRequest;
import com.rra.taxhandbook.document.dto.DocumentResponse;

@Service
public class DocumentService {

	public List<DocumentResponse> getDocuments() {
		return List.of(new DocumentResponse(1L, "Tax Handbook 2025", "RRA_Tax_Handbook_2025_Final.pdf", "/documents/tax-handbook-2025"));
	}

	public ApiResponse<DocumentResponse> createDocument(DocumentRequest request) {
		DocumentResponse response = new DocumentResponse(2L, request.title(), request.fileName(), request.fileUrl());
		return new ApiResponse<>("Document scaffold created", response);
	}
}
