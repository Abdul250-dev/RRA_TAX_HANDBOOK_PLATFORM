package com.rra.taxhandbook.content.topic.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.topic.entity.TopicType;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;

public interface TopicTranslationRepository extends JpaRepository<TopicTranslation, Long> {
	Optional<TopicTranslation> findBySlugAndLocale(String slug, LanguageCode locale);
	Optional<TopicTranslation> findBySlugAndLocaleAndTopic_Status(String slug, LanguageCode locale, com.rra.taxhandbook.common.enums.ContentStatus status);
	Optional<TopicTranslation> findFirstByTopic_IdOrderByIdAsc(Long topicId);
	Optional<TopicTranslation> findByTopic_IdAndLocale(Long topicId, LanguageCode locale);
	long countByTopic_IdAndLocaleIn(Long topicId, Collection<LanguageCode> locales);
	boolean existsBySlugAndLocale(String slug, LanguageCode locale);
	boolean existsBySlugAndLocaleAndTopic_IdNot(String slug, LanguageCode locale, Long topicId);
	void deleteByTopic_Id(Long topicId);

	@Query("""
		select tt
		from TopicTranslation tt
		where tt.locale = :locale
		and (:status is null or tt.topic.status = :status)
		order by tt.topic.updatedAt desc
	""")
	List<TopicTranslation> findForAdminList(@Param("locale") LanguageCode locale, @Param("status") ContentStatus status);

	List<TopicTranslation> findByTopic_Section_IdAndLocaleAndTopic_StatusOrderByTopic_SortOrderAsc(
		Long sectionId,
		LanguageCode locale,
		ContentStatus status
	);

	List<TopicTranslation> findByTopic_Section_IdAndLocaleAndTopic_StatusAndTopic_TopicTypeAndTopic_IdNotOrderByTopic_SortOrderAsc(
		Long sectionId,
		LanguageCode locale,
		ContentStatus status,
		TopicType topicType,
		Long topicId
	);

	List<TopicTranslation> findByLocaleAndTopic_StatusAndTopic_TopicTypeOrderByTopic_SortOrderAsc(
		LanguageCode locale,
		ContentStatus status,
		TopicType topicType
	);

	@Query("""
		select tt
		from TopicTranslation tt
		left join TopicBlock tb on tb.topic = tt.topic and tb.status = :status
		left join TopicBlockTranslation tbt on tbt.topicBlock = tb and tbt.locale = :locale
		where tt.locale = :locale
		and tt.topic.status = :status
		and (
			lower(tt.title) like lower(concat('%', :query, '%'))
			or lower(tt.summary) like lower(concat('%', :query, '%'))
			or lower(tt.introText) like lower(concat('%', :query, '%'))
			or lower(tbt.title) like lower(concat('%', :query, '%'))
			or lower(tbt.body) like lower(concat('%', :query, '%'))
		)
	""")
	List<TopicTranslation> searchPublishedByLocale(
		@Param("locale") LanguageCode locale,
		@Param("status") ContentStatus status,
		@Param("query") String query
	);

	Optional<TopicTranslation> findBySlugAndLocaleAndTopic_StatusAndTopic_TopicType(
		String slug,
		LanguageCode locale,
		ContentStatus status,
		TopicType topicType
	);
}
