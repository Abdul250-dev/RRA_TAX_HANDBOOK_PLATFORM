package com.rra.taxhandbook.content.topic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;

public interface TopicTranslationRepository extends JpaRepository<TopicTranslation, Long> {
	Optional<TopicTranslation> findBySlugAndLocale(String slug, LanguageCode locale);
	Optional<TopicTranslation> findBySlugAndLocaleAndTopic_Status(String slug, LanguageCode locale, com.rra.taxhandbook.common.enums.ContentStatus status);
	Optional<TopicTranslation> findFirstByTopic_IdOrderByIdAsc(Long topicId);
}
