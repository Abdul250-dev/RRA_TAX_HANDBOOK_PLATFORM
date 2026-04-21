import type { HandbookSectionDetail, HandbookTopicDetail } from "../../types/handbook";

export const handbookUpdatedAt = "2026-03-05";

const topicTitles: Record<string, { title: string; summary: string; sectionId: number }> = {
  intro: { title: "Introduction", summary: "RRA mandate, taxpayer guidance, and handbook overview.", sectionId: 1001 },
  purpose: { title: "Purpose of this Handbook", summary: "A simplified guide to registration, declaration, payment, and compliance.", sectionId: 1001 },
  foreword: { title: "Foreword by the Commissioner General", summary: "A message on service, clarity, and voluntary compliance.", sectionId: 1001 },
  benefits: { title: "Benefits of Taxes", summary: "How taxes support national development and public services.", sectionId: 1001 },
  principles: { title: "Principles of Taxation", summary: "Equity, certainty, convenience, and economy.", sectionId: 1001 },
  history: { title: "History of Taxes in Rwanda", summary: "A short timeline of Rwanda's tax administration.", sectionId: 1001 },
  "rra-structure": { title: "RRA Structure", summary: "How RRA is governed and organized.", sectionId: 1001 },
  "rra-service": { title: "RRA Service Charter", summary: "Taxpayer rights, obligations, and service standards.", sectionId: 1001 },
  definitions: { title: "Definitions", summary: "Common tax terms used in the handbook.", sectionId: 1001 },
  acronyms: { title: "Acronyms", summary: "Frequently used abbreviations.", sectionId: 1001 },
  "income-tax": { title: "Income Tax (PIT & CIT)", summary: "Personal and corporate income tax regimes.", sectionId: 1002 },
  paye: { title: "Pay As You Earn (PAYE)", summary: "Employment income tax withheld by employers.", sectionId: 1002 },
  vat: { title: "Value Added Tax (VAT)", summary: "VAT registration, obligations, declaration, and payment.", sectionId: 1002 },
  excise: { title: "Excise Duty", summary: "Tax on selected products and services.", sectionId: 1002 },
  withholding: { title: "Withholding Taxes", summary: "Tax withheld at the source of payment.", sectionId: 1002 },
  gaming: { title: "Gaming Tax", summary: "Tax on gambling and gaming proceeds.", sectionId: 1002 },
  "capital-gains": { title: "Capital Gains Tax", summary: "Tax on gains from sale or transfer of assets.", sectionId: 1002 },
  mining: { title: "Mining Royalty Tax", summary: "Royalty tax on mineral extraction and export.", sectionId: 1002 },
  customs: { title: "Customs Duties", summary: "Duties on imported and exported goods.", sectionId: 1002 },
  lgt: { title: "Local Government Taxes", summary: "Taxes and fees collected at local government level.", sectionId: 1002 },
  "other-services": { title: "Other services", summary: "Additional RRA services for taxpayers.", sectionId: 1003 },
  "user-guides": { title: "User guides and examples", summary: "Step-by-step filing guides and practical examples.", sectionId: 1004 },
};

