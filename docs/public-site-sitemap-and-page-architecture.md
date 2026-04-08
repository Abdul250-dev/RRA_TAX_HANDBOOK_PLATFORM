# RRA Tax Handbook Public Site Sitemap and Page Architecture

## Purpose

This document defines the recommended public-facing information architecture for the RRA Tax Handbook Platform. It is based on:

- the 2025 RRA Tax Handbook PDF structure
- the exported design screens
- the product requirement to support full language switching for English, French, and Kinyarwanda

This document should be treated as the source reference for:

- frontend route design
- page template design
- backend content modeling
- multilingual navigation behavior

## Design Principles

The public platform should behave like a guided tax knowledge portal, not like a generic blog and not like a raw PDF browser.

The user experience should prioritize:

- finding a tax type quickly
- understanding what a tax is
- completing a task such as registration, declaration, payment, or compliance
- navigating from broad categories to specific topics
- switching language without losing context

## Supported Languages

The public site should support:

- `en` for English
- `fr` for French
- `kin` for Kinyarwanda

## Locale Strategy

### Recommended URL Pattern

Use locale-prefixed routes:

- `/en`
- `/fr`
- `/kin`

All public content routes should be nested under the locale:

- `/en/taxes`
- `/fr/taxes`
- `/kin/taxes`

### Language Switching Rules

When a user switches language:

- keep them on the equivalent page if a translation exists
- if no translation exists, fall back to the same page shell with a “translation unavailable” notice or a default-language fallback
- preserve navigation context, breadcrumbs, and section hierarchy

### Slug Strategy

Recommended approach:

- keep a stable internal content identifier in the backend
- store localized slugs per language

Example:

- English: `/en/taxes/domestic/income-tax`
- French: `/fr/taxes/domestic/impot-sur-le-revenu`
- Kinyarwanda: `/kin/taxes/domestic/umusoro-ku-musaruro`

This is better than forcing one shared slug across all languages.

## Top-Level Navigation

The top-level public navigation should be:

- Home
- General Information
- Taxes in Rwanda
- Other Services
- User Guides and Examples
- Search
- Contact / Help

Optional utility actions:

- Accessibility
- Go to `rra.gov.rw`
- Staff Login
- Language Switcher

## Global Navigation Model

The platform navigation should follow this pattern:

1. homepage entry points
2. section landing pages
3. topic landing pages
4. deep content sections within a topic

This mirrors how the handbook itself is organized.

## Sitemap

### Root

- `/{locale}`

### Main Pages

- `/{locale}/general-information`
- `/{locale}/taxes`
- `/{locale}/other-services`
- `/{locale}/guides`
- `/{locale}/search`
- `/{locale}/contact`

### General Information

- `/{locale}/general-information`
- `/{locale}/general-information/introduction`
- `/{locale}/general-information/purpose-of-the-tax-handbook`
- `/{locale}/general-information/foreword`
- `/{locale}/general-information/benefits-of-taxes`
- `/{locale}/general-information/principles-of-taxation`
- `/{locale}/general-information/history-of-taxes-in-rwanda`
- `/{locale}/general-information/rra-modernisation-and-reforms`
- `/{locale}/general-information/rra-structure`
- `/{locale}/general-information/rra-service-charter`
- `/{locale}/general-information/definitions`
- `/{locale}/general-information/acronyms`

### Taxes in Rwanda

- `/{locale}/taxes`
- `/{locale}/taxes/domestic`
- `/{locale}/taxes/customs`

### Domestic Taxes

- `/{locale}/taxes/domestic`
- `/{locale}/taxes/domestic/central-government`
- `/{locale}/taxes/domestic/local-government`

### Central Government Tax Topics

- `/{locale}/taxes/domestic/central-government/income-tax`
- `/{locale}/taxes/domestic/central-government/paye`
- `/{locale}/taxes/domestic/central-government/vat`
- `/{locale}/taxes/domestic/central-government/excise-duty`
- `/{locale}/taxes/domestic/central-government/withholding-taxes`
- `/{locale}/taxes/domestic/central-government/gaming-taxes`
- `/{locale}/taxes/domestic/central-government/capital-gains-tax`
- `/{locale}/taxes/domestic/central-government/mining-royalty-tax`
- `/{locale}/taxes/domestic/central-government/road-maintenance-levy`
- `/{locale}/taxes/domestic/central-government/tourism-tax`

### Local Government Tax Topics

- `/{locale}/taxes/domestic/local-government`
- `/{locale}/taxes/domestic/local-government/immovable-property-tax`
- `/{locale}/taxes/domestic/local-government/tax-on-sale-of-immovable-property`
- `/{locale}/taxes/domestic/local-government/trading-license-tax`
- `/{locale}/taxes/domestic/local-government/rental-income-tax`
- `/{locale}/taxes/domestic/local-government/fees-levied-by-decentralised-entities`

### Customs

The customs branch should support its own subsection pages:

