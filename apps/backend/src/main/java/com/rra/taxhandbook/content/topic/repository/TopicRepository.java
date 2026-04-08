package com.rra.taxhandbook.content.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.content.topic.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
