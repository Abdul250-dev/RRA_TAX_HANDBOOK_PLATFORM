package com.rra.taxhandbook.content.topicblock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.content.topicblock.entity.TopicBlock;

public interface TopicBlockRepository extends JpaRepository<TopicBlock, Long> {
	List<TopicBlock> findByTopic_IdOrderBySortOrderAsc(Long topicId);
	void deleteByTopic_Id(Long topicId);
}
