package com.rra.taxhandbook.content.section.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.section.entity.SectionTranslation;

public interface SectionTranslationRepository extends JpaRepository<SectionTranslation, Long> {
	List<SectionTranslation> findByLocaleOrderBySection_SortOrderAsc(LanguageCode locale);
	List<SectionTranslation> findByLocaleAndSection_StatusOrderBySection_SortOrderAsc(LanguageCode locale, com.rra.taxhandbook.common.enums.ContentStatus status);
	List<SectionTranslation> findBySection_Parent_IdAndLocaleAndSection_StatusOrderBySection_SortOrderAsc(
		Long parentId,
		LanguageCode locale,
		com.rra.taxhandbook.common.enums.ContentStatus status
	);
	Optional<SectionTranslation> findBySlugAndLocale(String slug, LanguageCode locale);
	Optional<SectionTranslation> findFirstByNameIgnoreCaseAndLocale(String name, LanguageCode locale);
	Optional<SectionTranslation> findBySlugAndLocaleAndSection_Status(
		String slug,
		LanguageCode locale,
		com.rra.taxhandbook.common.enums.ContentStatus status
	);
	Optional<SectionTranslation> findBySection_IdAndLocale(Long sectionId, LanguageCode locale);
	long countBySection_IdAndLocaleIn(Long sectionId, Collection<LanguageCode> locales);
	boolean existsBySlugAndLocale(String slug, LanguageCode locale);
	boolean existsBySlugAndLocaleAndSection_IdNot(String slug, LanguageCode locale, Long sectionId);
}
