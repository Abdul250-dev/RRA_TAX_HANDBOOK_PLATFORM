package com.rra.taxhandbook.faq.repository;

import java.util.List;

import com.rra.taxhandbook.faq.entity.FAQTranslation;

public interface FAQTranslationRepository {

	List<FAQTranslation> findAll();
}
