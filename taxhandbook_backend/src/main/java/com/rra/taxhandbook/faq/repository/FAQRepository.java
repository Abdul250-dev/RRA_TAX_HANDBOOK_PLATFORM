package com.rra.taxhandbook.faq.repository;

import java.util.List;

import com.rra.taxhandbook.faq.entity.FAQ;

public interface FAQRepository {

	List<FAQ> findAll();
}
