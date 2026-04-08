package com.rra.taxhandbook.document.dto;

public record DocumentResponse(
	Long id,
	String title,
	String fileName,
	String fileUrl
) {
}
