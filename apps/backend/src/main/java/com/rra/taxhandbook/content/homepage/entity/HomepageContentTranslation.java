package com.rra.taxhandbook.content.homepage.entity;

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
@Table(name = "homepage_content_translations")
public class HomepageContentTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "homepage_content_id", nullable = false)
	private HomepageContent homepageContent;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LanguageCode locale;

	@Column(nullable = false, length = 255)
	private String kicker;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false, length = 2000)
	private String subtitle;

	@Column(name = "search_label", nullable = false, length = 120)
	private String searchLabel;

	@Column(name = "help_label", nullable = false, length = 120)
	private String helpLabel;

	protected HomepageContentTranslation() {}

	public HomepageContentTranslation(
		HomepageContent homepageContent,
		LanguageCode locale,
		String kicker,
		String title,
		String subtitle,
		String searchLabel,
		String helpLabel
	) {
		this.homepageContent = homepageContent;
		this.locale = locale;
		this.kicker = kicker;
		this.title = title;
		this.subtitle = subtitle;
		this.searchLabel = searchLabel;
		this.helpLabel = helpLabel;
	}

	public Long getId() { return id; }
	public HomepageContent getHomepageContent() { return homepageContent; }
	public LanguageCode getLocale() { return locale; }
	public String getKicker() { return kicker; }
	public String getTitle() { return title; }
	public String getSubtitle() { return subtitle; }
	public String getSearchLabel() { return searchLabel; }
	public String getHelpLabel() { return helpLabel; }

	public void update(
		String kicker,
		String title,
		String subtitle,
		String searchLabel,
		String helpLabel
	) {
		this.kicker = kicker;
		this.title = title;
		this.subtitle = subtitle;
		this.searchLabel = searchLabel;
		this.helpLabel = helpLabel;
	}
}
