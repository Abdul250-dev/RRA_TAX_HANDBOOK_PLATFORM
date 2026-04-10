package com.rra.taxhandbook.content.topicblock.entity;

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
@Table(name = "content_topic_blocks")
public class TopicBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Enumerated(EnumType.STRING)
	@Column(name = "block_type", nullable = false)
	private TopicBlockType blockType;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentStatus status;

	@Column(name = "anchor_key", nullable = false)
	private String anchorKey;

	@Column(name = "is_highlighted", nullable = false)
	private boolean highlighted;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected TopicBlock() {}

	public TopicBlock(Topic topic, TopicBlockType blockType, Integer sortOrder, ContentStatus status, String anchorKey, boolean highlighted, Instant createdAt, Instant updatedAt) {
		this.topic = topic;
		this.blockType = blockType;
		this.sortOrder = sortOrder;
		this.status = status;
		this.anchorKey = anchorKey;
		this.highlighted = highlighted;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() { return id; }
	public Topic getTopic() { return topic; }
	public TopicBlockType getBlockType() { return blockType; }
	public Integer getSortOrder() { return sortOrder; }
	public ContentStatus getStatus() { return status; }
	public String getAnchorKey() { return anchorKey; }
	public boolean isHighlighted() { return highlighted; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void updateStructure(TopicBlockType blockType, Integer sortOrder, String anchorKey) {
		this.blockType = blockType;
		this.sortOrder = sortOrder;
		this.anchorKey = anchorKey;
	}

	public void changeStatus(ContentStatus status) {
		this.status = status;
	}

	public void touch(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
