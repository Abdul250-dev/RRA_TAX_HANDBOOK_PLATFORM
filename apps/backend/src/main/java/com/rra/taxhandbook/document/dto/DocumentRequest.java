package com.rra.taxhandbook.document.dto;

public record DocumentRequest(
	String title,
	String fileName,
	String fileUrl
) {
}
