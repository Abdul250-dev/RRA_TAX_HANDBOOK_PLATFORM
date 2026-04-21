insert into content_sections (parent_id, type, sort_order, status, icon_key, is_featured, created_at, updated_at)
select null, 'MAIN', 5, 'PUBLISHED', 'phone', true, current_timestamp, current_timestamp
where not exists (
    select 1
    from content_section_translations
    where slug = 'contact'
      and locale = 'EN'
);

insert into content_section_translations (section_id, locale, name, slug, summary)
select section_id, locale, name, slug, summary
from (
    select s.id as section_id, 'EN' as locale, 'Contact' as name, 'contact' as slug,
           'Find official RRA contact details, taxpayer support channels, and tax centre information.' as summary
    from content_sections s
    where s.id = (
        select max(cs.id)
        from content_sections cs
        where cs.icon_key = 'phone'
          and cs.sort_order = 5
    )
    union all
    select s.id, 'FR', 'Contact', 'contact',
           'Trouvez les coordonnees officielles de la RRA, les canaux d''assistance aux contribuables et les centres fiscaux.'
    from content_sections s
    where s.id = (
        select max(cs.id)
        from content_sections cs
        where cs.icon_key = 'phone'
          and cs.sort_order = 5
    )
    union all
    select s.id, 'KIN', 'Twandikire', 'twandikire',
           'Menya amakuru yo kuvugana na RRA, inzira z''ubufasha ku basora n''ibiro by''imisoro.'
    from content_sections s
    where s.id = (
        select max(cs.id)
        from content_sections cs
        where cs.icon_key = 'phone'
          and cs.sort_order = 5
    )
) seed
where not exists (
    select 1
    from content_section_translations existing
    where existing.slug = seed.slug
      and existing.locale = seed.locale
);

insert into content_topics (section_id, topic_type, status, sort_order, is_featured, show_in_navigation, published_at, scheduled_publish_at, created_at, updated_at)
select section_id, 'STATIC_TOPIC', 'PUBLISHED', 1, true, true, current_timestamp, null, current_timestamp, current_timestamp
from content_section_translations
where slug = 'contact'
  and locale = 'EN'
  and not exists (
      select 1
      from content_topic_translations
      where slug = 'rra-contact-details'
        and locale = 'EN'
  );

insert into content_topic_translations (topic_id, locale, title, slug, summary, intro_text)
select topic_id, locale, title, slug, summary, intro_text
from (
    select t.id as topic_id, 'EN' as locale, 'RRA contact details' as title, 'rra-contact-details' as slug,
           'Official channels for reaching Rwanda Revenue Authority.' as summary,
           'Use the official RRA service channels for taxpayer assistance, service questions, and follow-up on tax handbook guidance.' as intro_text
    from content_topics t
    join content_section_translations s on s.section_id = t.section_id
    where s.slug = 'contact'
      and s.locale = 'EN'
      and t.sort_order = 1
    union all
    select t.id, 'FR', 'Coordonnees de la RRA', 'coordonnees-rra',
           'Canaux officiels pour contacter l''Office Rwandais des Recettes.',
           'Utilisez les canaux officiels de la RRA pour l''assistance aux contribuables, les questions de service et le suivi des orientations du guide fiscal.'
    from content_topics t
    join content_section_translations s on s.section_id = t.section_id
    where s.slug = 'contact'
      and s.locale = 'EN'
      and t.sort_order = 1
    union all
    select t.id, 'KIN', 'Uko wavugana na RRA', 'uko-wavugana-na-rra',
           'Inzira zemewe zo kuvugana n''Ikigo cy''Igihugu cy''Imisoro n''Amahoro.',
           'Koresha inzira zemewe za RRA mu gusaba ubufasha, kubaza ibibazo bya serivisi no gukurikirana ubuyobozi bwo mu gitabo cy''imisoro.'
    from content_topics t
    join content_section_translations s on s.section_id = t.section_id
    where s.slug = 'contact'
      and s.locale = 'EN'
      and t.sort_order = 1
) seed
where not exists (
    select 1
    from content_topic_translations existing
    where existing.slug = seed.slug
      and existing.locale = seed.locale
);

insert into content_topic_blocks (topic_id, block_type, sort_order, status, anchor_key, is_highlighted, created_at, updated_at)
select topic_id, 'INFO_CARD', 1, 'PUBLISHED', 'contact-channels', true, current_timestamp, current_timestamp
from content_topic_translations
where slug = 'rra-contact-details'
  and locale = 'EN'
  and not exists (
      select 1
      from content_topic_blocks
      where anchor_key = 'contact-channels'
  );

