export interface PageData {
  id: string;
  title: string;
  subtitle?: string;
  content: string;
  lastUpdated?: string;
}

export const TAX_HANDBOOK_PAGES: Record<string, PageData> = {
  home: {
    id: 'home',
    title: 'RRA Tax Handbook – Edition 2025',
    content: '',
    lastUpdated: '05/03/2026',
  },
  general: {
    id: 'general',
    title: 'General Information',
    subtitle: 'The RRA Tax Handbook is a guiding tool for taxpayers to understand the tax laws and procedures for all tax types in Rwanda.',
    content: '',
    lastUpdated: '05/03/2026',
  },
  intro: {
    id: 'intro',
    title: 'Introduction',
    subtitle: '',
    content: 'Taxes are the mandatory contribution from citizens to a government in order to fund public expenditures. Since 1997, Rwanda Revenue Authority (RRA) has been legally mandated by the Government of Rwanda with the task of assessing, collecting and accounting for taxes, customs duties, local government taxes and fees and non-tax revenues.',
    lastUpdated: '',
  },
  purpose: {
    id: 'purpose',
    title: 'Purpose of this Tax Handbook',
    content: 'The RRA Tax Handbook is a simplified guide for taxpayers to understand the tax laws and procedures for all tax types in Rwanda. It covers how to register, declare and pay each of the domestic taxes, Local Government Taxes (LGT) and fees, and customs duties.',
    lastUpdated: '',
  },
  foreword: {
    id: 'foreword',
    title: 'Foreword by the Commissioner General',
    content: 'It gives me great pleasure to present this edition of the RRA Tax Handbook — a strong demonstration of RRA\'s commitment to be Here For You, To Serve.',
    lastUpdated: '',
  },
  benefits: {
    id: 'benefits',
    title: 'Benefits of Taxes',
    content: 'Taxes are a vital and hugely beneficial requirement of modern society, necessary to pay for public goods and services that benefit all members of the population.',
    lastUpdated: '',
  },
  principles: {
    id: 'principles',
    title: 'Principles of Taxation',
    content: 'There are four key principles of good taxation systems, which RRA strives to attain and uphold.',
    lastUpdated: '',
  },
  history: {
    id: 'history',
    title: 'History of Taxes in Rwanda',
    content: 'The formal, monetary tax system in Rwanda has been in effect for more than one hundred years.',
    lastUpdated: '',
  },
  'rra-structure': {
    id: 'rra-structure',
    title: 'RRA Structure',
    content: 'RRA is a semi-autonomous revenue authority accountable to the Ministry of Finance and Economic Planning (MINECOFIN).',
    lastUpdated: '',
  },
  'rra-service': {
    id: 'rra-service',
    title: 'RRA Service Charter',
    content: 'The RRA Service Charter contains details on taxpayers\' rights and obligations, as well as the range of services offered by RRA.',
    lastUpdated: '',
  },
  definitions: {
    id: 'definitions',
    title: 'Definitions',
    content: '',
    lastUpdated: '',
  },
  acronyms: {
    id: 'acronyms',
    title: 'Acronyms',
    content: '',
    lastUpdated: '',
  },
  'taxes-rwanda': {
    id: 'taxes-rwanda',
    title: 'Taxes administered in Rwanda',
    subtitle: 'Domestic taxes refer to all taxes and fees administered by RRA\'s Domestic Tax Department (DTD).',
    content: '',
    lastUpdated: '05/03/2026',
  },
  'income-tax': {
    id: 'income-tax',
    title: 'Income Tax',
    subtitle: 'A tax on income resulting from business, self-employment and investment activities.',
    content: '',
    lastUpdated: '',
  },
  paye: {
    id: 'paye',
    title: 'Pay As You Earn (PAYE)',
    subtitle: 'Tax on employment income, withheld by employers on behalf of employees.',
    content: '',
    lastUpdated: '',
  },
  vat: {
    id: 'vat',
    title: 'Value Added Tax (VAT)',
    subtitle: 'A tax on the consumption of goods and services.',
    content: '',
    lastUpdated: '',
  },
  excise: {
    id: 'excise',
    title: 'Excise Duty',
    subtitle: 'A tax applied to specific products, discouraging consumption with negative social impacts.',
    content: '',
    lastUpdated: '',
  },
  withholding: {
    id: 'withholding',
    title: 'Withholding Taxes',
    subtitle: 'Taxes declared by the source of a transaction on behalf of the recipient.',
    content: '',
    lastUpdated: '',
  },
  gaming: {
    id: 'gaming',
    title: 'Gaming Tax',
    subtitle: 'Taxes applied on the proceeds of gambling activities.',
    content: '',
    lastUpdated: '',
  },
  'capital-gains': {
    id: 'capital-gains',
    title: 'Capital Gains Tax',
    subtitle: 'Tax on gains from the sale or transfer of shares, debentures or immovable assets.',
    content: '',
    lastUpdated: '',
  },
  mining: {
    id: 'mining',
    title: 'Mining Royalty Tax',
    subtitle: 'Tax withheld on mineral extraction and export.',
    content: '',
    lastUpdated: '',
  },
  customs: {
    id: 'customs',
    title: 'Customs Duties',
    subtitle: 'Taxes levied on goods imported into or exported from Rwanda.',
    content: '',
    lastUpdated: '',
  },
  lgt: {
    id: 'lgt',
    title: 'Local Government Taxes (LGT)',
    subtitle: 'Taxes and fees levied by local government entities within Rwanda.',
    content: '',
    lastUpdated: '',
  },
  'other-services': {
    id: 'other-services',
    title: 'Other services',
    subtitle: 'RRA offers a range of additional tax-related services for taxpayers and businesses.',
    content: '',
    lastUpdated: '05/03/2026',
  },
  'user-guides': {
    id: 'user-guides',
    title: 'User guides and examples',
    subtitle: 'Step-by-step filing guides and worked examples for all major tax types.',
    content: '',
    lastUpdated: '05/03/2026',
  },
};

