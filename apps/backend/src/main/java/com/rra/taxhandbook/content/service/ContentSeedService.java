package com.rra.taxhandbook.content.service;

import java.time.Instant;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.enums.ContentStatus;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.content.homepage.entity.HomepageCard;
import com.rra.taxhandbook.content.homepage.entity.HomepageCardTranslation;
import com.rra.taxhandbook.content.homepage.entity.HomepageContent;
import com.rra.taxhandbook.content.homepage.entity.HomepageContentTranslation;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageCardTranslationRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentRepository;
import com.rra.taxhandbook.content.homepage.repository.HomepageContentTranslationRepository;
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
	private final HomepageContentRepository homepageContentRepository;
	private final HomepageContentTranslationRepository homepageContentTranslationRepository;
	private final HomepageCardRepository homepageCardRepository;
	private final HomepageCardTranslationRepository homepageCardTranslationRepository;

	public ContentSeedService(
		SectionRepository sectionRepository,
		SectionTranslationRepository sectionTranslationRepository,
		TopicRepository topicRepository,
		TopicTranslationRepository topicTranslationRepository,
		TopicBlockRepository topicBlockRepository,
		TopicBlockTranslationRepository topicBlockTranslationRepository,
		HomepageContentRepository homepageContentRepository,
		HomepageContentTranslationRepository homepageContentTranslationRepository,
		HomepageCardRepository homepageCardRepository,
		HomepageCardTranslationRepository homepageCardTranslationRepository
	) {
		this.sectionRepository = sectionRepository;
		this.sectionTranslationRepository = sectionTranslationRepository;
		this.topicRepository = topicRepository;
		this.topicTranslationRepository = topicTranslationRepository;
		this.topicBlockRepository = topicBlockRepository;
		this.topicBlockTranslationRepository = topicBlockTranslationRepository;
		this.homepageContentRepository = homepageContentRepository;
		this.homepageContentTranslationRepository = homepageContentTranslationRepository;
		this.homepageCardRepository = homepageCardRepository;
		this.homepageCardTranslationRepository = homepageCardTranslationRepository;
	}

	@Bean
	ApplicationRunner seedContentStructure() {
		return args -> {
			if (sectionRepository.count() > 0 || topicRepository.count() > 0) {
				seedHomepageForExistingContentIfMissing();
				return;
			}

			Instant now = Instant.now();
			Section generalInformation = sectionRepository.save(new Section(null, SectionType.MAIN, 1, ContentStatus.PUBLISHED, "book", true, now, now));
			seedSectionTranslation(generalInformation, LanguageCode.EN, "General Information", "general-information", "See the tax handbook introduction, purpose of the tax handbook, history of taxation and other general information.");
			seedSectionTranslation(generalInformation, LanguageCode.FR, "Informations generales", "informations-generales", "Consultez l'introduction du guide fiscal, son objectif, l'histoire de la fiscalite et d'autres informations generales.");
			seedSectionTranslation(generalInformation, LanguageCode.KIN, "Amakuru rusange", "amakuru-rusange", "Reba intangiriro y'igitabo cy'imisoro, intego yacyo, amateka y'imisoro n'andi makuru rusange.");

			Section taxes = sectionRepository.save(new Section(null, SectionType.MAIN, 2, ContentStatus.PUBLISHED, "book", true, now, now));
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

			Section otherServices = sectionRepository.save(new Section(null, SectionType.MAIN, 3, ContentStatus.PUBLISHED, "folder", true, now, now));
			seedSectionTranslation(otherServices, LanguageCode.EN, "Other services", "other-services", "Find information on other tax-related services offered by RRA.");
			seedSectionTranslation(otherServices, LanguageCode.FR, "Autres services", "autres-services", "Consultez les informations sur d'autres services fiscaux proposes par la RRA.");
			seedSectionTranslation(otherServices, LanguageCode.KIN, "Izindi serivisi", "izindi-serivisi", "Menya amakuru ku zindi serivisi zijyanye n'imisoro zitangwa na RRA.");

			Section guides = sectionRepository.save(new Section(null, SectionType.MAIN, 4, ContentStatus.PUBLISHED, "folder", true, now, now));
			seedSectionTranslation(guides, LanguageCode.EN, "User guides and examples", "guides", "Find user guides and examples to help you understand different tax types.");
			seedSectionTranslation(guides, LanguageCode.FR, "Guides utilisateurs et exemples", "guides", "Trouvez des guides utilisateurs et des exemples pour mieux comprendre les differents types d'impots.");
			seedSectionTranslation(guides, LanguageCode.KIN, "Ubuyobozi n'ingero", "ubuyobozi-n-ingero", "Shakamo ubuyobozi n'ingero bigufasha gusobanukirwa ubwoko butandukanye bw'imisoro.");

			seedContactContent(now);

			HomepageContent homepageContent = homepageContentRepository.save(new HomepageContent(ContentStatus.PUBLISHED, now));
			seedHomepageTranslation(homepageContent, LanguageCode.EN, "Rwanda Revenue Authority", "Tax Handbook", "Explore the official public handbook for tax information, services, procedures, and practical guidance across Rwanda's tax system.", "Search handbook", "Get help");
			seedHomepageTranslation(homepageContent, LanguageCode.FR, "Office Rwandais des Recettes", "Guide fiscal", "Explorez le guide public officiel pour les informations fiscales, les services, les procedures et les orientations pratiques du systeme fiscal rwandais.", "Rechercher", "Obtenir de l'aide");
			seedHomepageTranslation(homepageContent, LanguageCode.KIN, "Ikigo cy'Igihugu cy'Imisoro n'Amahoro", "Igitabo cy'Imisoro", "Sura igitabo rusange gitanga amakuru ku misoro, serivisi, inzira n'ubuyobozi bufasha gusobanukirwa gahunda y'imisoro mu Rwanda.", "Shakisha", "Saba ubufasha");

			seedHomepageCard(homepageContent, generalInformation, 1, LanguageCode.EN, "General Information", "See the tax handbook introduction, purpose of the tax handbook, history of taxation and other general information.");
			seedHomepageCard(homepageContent, generalInformation, 1, LanguageCode.FR, "Informations generales", "Consultez l'introduction du guide fiscal, son objectif, l'histoire de la fiscalite et d'autres informations generales.");
			seedHomepageCard(homepageContent, generalInformation, 1, LanguageCode.KIN, "Amakuru rusange", "Reba intangiriro y'igitabo cy'imisoro, intego yacyo, amateka y'imisoro n'andi makuru rusange.");

			seedHomepageCard(homepageContent, taxes, 2, LanguageCode.EN, "Taxes administered in Rwanda", "Find information on different taxes, their rates, how to file and pay taxes and non-compliance penalties.");
			seedHomepageCard(homepageContent, taxes, 2, LanguageCode.FR, "Taxes administrees au Rwanda", "Consultez les informations sur les differents impots, leurs taux, les declarations, les paiements et les penalites.");
			seedHomepageCard(homepageContent, taxes, 2, LanguageCode.KIN, "Imisoro ikoreshwa mu Rwanda", "Menya amakuru ku misoro itandukanye, ibipimo byayo, uburyo bwo kuyitangaza, kuyishyura n'ibihano.");

			seedHomepageCard(homepageContent, otherServices, 3, LanguageCode.EN, "Other services", "Find information on other tax-related services such VAT Rewards& Refund, Debt Management, Audit, Certificates offered by RRA and Motor Vehicle Services among others.");
			seedHomepageCard(homepageContent, otherServices, 3, LanguageCode.FR, "Autres services", "Consultez les informations sur d'autres services fiscaux tels que les remboursements de TVA, la gestion des dettes, l'audit et les certificats.");
			seedHomepageCard(homepageContent, otherServices, 3, LanguageCode.KIN, "Izindi serivisi", "Menya amakuru ku zindi serivisi zijyanye n'imisoro nko gusubizwa TVA, gucunga imyenda, igenzura n'impamyabushobozi.");

			seedHomepageCard(homepageContent, guides, 4, LanguageCode.EN, "User guides and examples", "Here you will find user guides on filing different taxes and various examples to help your understanding on various tax types.");
			seedHomepageCard(homepageContent, guides, 4, LanguageCode.FR, "Guides utilisateurs et exemples", "Vous trouverez ici des guides de declaration et divers exemples pour mieux comprendre les differents types d'impots.");
			seedHomepageCard(homepageContent, guides, 4, LanguageCode.KIN, "Ubuyobozi n'ingero", "Aha uzasangamo ubuyobozi bwo gutanga imenyekanisha n'ingero zitandukanye zigufasha gusobanukirwa neza ubwoko bw'imisoro.");
		};
	}

	private void seedHomepageForExistingContentIfMissing() {
		ensureContactContentExists();

		if (homepageContentRepository.count() > 0) {
			return;
		}

		Section generalInformation = findSectionByEnglishSlug("general-information");
		Section taxes = findSectionByEnglishSlug("taxes");
		Section otherServices = findSectionByEnglishSlug("other-services");
		Section guides = findSectionByEnglishSlug("guides");

		if (generalInformation == null || taxes == null || otherServices == null || guides == null) {
			return;
		}

		Instant now = Instant.now();
		HomepageContent homepageContent = homepageContentRepository.save(new HomepageContent(ContentStatus.PUBLISHED, now));
		seedHomepageTranslation(homepageContent, LanguageCode.EN, "Rwanda Revenue Authority", "Tax Handbook", "Explore the official public handbook for tax information, services, procedures, and practical guidance across Rwanda's tax system.", "Search handbook", "Get help");
		seedHomepageTranslation(homepageContent, LanguageCode.FR, "Office Rwandais des Recettes", "Guide fiscal", "Explorez le guide public officiel pour les informations fiscales, les services, les procedures et les orientations pratiques du systeme fiscal rwandais.", "Rechercher", "Obtenir de l'aide");
		seedHomepageTranslation(homepageContent, LanguageCode.KIN, "Ikigo cy'Igihugu cy'Imisoro n'Amahoro", "Igitabo cy'Imisoro", "Sura igitabo rusange gitanga amakuru ku misoro, serivisi, inzira n'ubuyobozi bufasha gusobanukirwa gahunda y'imisoro mu Rwanda.", "Shakisha", "Saba ubufasha");

		seedHomepageCard(homepageContent, generalInformation, 1, LanguageCode.EN, "General Information", "See the tax handbook introduction, purpose of the tax handbook, history of taxation and other general information.");
		seedHomepageCard(homepageContent, generalInformation, 1, LanguageCode.FR, "Informations generales", "Consultez l'introduction du guide fiscal, son objectif, l'histoire de la fiscalite et d'autres informations generales.");
		seedHomepageCard(homepageContent, generalInformation, 1, LanguageCode.KIN, "Amakuru rusange", "Reba intangiriro y'igitabo cy'imisoro, intego yacyo, amateka y'imisoro n'andi makuru rusange.");

		seedHomepageCard(homepageContent, taxes, 2, LanguageCode.EN, "Taxes administered in Rwanda", "Find information on different taxes, their rates, how to file and pay taxes and non-compliance penalties.");
		seedHomepageCard(homepageContent, taxes, 2, LanguageCode.FR, "Taxes administrees au Rwanda", "Consultez les informations sur les differents impots, leurs taux, les declarations, les paiements et les penalites.");
		seedHomepageCard(homepageContent, taxes, 2, LanguageCode.KIN, "Imisoro ikoreshwa mu Rwanda", "Menya amakuru ku misoro itandukanye, ibipimo byayo, uburyo bwo kuyitangaza, kuyishyura n'ibihano.");

		seedHomepageCard(homepageContent, otherServices, 3, LanguageCode.EN, "Other services", "Find information on other tax-related services such VAT Rewards& Refund, Debt Management, Audit, Certificates offered by RRA and Motor Vehicle Services among others.");
		seedHomepageCard(homepageContent, otherServices, 3, LanguageCode.FR, "Autres services", "Consultez les informations sur d'autres services fiscaux tels que les remboursements de TVA, la gestion des dettes, l'audit et les certificats.");
		seedHomepageCard(homepageContent, otherServices, 3, LanguageCode.KIN, "Izindi serivisi", "Menya amakuru ku zindi serivisi zijyanye n'imisoro nko gusubizwa TVA, gucunga imyenda, igenzura n'impamyabushobozi.");

		seedHomepageCard(homepageContent, guides, 4, LanguageCode.EN, "User guides and examples", "Here you will find user guides on filing different taxes and various examples to help your understanding on various tax types.");
		seedHomepageCard(homepageContent, guides, 4, LanguageCode.FR, "Guides utilisateurs et exemples", "Vous trouverez ici des guides de declaration et divers exemples pour mieux comprendre les differents types d'impots.");
		seedHomepageCard(homepageContent, guides, 4, LanguageCode.KIN, "Ubuyobozi n'ingero", "Aha uzasangamo ubuyobozi bwo gutanga imenyekanisha n'ingero zitandukanye zigufasha gusobanukirwa neza ubwoko bw'imisoro.");
	}

	private void ensureContactContentExists() {
		if (findSectionByEnglishSlug("contact") != null) {
			return;
		}
		seedContactContent(Instant.now());
	}

	private void seedContactContent(Instant now) {
		Section contact = sectionRepository.save(new Section(null, SectionType.MAIN, 5, ContentStatus.PUBLISHED, "phone", true, now, now));
		seedSectionTranslation(contact, LanguageCode.EN, "Contact", "contact", "Find official RRA contact details, taxpayer support channels, and tax centre information.");
		seedSectionTranslation(contact, LanguageCode.FR, "Contact", "contact", "Trouvez les coordonnees officielles de la RRA, les canaux d'assistance aux contribuables et les centres fiscaux.");
		seedSectionTranslation(contact, LanguageCode.KIN, "Twandikire", "twandikire", "Menya amakuru yo kuvugana na RRA, inzira z'ubufasha ku basora n'ibiro by'imisoro.");

		Topic contactDetails = topicRepository.save(new Topic(contact, TopicType.STATIC_TOPIC, ContentStatus.PUBLISHED, 1, true, true, now, null, now, now));
		seedTopicTranslation(contactDetails, LanguageCode.EN, "RRA contact details", "rra-contact-details", "Official channels for reaching Rwanda Revenue Authority.", "Use the official RRA service channels for taxpayer assistance, service questions, and follow-up on tax handbook guidance.");
		seedTopicTranslation(contactDetails, LanguageCode.FR, "Coordonnees de la RRA", "coordonnees-rra", "Canaux officiels pour contacter l'Office Rwandais des Recettes.", "Utilisez les canaux officiels de la RRA pour l'assistance aux contribuables, les questions de service et le suivi des orientations du guide fiscal.");
		seedTopicTranslation(contactDetails, LanguageCode.KIN, "Uko wavugana na RRA", "uko-wavugana-na-rra", "Inzira zemewe zo kuvugana n'Ikigo cy'Igihugu cy'Imisoro n'Amahoro.", "Koresha inzira zemewe za RRA mu gusaba ubufasha, kubaza ibibazo bya serivisi no gukurikirana ubuyobozi bwo mu gitabo cy'imisoro.");

		TopicBlock contactChannels = topicBlockRepository.save(new TopicBlock(contactDetails, TopicBlockType.INFO_CARD, 1, ContentStatus.PUBLISHED, "contact-channels", true, now, now));
		seedTopicBlockTranslation(contactChannels, LanguageCode.EN, "Taxpayer support channels", "RRA support content is managed from the CMS so public contact details can be updated without a code release. Add phone numbers, email addresses, office hours, and service links here when confirmed by the business owner.");
		seedTopicBlockTranslation(contactChannels, LanguageCode.FR, "Canaux d'assistance aux contribuables", "Le contenu d'assistance RRA est gere depuis le CMS afin que les coordonnees publiques puissent etre mises a jour sans livraison de code. Ajoutez ici les numeros, e-mails, horaires et liens de service apres confirmation.");
		seedTopicBlockTranslation(contactChannels, LanguageCode.KIN, "Inzira z'ubufasha ku basora", "Amakuru y'ubufasha bwa RRA acungwa muri CMS kugira ngo avugururwe bitabaye ngombwa guhindura code. Ongeramo telefoni, email, amasaha y'akazi n'amahuza ya serivisi byemejwe.");

		Topic taxCentres = topicRepository.save(new Topic(contact, TopicType.STATIC_TOPIC, ContentStatus.PUBLISHED, 2, false, true, now, null, now, now));
		seedTopicTranslation(taxCentres, LanguageCode.EN, "Tax centres", "tax-centres", "Information about RRA tax centres and taxpayer service locations.", "Use this page for CMS-managed tax centre details, service coverage, addresses, and taxpayer visit guidance.");
		seedTopicTranslation(taxCentres, LanguageCode.FR, "Centres fiscaux", "centres-fiscaux", "Informations sur les centres fiscaux de la RRA et les lieux de service aux contribuables.", "Utilisez cette page pour gerer dans le CMS les centres fiscaux, les zones de service, les adresses et les conseils de visite.");
		seedTopicTranslation(taxCentres, LanguageCode.KIN, "Ibiro by'imisoro", "ibiro-by-imisoro", "Amakuru yerekeye ibiro by'imisoro bya RRA n'aho abasora bahererwa serivisi.", "Koresha uru rupapuro mu gucunga amakuru y'ibiro by'imisoro, aho bitanga serivisi, aderesi n'inama ku basora babigana.");

		TopicBlock taxCentreGuidance = topicBlockRepository.save(new TopicBlock(taxCentres, TopicBlockType.RICH_TEXT, 1, ContentStatus.PUBLISHED, "tax-centre-guidance", false, now, now));
		seedTopicBlockTranslation(taxCentreGuidance, LanguageCode.EN, "Before visiting a tax centre", "Confirm the service you need, prepare your taxpayer identification details, and check the official RRA channels for the latest office information before visiting.");
		seedTopicBlockTranslation(taxCentreGuidance, LanguageCode.FR, "Avant de vous rendre dans un centre fiscal", "Confirmez le service dont vous avez besoin, preparez vos informations d'identification fiscale et consultez les canaux officiels de la RRA avant votre visite.");
		seedTopicBlockTranslation(taxCentreGuidance, LanguageCode.KIN, "Mbere yo kujya ku biro by'imisoro", "Banza wemeze serivisi ukeneye, utegure amakuru akuranga nk'umusora kandi urebe amakuru agezweho ku nzira zemewe za RRA mbere yo kuhagera.");
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

	private void seedHomepageTranslation(HomepageContent homepageContent, LanguageCode locale, String kicker, String title, String subtitle, String searchLabel, String helpLabel) {
		homepageContentTranslationRepository.save(
			new HomepageContentTranslation(homepageContent, locale, kicker, title, subtitle, searchLabel, helpLabel)
		);
	}

	private void seedHomepageCard(HomepageContent homepageContent, Section section, Integer sortOrder, LanguageCode locale, String title, String description) {
		HomepageCard storedCard = homepageCardRepository
			.findByHomepageContent_IdAndSection_Id(homepageContent.getId(), section.getId())
			.orElseGet(() -> homepageCardRepository.save(new HomepageCard(homepageContent, section, sortOrder)));
		homepageCardTranslationRepository.save(new HomepageCardTranslation(storedCard, locale, title, description));
	}

	private Section findSectionByEnglishSlug(String slug) {
		return sectionTranslationRepository.findBySlugAndLocale(slug, LanguageCode.EN)
			.map(SectionTranslation::getSection)
			.orElse(null);
	}
}
