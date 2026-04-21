package com.rra.taxhandbook.content.workflow;

import java.time.Instant;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.content.topic.entity.Topic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_topic_workflow_history")
public class TopicWorkflowHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TopicWorkflowAction action;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", nullable = false)
	private ContentStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false)
	private ContentStatus toStatus;

	@Column(length = 4000)
	private String comment;

	@Column(name = "performed_by", nullable = false)
	private String performedBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TopicWorkflowHistory() {}

	public TopicWorkflowHistory(Topic topic, TopicWorkflowAction action, ContentStatus fromStatus, ContentStatus toStatus, String comment, String performedBy, Instant createdAt) {
		this.topic = topic;
		this.action = action;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.comment = comment;
		this.performedBy = performedBy;
		this.createdAt = createdAt;
	}

	public Long getId() { return id; }
	public Topic getTopic() { return topic; }
	public TopicWorkflowAction getAction() { return action; }
	public ContentStatus getFromStatus() { return fromStatus; }
	public ContentStatus getToStatus() { return toStatus; }
	public String getComment() { return comment; }
	public String getPerformedBy() { return performedBy; }
	public Instant getCreatedAt() { return createdAt; }
}
