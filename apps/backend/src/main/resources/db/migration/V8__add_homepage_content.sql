create table homepage_contents (
    id bigserial primary key,
    status varchar(32) not null,
    updated_at timestamp with time zone not null
);

create table homepage_content_translations (
    id bigserial primary key,
    homepage_content_id bigint not null references homepage_contents(id) on delete cascade,
    locale varchar(16) not null,
    kicker varchar(255) not null,
    title varchar(255) not null,
    subtitle varchar(2000) not null,
    search_label varchar(120) not null,
    help_label varchar(120) not null,
    constraint uk_homepage_content_translation unique (homepage_content_id, locale)
);

create table homepage_cards (
    id bigserial primary key,
    homepage_content_id bigint not null references homepage_contents(id) on delete cascade,
    section_id bigint not null references content_sections(id) on delete restrict,
    sort_order integer not null
);

create table homepage_card_translations (
    id bigserial primary key,
    homepage_card_id bigint not null references homepage_cards(id) on delete cascade,
    locale varchar(16) not null,
    title varchar(255) not null,
    description varchar(2000) not null,
    constraint uk_homepage_card_translation unique (homepage_card_id, locale)
);
