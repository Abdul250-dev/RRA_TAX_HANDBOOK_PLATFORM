package com.rra.taxhandbook.content.homepage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.content.homepage.entity.HomepageContent;

public interface HomepageContentRepository extends JpaRepository<HomepageContent, Long> {
	Optional<HomepageContent> findFirstByStatusOrderByUpdatedAtDesc(ContentStatus status);
	Optional<HomepageContent> findFirstByOrderByUpdatedAtDesc();
}