insert into content_topic_block_translations (topic_block_id, locale, title, body)
select block_id, locale, title, body
from (
    select b.id as block_id, 'EN' as locale, 'Taxpayer support channels' as title,
           'RRA support content is managed from the CMS so public contact details can be updated without a code release. Add phone numbers, email addresses, office hours, and service links here when confirmed by the business owner.' as body
    from content_topic_blocks b
    where b.anchor_key = 'contact-channels'
    union all
    select b.id, 'FR', 'Canaux d''assistance aux contribuables',
           'Le contenu d''assistance RRA est gere depuis le CMS afin que les coordonnees publiques puissent etre mises a jour sans livraison de code. Ajoutez ici les numeros, e-mails, horaires et liens de service apres confirmation.'
    from content_topic_blocks b
    where b.anchor_key = 'contact-channels'
    union all
    select b.id, 'KIN', 'Inzira z''ubufasha ku basora',
           'Amakuru y''ubufasha bwa RRA acungwa muri CMS kugira ngo avugururwe bitabaye ngombwa guhindura code. Ongeramo telefoni, email, amasaha y''akazi n''amahuza ya serivisi byemejwe.'
    from content_topic_blocks b
    where b.anchor_key = 'contact-channels'
) seed
where not exists (
    select 1
    from content_topic_block_translations existing
    where existing.topic_block_id = seed.block_id
      and existing.locale = seed.locale
);

insert into content_topics (section_id, topic_type, status, sort_order, is_featured, show_in_navigation, published_at, scheduled_publish_at, created_at, updated_at)
select section_id, 'STATIC_TOPIC', 'PUBLISHED', 2, false, true, current_timestamp, null, current_timestamp, current_timestamp
from content_section_translations
where slug = 'contact'
  and locale = 'EN'
  and not exists (
      select 1
      from content_topic_translations
      where slug = 'tax-centres'
        and locale = 'EN'
  );

insert into content_topic_translations (topic_id, locale, title, slug, summary, intro_text)
select topic_id, locale, title, slug, summary, intro_text
from (
    select t.id as topic_id, 'EN' as locale, 'Tax centres' as title, 'tax-centres' as slug,
           'Information about RRA tax centres and taxpayer service locations.' as summary,
           'Use this page for CMS-managed tax centre details, service coverage, addresses, and taxpayer visit guidance.' as intro_text
    from content_topics t
    join content_section_translations s on s.section_id = t.section_id
    where s.slug = 'contact'
      and s.locale = 'EN'
      and t.sort_order = 2
    union all
    select t.id, 'FR', 'Centres fiscaux', 'centres-fiscaux',
           'Informations sur les centres fiscaux de la RRA et les lieux de service aux contribuables.',
           'Utilisez cette page pour gerer dans le CMS les centres fiscaux, les zones de service, les adresses et les conseils de visite.'
    from content_topics t
    join content_section_translations s on s.section_id = t.section_id
    where s.slug = 'contact'
      and s.locale = 'EN'
      and t.sort_order = 2
    union all
    select t.id, 'KIN', 'Ibiro by''imisoro', 'ibiro-by-imisoro',
           'Amakuru yerekeye ibiro by''imisoro bya RRA n''aho abasora bahererwa serivisi.',
           'Koresha uru rupapuro mu gucunga amakuru y''ibiro by''imisoro, aho bitanga serivisi, aderesi n''inama ku basora babigana.'
    from content_topics t
    join content_section_translations s on s.section_id = t.section_id
    where s.slug = 'contact'
      and s.locale = 'EN'
      and t.sort_order = 2
) seed
where not exists (
    select 1
    from content_topic_translations existing
    where existing.slug = seed.slug
      and existing.locale = seed.locale
);

insert into content_topic_blocks (topic_id, block_type, sort_order, status, anchor_key, is_highlighted, created_at, updated_at)
select topic_id, 'RICH_TEXT', 1, 'PUBLISHED', 'tax-centre-guidance', false, current_timestamp, current_timestamp
from content_topic_translations
where slug = 'tax-centres'
  and locale = 'EN'
  and not exists (
      select 1
      from content_topic_blocks
      where anchor_key = 'tax-centre-guidance'
  );

insert into content_topic_block_translations (topic_block_id, locale, title, body)
select block_id, locale, title, body
from (
    select b.id as block_id, 'EN' as locale, 'Before visiting a tax centre' as title,
           'Confirm the service you need, prepare your taxpayer identification details, and check the official RRA channels for the latest office information before visiting.' as body
    from content_topic_blocks b
    where b.anchor_key = 'tax-centre-guidance'
    union all
    select b.id, 'FR', 'Avant de vous rendre dans un centre fiscal',
           'Confirmez le service dont vous avez besoin, preparez vos informations d''identification fiscale et consultez les canaux officiels de la RRA avant votre visite.'
    from content_topic_blocks b
    where b.anchor_key = 'tax-centre-guidance'
    union all
    select b.id, 'KIN', 'Mbere yo kujya ku biro by''imisoro',
           'Banza wemeze serivisi ukeneye, utegure amakuru akuranga nk''umusora kandi urebe amakuru agezweho ku nzira zemewe za RRA mbere yo kuhagera.'
    from content_topic_blocks b
    where b.anchor_key = 'tax-centre-guidance'
) seed
where not exists (
    select 1
    from content_topic_block_translations existing
    where existing.topic_block_id = seed.block_id
      and existing.locale = seed.locale
);