export const staticSections: HandbookSectionDetail[] = [
  {
    id: 1001,
    parentId: null,
    name: "General Information",
    slug: "general-information",
    summary:
      "The RRA Tax Handbook is a guiding tool for taxpayers to understand tax laws, procedures, rights, and obligations.",
    type: "MAIN",
    sortOrder: 1,
    children: [],
    topics: [
      topicSummary("intro"),
      topicSummary("purpose"),
      topicSummary("foreword"),
      topicSummary("benefits"),
      topicSummary("principles"),
      topicSummary("history"),
      topicSummary("rra-structure"),
      topicSummary("rra-service"),
      topicSummary("definitions"),
      topicSummary("acronyms"),
    ],
  },
  {
    id: 1002,
    parentId: null,
    name: "Taxes administered in Rwanda",
    slug: "taxes",
    summary:
      "Find information on different taxes, rates, filing procedures, payment rules, and non-compliance penalties.",
    type: "MAIN",
    sortOrder: 2,
    children: [],
    topics: [
      topicSummary("income-tax"),
      topicSummary("paye"),
      topicSummary("vat"),
      topicSummary("excise"),
      topicSummary("withholding"),
      topicSummary("gaming"),
      topicSummary("capital-gains"),
      topicSummary("mining"),
      topicSummary("customs"),
      topicSummary("lgt"),
    ],
  },
  {
    id: 1003,
    parentId: null,
    name: "Other services",
    slug: "other-services",
    summary:
      "Additional RRA services including refunds, certificates, audits, voluntary disclosure, and motor vehicle services.",
    type: "MAIN",
    sortOrder: 3,
    children: [],
    topics: [topicSummary("other-services")],
  },
  {
    id: 1004,
    parentId: null,
    name: "User guides and examples",
    slug: "guides",
    summary: "Step-by-step filing guides and worked examples for major tax types.",
    type: "MAIN",
    sortOrder: 4,
    children: [],
    topics: [topicSummary("user-guides")],
  },
];

