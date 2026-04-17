package com.rra.taxhandbook.content.service;

import java.time.Instant;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.section.entity.Section;
import com.rra.taxhandbook.content.section.entity.SectionTranslation;
import com.rra.taxhandbook.content.section.entity.SectionType;
import com.rra.taxhandbook.content.section.repository.SectionRepository;
import com.rra.taxhandbook.content.section.repository.SectionTranslationRepository;
import com.rra.taxhandbook.content.topic.entity.Topic;
import com.rra.taxhandbook.content.topic.entity.TopicTranslation;
import com.rra.taxhandbook.content.topic.entity.TopicType;
import com.rra.taxhandbook.content.topic.repository.TopicRepository;
import com.rra.taxhandbook.content.topic.repository.TopicTranslationRepository;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlock;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockTranslation;
import com.rra.taxhandbook.content.topicblock.entity.TopicBlockType;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockRepository;
import com.rra.taxhandbook.content.topicblock.repository.TopicBlockTranslationRepository;

@Service
public class ContentSeedService {

	private final SectionRepository sectionRepository;
	private final SectionTranslationRepository sectionTranslationRepository;
	private final TopicRepository topicRepository;
	private final TopicTranslationRepository topicTranslationRepository;
	private final TopicBlockRepository topicBlockRepository;
	private final TopicBlockTranslationRepository topicBlockTranslationRepository;

	public ContentSeedService(
		SectionRepository sectionRepository,
		SectionTranslationRepository sectionTranslationRepository,
		TopicRepository topicRepository,
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		TopicBlockTranslationRepository topicBlockTranslationRepository
	) {
		this.sectionRepository = sectionRepository;
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.topicBlockTranslationRepository = topicBlockTranslationRepository;
	}

