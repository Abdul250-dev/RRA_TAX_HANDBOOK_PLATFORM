package com.rra.taxhandbook.content.topicblock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.content.topicblock.entity.TopicBlock;

public interface TopicBlockRepository extends JpaRepository<TopicBlock, Long> {
}
