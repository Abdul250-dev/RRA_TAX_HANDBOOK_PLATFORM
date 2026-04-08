package com.rra.taxhandbook.document.entity;

public record Document(
	Long id,
	String title,
	String fileName,
	String fileUrl
) {
}
