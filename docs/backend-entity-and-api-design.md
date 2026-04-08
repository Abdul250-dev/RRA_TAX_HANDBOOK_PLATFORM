# RRA Tax Handbook Backend Entity and API Design

## Purpose

This document defines the recommended backend data model and API design for the RRA Tax Handbook Platform based on:

- the approved public sitemap and page architecture
- the multilingual requirement for English, French, and Kinyarwanda
- the handbook structure and exported design screens

The goal is to support a structured, searchable, multilingual handbook platform rather than a flat collection of pages or PDF files.

## Backend Design Principles

The backend should be:

- multilingual-first
- navigation-aware
- content-structured
- admin-manageable
- search-friendly
- future-proof for workflow and publishing states

## Core Modeling Strategy

The public site architecture is best supported by this content hierarchy:

- `Section`
- `Topic`
- `TopicBlock`
- `Guide`
- `FAQ`
- `Document`

This should be combined with translation tables rather than duplicating whole entities per language.

## Recommended Entities

### 1. Section

Represents a navigational branch or category in the handbook.

Examples:

- General Information
- Taxes in Rwanda
- Domestic Taxes
- Central Government Taxes
- Local Government Taxes
- Other Services
- User Guides and Examples

Recommended fields:

- `id`
- `parent_id` nullable
- `type`
- `sort_order`
- `status`
- `icon_key` nullable
- `is_featured`
- `created_at`
- `updated_at`

Notes:

- `parent_id` supports nesting
- `type` can distinguish major navigation branches from subgrouping sections

### 2. SectionTranslation

Stores localized display content for a section.

Recommended fields:

- `id`
- `section_id`
- `locale` (`en`, `fr`, `kin`)
- `name`
- `slug`
- `summary`
- `seo_title`
- `seo_description`

Constraints:

- unique on `(section_id, locale)`
- unique on `(locale, slug)`

### 3. Topic

Represents a main public content page under a section.

Examples:

- Income Tax
- VAT
- Excise Duty
- Trading License Tax
- Certificates
- Debt Management

Recommended fields:

- `id`
- `section_id`
- `topic_type`
- `status`
- `sort_order`
- `is_featured`
- `show_in_navigation`
- `last_reviewed_at` nullable
- `published_at` nullable
- `created_at`
- `updated_at`

Suggested `topic_type` values:

- `TAX_TOPIC`
- `SERVICE_TOPIC`
- `STATIC_TOPIC`
- `LANDING_TOPIC`

### 4. TopicTranslation

Stores localized topic metadata.

Recommended fields:

- `id`
- `topic_id`
- `locale`
- `title`
- `slug`
- `summary`
- `intro_text`
- `seo_title`
- `seo_description`

Constraints:

- unique on `(topic_id, locale)`
- unique on `(locale, slug)`

### 5. TopicBlock

Represents an ordered content block inside a topic page.

This is the most important entity for preserving handbook structure.

Examples:

- overview
- taxable income
- registration
- obligations
- declaration
- payment
- penalties
- examples

Recommended fields:

- `id`
- `topic_id`
- `block_type`
- `sort_order`
- `status`
- `anchor_key`
- `is_highlighted`
- `created_at`
- `updated_at`

Suggested `block_type` values:

- `RICH_TEXT`
- `ACCORDION`
- `CHECKLIST`
- `STEP_LIST`
- `TABLE`
- `INFO_CARD`
- `FAQ_REFERENCE`
- `DOCUMENT_REFERENCE`
- `RELATED_LINKS`

### 6. TopicBlockTranslation

Stores localized content for each topic block.

Recommended fields:

- `id`
- `topic_block_id`
- `locale`
- `title`
- `body`
- `json_content` nullable

Notes:

- `body` works for standard rich text
- `json_content` supports more structured block rendering for tables, steps, or linked cards

### 7. Guide

Represents step-by-step user guides or examples.

Examples:

- declaring domestic taxes
- using E-Tax
- using M-Declaration
- paying via mobile money

Recommended fields:

- `id`
- `section_id` nullable
- `status`
- `sort_order`
- `published_at` nullable
- `created_at`
- `updated_at`

### 8. GuideTranslation