export const staticTopics: HandbookTopicDetail[] = [
  topic("intro", 1001, "Introduction", "RRA mandate, taxpayer guidance, and handbook overview.", [
    block(
      "About this handbook",
      "Taxes are mandatory contributions from citizens and businesses used to fund public expenditures. Since 1997, Rwanda Revenue Authority has been mandated by the Government of Rwanda to assess, collect, and account for taxes, customs duties, local government taxes, fees, and non-tax revenues.\n\nThis handbook simplifies key tax concepts so taxpayers can register, declare, pay, and comply with confidence.",
    ),
  ]),
  topic("purpose", 1001, "Purpose of this Handbook", "A simplified guide to registration, declaration, payment, and compliance.", [
    block(
      "Purpose",
      "The handbook helps taxpayers understand domestic taxes, Local Government Taxes, customs duties, and RRA services. It is designed as a practical guide, not a substitute for tax laws, regulations, and official notices.",
    ),
  ]),
  topic("foreword", 1001, "Foreword by the Commissioner General", "A message on service, clarity, and voluntary compliance.", [
    block(
      "Foreword",
      "This edition demonstrates RRA's commitment to be Here For You, To Serve. Clear guidance supports voluntary compliance and strengthens the relationship between taxpayers and the tax administration.",
    ),
  ]),
  topic("benefits", 1001, "Benefits of Taxes", "How taxes support national development and public services.", [
    block(
      "Why taxes matter",
      "Taxes finance infrastructure, security, education, healthcare, utilities, governance, and national self-reliance. They allow citizens and businesses to contribute directly to Rwanda's development.",
      "INFO_CARD",
    ),
  ]),
  topic("principles", 1001, "Principles of Taxation", "Equity, certainty, convenience, and economy.", [
    block(
      "Four principles",
      "Equity means taxpayers contribute fairly. Certainty means obligations are clear. Convenience means payment and declaration should be practical. Economy means the cost of collecting tax should be reasonable.",
    ),
  ]),
  topic("history", 1001, "History of Taxes in Rwanda", "A short timeline of Rwanda's tax administration.", [
    block(
      "Timeline",
      "1912: Early formal tax administration begins.\n1925: Monetary taxation expands.\n1997: Rwanda Revenue Authority is established.\n2001: VAT is introduced.\n2009: E-Tax services expand digital filing.\n2021: AfCFTA context strengthens trade and customs modernization.",
      "STEP_LIST",
    ),
  ]),
  topic("rra-structure", 1001, "RRA Structure", "How RRA is governed and organized.", [
    block(
      "Structure",
      "RRA is a semi-autonomous revenue authority accountable to the Ministry of Finance and Economic Planning. It is led by the Commissioner General and organized through departments responsible for domestic taxes, customs, support services, modernization, and taxpayer services.",
    ),
  ]),
  topic("rra-service", 1001, "RRA Service Charter", "Taxpayer rights, obligations, and service standards.", [
    block(
      "Service charter",
      "The service charter describes taxpayer rights and obligations, service channels, expected timelines, and the standards RRA commits to when serving taxpayers.",
    ),
  ]),
  topic("definitions", 1001, "Definitions", "Common tax terms used in the handbook.", [
    block(
      "Common terms",
      "TIN: Taxpayer Identification Number.\nTax period: the reporting period for a tax obligation.\nDeclaration: a return submitted to RRA showing tax due or refundable.\nTurnover: gross business income before expenses.\nArrears: unpaid tax after the due date.\nRefund: tax amount owed back to a taxpayer.",
      "STEP_LIST",
    ),
  ]),
  topic("acronyms", 1001, "Acronyms", "Frequently used abbreviations.", [
    block(
      "Acronyms",
      "CIT: Corporate Income Tax\nPIT: Personal Income Tax\nPAYE: Pay As You Earn\nVAT: Value Added Tax\nWHT: Withholding Tax\nLGT: Local Government Taxes\nRSSB: Rwanda Social Security Board\nTCC: Tax Clearance Certificate\nVDS: Voluntary Disclosure Scheme",
      "STEP_LIST",
    ),
  ]),
  topic("income-tax", 1002, "Income Tax (PIT & CIT)", "Personal and corporate income tax regimes.", [
    block(
      "Overview",
      "Income tax applies to income from employment, business, self-employment, and investment. Small businesses may use flat or lump sum regimes, while larger businesses file under the real regime.",
    ),
    block("Key rates", "Corporate income tax is generally 28 percent. Personal income tax is progressive. Quarterly prepayments may apply based on prior year tax or current year expectations.", "INFO_CARD"),
  ]),
  topic("paye", 1002, "Pay As You Earn (PAYE)", "Employment income tax withheld by employers.", [
    block(
      "PAYE filing",
      "Employers calculate PAYE from employee remuneration, withhold it at source, declare it, and pay it to RRA by the required deadline. PAYE is commonly filed through ISHEMA/RSSB and linked systems.",
    ),
  ]),
  topic("vat", 1002, "Value Added Tax (VAT)", "VAT registration, obligations, declaration, and payment.", [
    block("VAT overview", "VAT is a tax on consumption of goods and services. The standard rate is 18 percent, with zero-rated and exempt supplies for specific categories."),
    block(
      "VAT obligations",
      "Display the VAT certificate, issue EBM or EIS invoices, keep supporting records, file VAT returns, pay by the 15th of the following month, and make records available for RRA review.",
      "STEP_LIST",
    ),
  ]),
  topic("excise", 1002, "Excise Duty", "Tax on selected products and services.", [
    block("Excise duty", "Excise duty applies to specific goods and services such as alcoholic beverages, tobacco, fuel, telecom services, and other products defined by law. It may be specific, ad valorem, or mixed."),
  ]),
  topic("withholding", 1002, "Withholding Taxes", "Tax withheld at the source of payment.", [
    block("Withholding", "Withholding taxes may apply on payments such as interest, dividends, royalties, service fees, public tenders, and imports. Rates depend on the transaction type and taxpayer status."),
  ]),
  topic("gaming", 1002, "Gaming Tax", "Tax on gambling and gaming proceeds.", [
    block("Gaming", "Gaming tax applies to operators and, in some cases, winnings from betting, casino, lottery, and other gambling activities according to the applicable law."),
  ]),
  topic("capital-gains", 1002, "Capital Gains Tax", "Tax on gains from sale or transfer of assets.", [
    block("Capital gains", "Capital gains tax applies to gains from the sale or transfer of shares, debentures, and other taxable assets. The taxable gain is generally the difference between disposal proceeds and allowable cost."),
  ]),
  topic("mining", 1002, "Mining Royalty Tax", "Royalty tax on mineral extraction and export.", [
    block("Mining royalty", "Mining royalty is charged on mineral extraction and export. Rates depend on the mineral category and are applied according to mining and tax legislation."),
  ]),
  topic("customs", 1002, "Customs Duties", "Duties on imported and exported goods.", [
    block("Customs", "Customs duties are levied on goods imported into or exported from Rwanda. Rwanda applies EAC customs rules, tariff classification, valuation, origin rules, and border clearance procedures."),
  ]),
  topic("lgt", 1002, "Local Government Taxes", "Taxes and fees collected at local government level.", [
    block("LGT", "Local Government Taxes include property tax, trading licence tax, rental income tax, market fees, public cleaning fees, and other district-level revenues provided by law."),
  ]),
  topic("other-services", 1003, "Other services", "Additional RRA services for taxpayers.", [
    block("Services", "RRA provides VAT refunds, debt management, audit support, tax clearance certificates, quitus fiscal, voluntary disclosure services, motor vehicle services, transfer pricing guidance, and international exchange of information support."),
  ]),
  topic("user-guides", 1004, "User guides and examples", "Step-by-step filing guides and practical examples.", [
    block(
      "Flat Tax / Lump Sum: M-Declaration",
      "1. Keep records of income to calculate annual turnover.\n2. Dial *800# and choose language.\n3. Select Other Business Activities, then Registration or Declaration.\n4. Enter TIN, National ID, turnover, and tax period.\n5. Pay using the RRA reference number via E-Payment, Mobile Money, MobiCash, or bank.",
      "STEP_LIST",
    ),
    block(
      "VAT declaration",
      "1. Download VAT annexures from E-Tax.\n2. Complete sales, purchases, importation, reverse charge, and retained VAT tabs where applicable.\n3. Validate and save annexures.\n4. Complete the declaration form, upload annexures, certify, and submit.\n5. Pay by the 15th of the following month.",
      "STEP_LIST",
    ),
    block(
      "IQP worked example",
      "If a taxpayer paid FRW 540,000 income tax in 2023 from FRW 18,000,000 turnover, and Q2 2024 turnover is FRW 9,000,000: (540,000 / 18,000,000) x 9,000,000 = FRW 270,000. If FRW 50,000 WHT was paid, IQP due is FRW 220,000.",
      "INFO_CARD",
    ),
  ]),
];

