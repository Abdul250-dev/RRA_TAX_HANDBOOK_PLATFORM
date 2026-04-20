package com.rra.taxhandbook.content.homepage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.homepage.entity.HomepageContentTranslation;

public interface HomepageContentTranslationRepository extends JpaRepository<HomepageContentTranslation, Long> {
	Optional<HomepageContentTranslation> findByHomepageContent_IdAndLocale(Long homepageContentId, LanguageCode locale);
}
