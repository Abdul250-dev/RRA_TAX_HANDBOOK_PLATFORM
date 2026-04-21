package com.rra.taxhandbook.content.homepage.entity;

import java.time.Instant;

import com.rra.taxhandbook.common.enums.ContentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "homepage_contents")
public class HomepageContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentStatus status;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected HomepageContent() {}

	public HomepageContent(ContentStatus status, Instant updatedAt) {
		this.status = status;
		this.updatedAt = updatedAt;
	}

	public Long getId() { return id; }
	public ContentStatus getStatus() { return status; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void changeStatus(ContentStatus status) {
		this.status = status;
	}

	public void touch(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
