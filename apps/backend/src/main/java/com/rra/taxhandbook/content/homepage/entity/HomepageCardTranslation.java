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
@Table(name = "homepage_card_translations")
public class HomepageCardTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "homepage_card_id", nullable = false)
	private HomepageCard homepageCard;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LanguageCode locale;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false, length = 2000)
	private String description;

	protected HomepageCardTranslation() {}

	public HomepageCardTranslation(HomepageCard homepageCard, LanguageCode locale, String title, String description) {
		this.homepageCard = homepageCard;
		this.locale = locale;
		this.title = title;
		this.description = description;
	}

	public Long getId() { return id; }
	public HomepageCard getHomepageCard() { return homepageCard; }
	public LanguageCode getLocale() { return locale; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }

	public void update(String title, String description) {
		this.title = title;
		this.description = description;
	}
}
