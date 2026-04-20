package com.rra.taxhandbook.content.homepage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.homepage.entity.HomepageCardTranslation;

public interface HomepageCardTranslationRepository extends JpaRepository<HomepageCardTranslation, Long> {
	List<HomepageCardTranslation> findByHomepageCard_HomepageContent_IdAndLocaleOrderByHomepageCard_SortOrderAsc(Long homepageContentId, LanguageCode locale);
}
