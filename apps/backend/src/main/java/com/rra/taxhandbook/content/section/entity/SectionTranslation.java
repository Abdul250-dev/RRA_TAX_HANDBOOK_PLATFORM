package com.rra.taxhandbook.content.section.entity;

import com.rra.taxhandbook.common.enums.LanguageCode;

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
@Table(name = "content_section_translations")
public class SectionTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "section_id", nullable = false)
	private Section section;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LanguageCode locale;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String slug;

	@Column(length = 2000)
	private String summary;

	protected SectionTranslation() {}

	public SectionTranslation(Section section, LanguageCode locale, String name, String slug, String summary) {
		this.section = section;
		this.locale = locale;
		this.name = name;
		this.slug = slug;
		this.summary = summary;
	}

	public Long getId() { return id; }
	public Section getSection() { return section; }
	public LanguageCode getLocale() { return locale; }
	public String getName() { return name; }
	public String getSlug() { return slug; }
	public String getSummary() { return summary; }

	public void update(String name, String slug, String summary) {
		this.name = name;
		this.slug = slug;
		this.summary = summary;
	}
}