export const staticSearchIndex = staticTopics.map((item) => ({
  href: `/topics/${item.slug}`,
  title: item.title,
  text: [item.summary, item.introText, ...item.blocks.map((block) => `${block.title} ${block.body ?? ""}`)].join(" "),
}));

export function findStaticSection(slug: string) {
  return staticSections.find((section) => section.slug === slug) ?? null;
}

export function findStaticTopic(slug: string) {
  return staticTopics.find((topic) => topic.slug === slug) ?? null;
}

export function getStaticHomepageCards(locale: string) {
  return staticSections.map((section) => ({
    key: section.slug,
    title: section.name,
    description: section.summary ?? "",
    href: `/${locale}/sections/${section.slug}`,
  }));
}

function topicSummary(slug: string) {
  const found = topicTitles[slug];
  return {
    id: Math.abs(slug.split("").reduce((sum, char) => sum + char.charCodeAt(0), 0)),
    sectionId: found?.sectionId ?? 0,
    title: found?.title ?? slug,
    slug,
    summary: found?.summary ?? null,
    topicType: "GUIDE",
    status: "PUBLISHED",
    sortOrder: 1,
    scheduledPublishAt: null,
  };
}

function topic(slug: string, sectionId: number, title: string, summary: string, blocks: HandbookTopicDetail["blocks"]): HandbookTopicDetail {
  return {
    id: Math.abs(slug.split("").reduce((sum, char) => sum + char.charCodeAt(0), 0)),
    sectionId,
    title,
    slug,
    summary,
    introText: summary,
    topicType: "GUIDE",
    status: "PUBLISHED",
    scheduledPublishAt: null,
    blocks,
  };
}

function block(title: string, body: string, blockType = "RICH_TEXT") {
  return {
    id: Math.abs(`${title}-${body.length}`.split("").reduce((sum, char) => sum + char.charCodeAt(0), 0)),
    title,
    body,
    blockType,
    anchorKey: title.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, ""),
    sortOrder: 1,
  };
}
