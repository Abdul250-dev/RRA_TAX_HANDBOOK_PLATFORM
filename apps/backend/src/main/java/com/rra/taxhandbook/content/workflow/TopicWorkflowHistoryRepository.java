package com.rra.taxhandbook.content.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicWorkflowHistoryRepository extends JpaRepository<TopicWorkflowHistory, Long> {
	List<TopicWorkflowHistory> findByTopic_IdOrderByCreatedAtDesc(Long topicId);
	void deleteByTopic_Id(Long topicId);
}
