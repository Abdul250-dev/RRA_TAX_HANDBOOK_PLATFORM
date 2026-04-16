package com.rra.taxhandbook.content.topicblock.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockTranslation;

public interface TopicBlockTranslationRepository extends JpaRepository<TopicBlockTranslation, Long> {
	List<TopicBlockTranslation> findByTopicBlock_Topic_IdAndLocaleOrderByTopicBlock_SortOrderAsc(Long topicId, LanguageCode locale);
	List<TopicBlockTranslation> findByTopicBlock_Topic_IdAndLocaleAndTopicBlock_StatusOrderByTopicBlock_SortOrderAsc(Long topicId, LanguageCode locale, com.rra.taxhandbook.common.enums.ContentStatus status);
	Optional<TopicBlockTranslation> findByTopicBlock_IdAndLocale(Long topicBlockId, LanguageCode locale);
	long countByTopicBlock_IdAndLocaleIn(Long topicBlockId, Collection<LanguageCode> locales);
	void deleteByTopicBlock_Id(Long topicBlockId);
	void deleteByTopicBlock_Topic_Id(Long topicId);
}
