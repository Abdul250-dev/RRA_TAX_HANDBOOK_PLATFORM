package com.rra.taxhandbook.content.homepage.entity;

import com.rra.taxhandbook.content.section.entity.Section;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "homepage_cards")
public class HomepageCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "homepage_content_id", nullable = false)
	private HomepageContent homepageContent;

	@ManyToOne(optional = false)
	@JoinColumn(name = "section_id", nullable = false)
	private Section section;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	protected HomepageCard() {}

	public HomepageCard(HomepageContent homepageContent, Section section, Integer sortOrder) {
		this.homepageContent = homepageContent;
		this.section = section;
		this.sortOrder = sortOrder;
	}

	public Long getId() { return id; }
	public HomepageContent getHomepageContent() { return homepageContent; }
	public Section getSection() { return section; }
	public Integer getSortOrder() { return sortOrder; }

	public void update(Section section, Integer sortOrder) {
		this.section = section;
		this.sortOrder = sortOrder;
	}
}