	@Bean
	ApplicationRunner seedContentStructure() {
		return args -> {
			if (sectionRepository.count() > 0 || topicRepository.count() > 0) {
				return;
			}

			Instant now = Instant.now();
			Section taxes = sectionRepository.save(new Section(null, SectionType.MAIN, 1, ContentStatus.PUBLISHED, "book", true, now, now));
			seedSectionTranslation(taxes, LanguageCode.EN, "Taxes in Rwanda", "taxes", "Find information on the different taxes administered in Rwanda.");
			seedSectionTranslation(taxes, LanguageCode.FR, "Taxes au Rwanda", "taxes", "Consultez les informations sur les differents impots administres au Rwanda.");
			seedSectionTranslation(taxes, LanguageCode.KIN, "Imisoro ikoreshwa mu Rwanda", "imisoro", "Menya amakuru yerekeye imisoro itandukanye ikoreshwa mu Rwanda.");

			Section domestic = sectionRepository.save(new Section(taxes, SectionType.GROUP, 1, ContentStatus.PUBLISHED, "folder", true, now, now));
			seedSectionTranslation(domestic, LanguageCode.EN, "Domestic Taxes", "domestic", "Domestic taxes include central and local government taxes collected within Rwanda.");
			seedSectionTranslation(domestic, LanguageCode.FR, "Impots interieurs", "impots-interieurs", "Les impots interieurs comprennent les impots du gouvernement central et local.");
			seedSectionTranslation(domestic, LanguageCode.KIN, "Imisoro y'imbere mu gihugu", "imisoro-y-imbere-mu-gihugu", "Imisoro y'imbere mu gihugu ikubiyemo imisoro yo ku rwego rw'igihugu n'uturere.");

			Section central = sectionRepository.save(new Section(domestic, SectionType.SUBGROUP, 1, ContentStatus.PUBLISHED, "folder-open", false, now, now));
			seedSectionTranslation(central, LanguageCode.EN, "Central Government Taxes", "central-government", "Taxes collected by RRA on behalf of the central government.");
			seedSectionTranslation(central, LanguageCode.FR, "Impots du gouvernement central", "gouvernement-central", "Impots collectes par la RRA pour le compte du gouvernement central.");
			seedSectionTranslation(central, LanguageCode.KIN, "Imisoro ya Leta Nkuru", "leta-nkuru", "Imisoro ikusanywa na RRA mu izina rya Leta Nkuru.");

			Topic vat = topicRepository.save(new Topic(central, TopicType.TAX_TOPIC, ContentStatus.PUBLISHED, 1, true, true, now, null, now, now));
			seedTopicTranslation(vat, LanguageCode.EN, "Value Added Tax (VAT)", "value-added-tax-vat", "VAT is a tax on the consumption of goods and services.", "VAT is applied on a wide range of products and services. This page brings together registration, obligations, declaration, payment, and penalties.");
			seedTopicTranslation(vat, LanguageCode.FR, "Taxe sur la valeur ajoutee (TVA)", "taxe-sur-la-valeur-ajoutee", "La TVA est un impot sur la consommation des biens et services.", "Cette page rassemble l'inscription, les obligations, la declaration, le paiement et les penalites.");
			seedTopicTranslation(vat, LanguageCode.KIN, "Umusoro ku nyongeragaciro (VAT)", "umusoro-ku-nyongeragaciro", "VAT ni umusoro ucibwa ku ikoreshwa ry'ibicuruzwa na serivisi.", "Uru rupapuro ruhuriza hamwe kwiyandikisha, inshingano, gutanga imenyekanisha, kwishyura n'ibihano.");

			TopicBlock overview = topicBlockRepository.save(new TopicBlock(vat, TopicBlockType.RICH_TEXT, 1, ContentStatus.PUBLISHED, "overview", true, now, now));
			seedTopicBlockTranslation(overview, LanguageCode.EN, "Overview", "VAT is a consumption tax with a standard rate of 18 percent, with zero-rated and exempt categories for specific goods and services.");
			seedTopicBlockTranslation(overview, LanguageCode.FR, "Apercu", "La TVA est un impot sur la consommation avec un taux normal de 18 pour cent, ainsi que des categories a taux zero et des exemptions.");
			seedTopicBlockTranslation(overview, LanguageCode.KIN, "Incamake", "VAT ni umusoro ucibwa ku ikoreshwa ufite igipimo gisanzwe cya 18%, kandi hari ibyiciro bifite 0% cyangwa bisonerwa.");

			TopicBlock obligations = topicBlockRepository.save(new TopicBlock(vat, TopicBlockType.STEP_LIST, 2, ContentStatus.PUBLISHED, "obligations", false, now, now));
			seedTopicBlockTranslation(obligations, LanguageCode.EN, "Obligations of VAT registered taxpayers", "Display the VAT certificate, use EIS or EBM invoices, submit declarations on time, and keep records available for RRA review.");
			seedTopicBlockTranslation(obligations, LanguageCode.FR, "Obligations des contribuables enregistres a la TVA", "Afficher le certificat TVA, utiliser les factures EIS ou EBM, declarer dans les delais et conserver les registres.");
			seedTopicBlockTranslation(obligations, LanguageCode.KIN, "Inshingano z'abanditse kuri VAT", "Garagaza icyemezo cya VAT, koresha inyemezabuguzi za EIS cyangwa EBM, tanga imenyekanisha ku gihe kandi ubike inyandiko.");
		};
	}

	private void seedSectionTranslation(Section section, LanguageCode locale, String name, String slug, String summary) {
		sectionTranslationRepository.save(new SectionTranslation(section, locale, name, slug, summary));
	}

	private void seedTopicTranslation(Topic topic, LanguageCode locale, String title, String slug, String summary, String introText) {
		topicTranslationRepository.save(new TopicTranslation(topic, locale, title, slug, summary, introText));
	}

	private void seedTopicBlockTranslation(TopicBlock topicBlock, LanguageCode locale, String title, String body) {
		topicBlockTranslationRepository.save(new TopicBlockTranslation(topicBlock, locale, title, body));
	}
}
