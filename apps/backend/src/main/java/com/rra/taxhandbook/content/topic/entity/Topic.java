package com.rra.taxhandbook.content.topic.entity;

import java.time.Instant;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.content.section.entity.Section;

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
@Table(name = "content_topics")
public class Topic {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "section_id", nullable = false)
	private Section section;

	@Enumerated(EnumType.STRING)
	@Column(name = "topic_type", nullable = false)
	private TopicType topicType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentStatus status;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Column(name = "is_featured", nullable = false)
	private boolean featured;

	@Column(name = "show_in_navigation", nullable = false)
	private boolean showInNavigation;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Topic() {}

	public Topic(Section section, TopicType topicType, ContentStatus status, Integer sortOrder, boolean featured, boolean showInNavigation, Instant publishedAt, Instant createdAt, Instant updatedAt) {
		this.section = section;
		this.topicType = topicType;
		this.status = status;
		this.sortOrder = sortOrder;
		this.featured = featured;
		this.showInNavigation = showInNavigation;
		this.publishedAt = publishedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() { return id; }
	public Section getSection() { return section; }
	public TopicType getTopicType() { return topicType; }
	public ContentStatus getStatus() { return status; }
	public Integer getSortOrder() { return sortOrder; }
	public boolean isFeatured() { return featured; }
	public boolean isShowInNavigation() { return showInNavigation; }
	public Instant getPublishedAt() { return publishedAt; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void updateStructure(Section section, TopicType topicType, Integer sortOrder) {
		this.section = section;
		this.topicType = topicType;
		this.sortOrder = sortOrder;
	}

	public void changeStatus(ContentStatus status) {
		this.status = status;
	}

	public void publishNow(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public void touch(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