export const NAVIGATION_ITEMS = [
  { label: 'General Information', pageId: 'general' },
  { label: 'Taxes in Rwanda', pageId: 'taxes-rwanda' },
  { label: 'Other services', pageId: 'other-services' },
  { label: 'User guides', pageId: 'user-guides' },
];

export const GENERAL_SUB_ITEMS = [
  { label: 'Introduction', pageId: 'intro' },
  { label: 'Purpose of this Tax Handbook', pageId: 'purpose' },
  { label: 'Foreword', pageId: 'foreword' },
  { label: 'Benefits of Taxes', pageId: 'benefits' },
  { label: 'Principles of Taxation', pageId: 'principles' },
  { label: 'History of Taxes in Rwanda', pageId: 'history' },
  { label: 'RRA Structure', pageId: 'rra-structure' },
  { label: 'RRA Service Charter', pageId: 'rra-service' },
  { label: 'Definitions', pageId: 'definitions' },
  { label: 'Acronyms', pageId: 'acronyms' },
];

export const TAXES_IN_RWANDA_ITEMS = [
  { label: 'Income Tax', pageId: 'income-tax' },
  { label: 'PAYE', pageId: 'paye' },
  { label: 'VAT', pageId: 'vat' },
  { label: 'Excise Duty', pageId: 'excise' },
  { label: 'Withholding Taxes', pageId: 'withholding' },
  { label: 'Gaming Tax', pageId: 'gaming' },
  { label: 'Capital Gains Tax', pageId: 'capital-gains' },
  { label: 'Mining Royalty Tax', pageId: 'mining' },
  { label: 'Customs Duties', pageId: 'customs' },
  { label: 'Local Government Taxes', pageId: 'lgt' },
];