- `/{locale}/taxes/customs`
- `/{locale}/taxes/customs/explanation`
- `/{locale}/taxes/customs/duties`
- `/{locale}/taxes/customs/clearing-agents`
- `/{locale}/taxes/customs/border-posts-and-dry-ports`
- `/{locale}/taxes/customs/imports-and-exports`
- `/{locale}/taxes/customs/facilitation-schemes`
- `/{locale}/taxes/customs/importing-motor-vehicles`
- `/{locale}/taxes/customs/penalties`

### Other Services

- `/{locale}/other-services`
- `/{locale}/other-services/vat-rewards-and-refunds`
- `/{locale}/other-services/debt-management`
- `/{locale}/other-services/audits`
- `/{locale}/other-services/certificates`
- `/{locale}/other-services/motor-vehicle-services`
- `/{locale}/other-services/voluntary-disclosure-scheme`
- `/{locale}/other-services/online-requests`
- `/{locale}/other-services/myrra`
- `/{locale}/other-services/exchange-of-information`

### User Guides and Examples

- `/{locale}/guides`
- `/{locale}/guides/registration`
- `/{locale}/guides/etax`
- `/{locale}/guides/m-declaration`
- `/{locale}/guides/lgt-system`
- `/{locale}/guides/payment-methods`
- `/{locale}/guides/mobile-money`
- `/{locale}/guides/internet-banking`
- `/{locale}/guides/examples`

### Search

- `/{locale}/search`

### Contact

- `/{locale}/contact`
- `/{locale}/contact/rra-contact-details`
- `/{locale}/contact/tax-centres`

## Page Types

### 1. Homepage

Purpose:

- introduce the handbook
- direct users into the correct branch quickly
- highlight the major content groups

Core sections:

- hero section
- featured entry points
- quick links to major handbook branches
- search box
- update date
- footer and institutional links

### 2. Section Landing Page

Examples:

- General Information
- Taxes in Rwanda
- Domestic Taxes
- Central Government Taxes

Purpose:

- explain the section
- show the list of child topics
- give users a clear choice of where to go next

Core sections:

- title
- section summary
- child topic cards
- optional quick facts
- optional featured guides

### 3. Topic Landing Page

Examples:

- Income Tax
- VAT
- Local Government Taxes

Purpose:

- provide an overview of a tax or service topic
- present its major subtopics in a scannable way

Core sections:

- topic summary
- subtopic cards or accordion list
- related guides
- related FAQs
- related documents

### 4. Topic Detail Page

Examples:

- VAT Registration
- Taxable Income
- Declaration Guide

Purpose:

- explain a specific concept or procedure
- allow deep reading without losing navigation context

Core sections:

- page header
- breadcrumb
- section navigation
- article content blocks
- related links
- download/document links
- last updated

### 5. Search Results Page

Purpose:

- help users find content by tax name, keyword, acronym, or task

Search should support:

- title matches
- keyword matches
- acronym matches
- translated content matches
- filtering by category
- filtering by language

### 6. Guide Page

Purpose:

- present step-by-step instructions and examples

Core sections:

- summary
- prerequisites
- numbered steps
- screenshots or diagrams if available
- related topic links

## Detail Page Structure

Each major tax topic should be structured consistently.

Recommended reusable sub-sections:

- overview
- who is affected
- registration
- rates or thresholds
- obligations
- declaration
- payment
- penalties and fines
- examples
- related documents
- FAQs

Not all topics need every subsection, but the pattern should be reusable.

## Breadcrumb Strategy

Example:

- Home
- Taxes in Rwanda
- Domestic Taxes
- Central Government Taxes
- VAT

Breadcrumbs should be localized and match the translated navigation hierarchy.

## Sidebar / In-Page Navigation

For topic detail pages, use:

- left sidebar for sibling sections
- in-page anchor navigation for long pages

This is important because handbook content is dense and procedural.

## Search Architecture Requirements

The search experience should allow users to find:

- a tax by name
- a process such as registration or declaration
- a legal or tax acronym
- a user guide
- a related downloadable document

Recommended search result grouping:

- topics
- guides
- FAQs
- documents

## Multilingual Content Rules

All public content should support translation for:

- title
- summary
- body content
- slug
- navigation labels
- SEO metadata

Recommended fallback behavior:

- if a translation is missing, keep the route structure but show a localized fallback notice
- fallback to default language content only if product owners approve that behavior

## Recommended Homepage Content Blocks

The homepage should include these visible entry points:

- General Information
- Taxes administered in Rwanda
- Other Services
- User Guides and Examples

Optional additional homepage blocks:

- Tax Types Summary
- Quick Reference
- Recently updated topics
- Popular searches

## Content Ownership Implications

This architecture implies that the admin system will need to manage:

- navigation sections
- topics
- topic sub-sections
- translations
- guides
- FAQs
- related documents

## Backend Modeling Implications

This sitemap should lead directly to a backend model with at least:

- sections
- topics
- topic translations
- content blocks
- guides
- FAQs
- documents
- localized slugs

## Recommended Next Step

After approving this sitemap and page architecture, the next step should be:

- exact backend entity design and API contracts derived from this structure

That backend design should be multilingual-first and should preserve:

- section hierarchy
- ordered content blocks
- localized slugs
- cross-links between topics, guides, FAQs, and documents
