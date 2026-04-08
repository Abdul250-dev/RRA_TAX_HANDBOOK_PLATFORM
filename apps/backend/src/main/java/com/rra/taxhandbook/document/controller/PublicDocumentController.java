package com.rra.taxhandbook.document.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.document.dto.DocumentResponse;
import com.rra.taxhandbook.document.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class PublicDocumentController {

	private final DocumentService documentService;

	public PublicDocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@GetMapping
	public List<DocumentResponse> getDocuments() {
		return documentService.getDocuments();
	}
}
