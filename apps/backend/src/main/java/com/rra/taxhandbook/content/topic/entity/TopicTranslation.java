package com.rra.taxhandbook.content.topic.entity;

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
@Table(name = "content_topic_translations")
public class TopicTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LanguageCode locale;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String slug;

	@Column(length = 2000)
	private String summary;

	@Column(name = "intro_text", length = 8000)
	private String introText;

	protected TopicTranslation() {}

	public TopicTranslation(Topic topic, LanguageCode locale, String title, String slug, String summary, String introText) {
		this.topic = topic;
		this.locale = locale;
		this.title = title;
		this.slug = slug;
		this.summary = summary;
		this.introText = introText;
	}

	public Long getId() { return id; }
	public Topic getTopic() { return topic; }
	public LanguageCode getLocale() { return locale; }
	public String getTitle() { return title; }
	public String getSlug() { return slug; }
	public String getSummary() { return summary; }
	public String getIntroText() { return introText; }
}
