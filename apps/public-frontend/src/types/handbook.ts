export interface HandbookSectionSummary {
  id: number;
  parentId: number | null;
  name: string;
  slug: string;
  summary: string | null;
  type: string;
  sortOrder: number;
}

export interface HandbookTopicSummary {
  id: number;
  sectionId: number;
  title: string;
  slug: string;
  summary: string | null;
  topicType: string;
  status: string;
  sortOrder: number;
  scheduledPublishAt: string | null;
}

export interface HandbookTopicBlock {
  id: number;
  title: string;
  body: string | null;
  blockType: string;
  anchorKey: string;
  sortOrder: number;
}

export interface HandbookTopicDetail {
  id: number;
  sectionId: number;
  title: string;
  slug: string;
  summary: string | null;
  introText: string | null;
  topicType: string;
  status: string;
  scheduledPublishAt: string | null;
  lastUpdated: string | null;
  blocks: HandbookTopicBlock[];
  relatedFaqs: Array<{
    id: number;
    question: string;
    answer: string;
    language: string;
  }>;
  relatedDocuments: Array<{
    id: number;
    title: string;
    fileName: string;
    fileUrl: string;
  }>;
  relatedGuides: HandbookTopicSummary[];
}

export interface HomepageCard {
  sectionId: number;
  title: string;
  slug: string;
  description: string;
  sortOrder: number;
}

export interface HomepageContent {
  kicker: string;
  title: string;
  subtitle: string;
  searchLabel: string;
  helpLabel: string;
  updatedAt: string;
  cards: HomepageCard[];
}

export interface HandbookSectionDetail extends HandbookSectionSummary {
  children: HandbookSectionSummary[];
  topics: HandbookTopicSummary[];
}

export interface PublicSearchResult {
  id: number;
  title: string;
  slug: string;
  summary: string | null;
  type: string;
  sectionId: number | null;
  url: string;
}

export interface PublicSearchFaqResult {
  id: number;
  question: string;
  answer: string;
  language: string;
}

export interface PublicSearchDocumentResult {
  id: number;
  title: string;
  fileName: string;
  fileUrl: string;
}

export interface PublicSearchResponse {
  query: string;
  locale: string;
  sections: PublicSearchResult[];
  topics: PublicSearchResult[];
  guides: PublicSearchResult[];
  faqs: PublicSearchFaqResult[];
  documents: PublicSearchDocumentResult[];
}
