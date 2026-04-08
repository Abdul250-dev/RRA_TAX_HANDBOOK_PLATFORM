package com.rra.taxhandbook.content.topicblock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockTranslation;

public interface TopicBlockTranslationRepository extends JpaRepository<TopicBlockTranslation, Long> {
	List<TopicBlockTranslation> findByTopicBlock_Topic_IdAndLocaleOrderByTopicBlock_SortOrderAsc(Long topicId, LanguageCode locale);
	List<TopicBlockTranslation> findByTopicBlock_Topic_IdAndLocaleAndTopicBlock_StatusOrderByTopicBlock_SortOrderAsc(Long topicId, LanguageCode locale, com.rra.taxhandbook.common.enums.ContentStatus status);
}