Recommended fields:

- `id`
- `guide_id`
- `locale`
- `title`
- `slug`
- `summary`
- `body`
- `seo_title`
- `seo_description`

### 9. FAQ

Represents structured question-and-answer content.

Recommended fields:

- `id`
- `section_id` nullable
- `topic_id` nullable
- `sort_order`
- `status`
- `created_at`
- `updated_at`

### 10. FAQTranslation

Recommended fields:

- `id`
- `faq_id`
- `locale`
- `question`
- `answer`

### 11. Document

Represents downloadable files or supporting resources.

Examples:

- handbook PDFs
- forms
- tax tables
- reference documents

Recommended fields:

- `id`
- `topic_id` nullable
- `guide_id` nullable
- `document_type`
- `file_name`
- `file_url`
- `mime_type`
- `file_size`
- `status`
- `published_at` nullable
- `created_at`
- `updated_at`

Suggested `document_type` values:

- `PDF`
- `FORM`
- `REFERENCE`
- `ATTACHMENT`

### 12. DocumentTranslation

Recommended fields:

- `id`
- `document_id`
- `locale`
- `title`
- `description`

### 13. NavigationMenuItem

Optional but recommended if menu ordering and manual curation must differ from section hierarchy.

Recommended fields:

- `id`
- `parent_id` nullable
- `label_source_type`
- `label_source_id`
- `target_type`
- `target_id`
- `sort_order`
- `visibility`

This can be deferred if section ordering alone is sufficient.

## Relationship Summary

Recommended relationships:

- one `Section` has many child `Sections`
- one `Section` has many `Topics`
- one `Section` has many `Guides`
- one `Topic` has many `TopicBlocks`
- one `Topic` has many `FAQs`
- one `Topic` has many `Documents`
- one `Guide` can have many `Documents`

Translation relationships:

- one `Section` has many `SectionTranslations`
- one `Topic` has many `TopicTranslations`
- one `TopicBlock` has many `TopicBlockTranslations`
- one `Guide` has many `GuideTranslations`
- one `FAQ` has many `FAQTranslations`
- one `Document` has many `DocumentTranslations`

## Recommended Content Status Model

Use a shared status model for all managed content:

- `DRAFT`
- `IN_REVIEW`
- `APPROVED`
- `PUBLISHED`
- `ARCHIVED`

This supports the role model already defined for:

- editor
- reviewer
- publisher
- admin

## Locale Model

Use a fixed locale enum:

- `EN`
- `FR`
- `KIN`

Do not store public content as separate per-language root entities. Use translation tables instead.

## Slug Rules

Every translatable public page should have:

- localized slug
- stable internal ID

Slug uniqueness should be enforced per locale, not globally.

## Search Model

Search should index at least:

- section names
- topic titles
- topic summaries
- topic block titles
- topic block body
- guide titles
- guide body
- FAQ questions and answers
- document titles and descriptions

Recommended searchable fields:

- `locale`
- `entity_type`
- `entity_id`
- `title`
- `summary`
- `body_excerpt`
- `keywords` optional
- `section_path`

Suggested search result types:

- `SECTION`
- `TOPIC`
- `GUIDE`
- `FAQ`
- `DOCUMENT`

## Recommended Public API Design

The public API should be content-oriented and locale-aware.

### Locale Handling

Choose one of these two patterns:

1. locale in path
2. locale in query/header

Recommended approach:

- locale in path at frontend layer
- locale in query param between frontend and backend

Example:

- `GET /api/public/sections?locale=en`

This keeps the backend simpler while preserving multilingual routing in the frontend.

## Public API Endpoints

### Sections

- `GET /api/public/sections?locale=en`
- `GET /api/public/sections/{slug}?locale=en`

Purpose:

- homepage navigation
- section landing pages
- nested category navigation

### Topics

- `GET /api/public/topics/{slug}?locale=en`
- `GET /api/public/topics?sectionSlug=domestic-taxes&locale=en`
- `GET /api/public/topics/featured?locale=en`

Purpose:

- topic landing pages
- tax detail pages
- featured content

### Topic Blocks

Usually returned inside topic detail response, not necessarily as a separate endpoint.

### Guides

