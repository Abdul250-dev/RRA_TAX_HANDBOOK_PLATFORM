package com.rra.taxhandbook.document.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.document.dto.DocumentRequest;
import com.rra.taxhandbook.document.dto.DocumentResponse;
import com.rra.taxhandbook.document.service.DocumentService;

@RestController
@RequestMapping("/api/admin/documents")
public class AdminDocumentController {

	private final DocumentService documentService;

	public AdminDocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping
	public ApiResponse<DocumentResponse> createDocument(@RequestBody DocumentRequest request) {
		return documentService.createDocument(request);
	}
}
