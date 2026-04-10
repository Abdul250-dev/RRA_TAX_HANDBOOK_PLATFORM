package com.rra.taxhandbook.content.topicblock.entity;

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
@Table(name = "content_topic_block_translations")
public class TopicBlockTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "topic_block_id", nullable = false)
	private TopicBlock topicBlock;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LanguageCode locale;

	@Column(nullable = false)
	private String title;

	@Column(length = 12000)
	private String body;

	protected TopicBlockTranslation() {}

	public TopicBlockTranslation(TopicBlock topicBlock, LanguageCode locale, String title, String body) {
		this.topicBlock = topicBlock;
		this.locale = locale;
		this.title = title;
		this.body = body;
	}

	public Long getId() { return id; }
	public TopicBlock getTopicBlock() { return topicBlock; }
	public LanguageCode getLocale() { return locale; }
	public String getTitle() { return title; }
	public String getBody() { return body; }

	public void update(String title, String body) {
		this.title = title;
		this.body = body;
	}
}