- `GET /api/public/guides?locale=en`
- `GET /api/public/guides/{slug}?locale=en`
- `GET /api/public/guides?topicSlug=vat&locale=en`

### FAQs

- `GET /api/public/faqs?locale=en`
- `GET /api/public/faqs?topicSlug=income-tax&locale=en`

### Documents

- `GET /api/public/documents?locale=en`
- `GET /api/public/documents?topicSlug=vat&locale=en`

### Search

- `GET /api/public/search?q=vat&locale=en`

Suggested response shape:

- grouped results by type
- highlighted excerpts where possible

## Recommended Public Response Shapes

### Section Detail Response

Should include:

- localized section metadata
- child sections
- featured topics
- related guides

### Topic Detail Response

Should include:

- localized topic metadata
- parent section path
- ordered blocks
- related guides
- related FAQs
- related documents
- last updated date

## Recommended Admin API Design

The admin API should support full content management and translation workflows.

### Sections

- `POST /api/admin/sections`
- `GET /api/admin/sections`
- `GET /api/admin/sections/{id}`
- `PUT /api/admin/sections/{id}`
- `DELETE /api/admin/sections/{id}`

### Section Translations

- `PUT /api/admin/sections/{id}/translations/{locale}`

### Topics

- `POST /api/admin/topics`
- `GET /api/admin/topics`
- `GET /api/admin/topics/{id}`
- `PUT /api/admin/topics/{id}`
- `DELETE /api/admin/topics/{id}`

### Topic Translations

- `PUT /api/admin/topics/{id}/translations/{locale}`

### Topic Blocks

- `POST /api/admin/topics/{id}/blocks`
- `PUT /api/admin/topics/{topicId}/blocks/{blockId}`
- `DELETE /api/admin/topics/{topicId}/blocks/{blockId}`
- `PUT /api/admin/topics/{topicId}/blocks/reorder`

### Guides

- `POST /api/admin/guides`
- `GET /api/admin/guides`
- `PUT /api/admin/guides/{id}`
- `DELETE /api/admin/guides/{id}`

### FAQs

- `POST /api/admin/faqs`
- `GET /api/admin/faqs`
- `PUT /api/admin/faqs/{id}`
- `DELETE /api/admin/faqs/{id}`

### Documents

- `POST /api/admin/documents`
- `GET /api/admin/documents`
- `PUT /api/admin/documents/{id}`
- `DELETE /api/admin/documents/{id}`

### Publish Workflow

- `POST /api/admin/content/{entityType}/{id}/submit-review`
- `POST /api/admin/content/{entityType}/{id}/approve`
- `POST /api/admin/content/{entityType}/{id}/publish`
- `POST /api/admin/content/{entityType}/{id}/archive`

## Recommended DTO Groups

The backend should define DTOs for:

- summary cards
- detail pages
- translation payloads
- search results
- admin forms
- publish workflow transitions

## Minimal First Implementation Scope

For an initial production-ready content core, prioritize:

1. `Section`
2. `SectionTranslation`
3. `Topic`
4. `TopicTranslation`
5. `TopicBlock`
6. `TopicBlockTranslation`
7. `Document`
8. `DocumentTranslation`
9. `FAQ`
10. `FAQTranslation`

`Guide` can either be a separate entity or temporarily modeled as a `Topic` with `topic_type = GUIDE`.

## Recommended First Build Decision

If speed matters, use this pragmatic first phase:

- model `Guide` as a specialized `Topic`
- keep `NavigationMenuItem` out of phase 1
- use translation tables from day one
- keep search simple at first with SQL-backed search

## Security and Workflow Implications

This backend model fits well with the approved role policy:

- `EDITOR` creates and edits content
- `REVIEWER` reviews content
- `PUBLISHER` publishes and archives content
- `ADMIN` manages taxonomy and users
- `SUPER_ADMIN` handles configuration and privileged controls
- `AUDITOR` reads logs and oversight data

## Final Recommendation

Build the backend around:

- hierarchical sections
- localized topics
- ordered content blocks
- multilingual slugs
- reusable public and admin DTOs

This will give the frontend exactly what it needs to reproduce the handbook experience in a structured, maintainable, multilingual way.
