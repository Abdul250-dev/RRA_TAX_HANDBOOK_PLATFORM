package com.rra.taxhandbook.content.section.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.content.section.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
	List<Section> findAllByOrderBySortOrderAsc();
}
