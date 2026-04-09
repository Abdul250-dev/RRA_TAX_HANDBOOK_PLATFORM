package com.rra.taxhandbook.content.section.entity;

import java.time.Instant;

import com.rra.taxhandbook.common.enums.ContentStatus;

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
@Table(name = "content_sections")
public class Section {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Section parent;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SectionType type;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentStatus status;

	@Column(name = "icon_key")
	private String iconKey;

	@Column(name = "is_featured", nullable = false)
	private boolean featured;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Section() {}

	public Section(Section parent, SectionType type, Integer sortOrder, ContentStatus status, String iconKey, boolean featured, Instant createdAt, Instant updatedAt) {
		this.parent = parent;
		this.type = type;
		this.sortOrder = sortOrder;
		this.status = status;
		this.iconKey = iconKey;
		this.featured = featured;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() { return id; }
	public Section getParent() { return parent; }
	public SectionType getType() { return type; }
	public Integer getSortOrder() { return sortOrder; }
	public ContentStatus getStatus() { return status; }
	public String getIconKey() { return iconKey; }
	public boolean isFeatured() { return featured; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void updateStructure(Section parent, SectionType type, Integer sortOrder) {
		this.parent = parent;
		this.type = type;
		this.sortOrder = sortOrder;
	}

	public void changeStatus(ContentStatus status) {
		this.status = status;
	}

	public void touch(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
