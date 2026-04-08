package com.rra.taxhandbook.document.repository;

import java.util.List;

import com.rra.taxhandbook.document.entity.Document;

public interface DocumentRepository {

	List<Document> findAll();
}
