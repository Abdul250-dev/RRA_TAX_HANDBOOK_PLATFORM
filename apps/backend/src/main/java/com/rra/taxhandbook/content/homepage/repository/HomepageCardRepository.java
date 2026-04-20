package com.rra.taxhandbook.content.homepage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.content.homepage.entity.HomepageCard;

public interface HomepageCardRepository extends JpaRepository<HomepageCard, Long> {
	Optional<HomepageCard> findByHomepageContent_IdAndSection_Id(Long homepageContentId, Long sectionId);
}
