package com.rra.taxhandbook.content.section.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.section.entity.SectionTranslation;

public interface SectionTranslationRepository extends JpaRepository<SectionTranslation, Long> {
	List<SectionTranslation> findByLocaleOrderBySection_SortOrderAsc(LanguageCode locale);
	List<SectionTranslation> findByLocaleAndSection_StatusOrderBySection_SortOrderAsc(LanguageCode locale, com.rra.taxhandbook.common.enums.ContentStatus status);
	Optional<SectionTranslation> findBySlugAndLocale(String slug, LanguageCode locale);
	Optional<SectionTranslation> findBySection_IdAndLocale(Long sectionId, LanguageCode locale);
	boolean existsBySlugAndLocale(String slug, LanguageCode locale);
	boolean existsBySlugAndLocaleAndSection_IdNot(String slug, LanguageCode locale, Long sectionId);
}
