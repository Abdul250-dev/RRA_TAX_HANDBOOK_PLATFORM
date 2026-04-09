package com.rra.taxhandbook.content.topic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.content.topic.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
	List<Topic> findByStatusOrderByUpdatedAtDesc(ContentStatus status);
}
