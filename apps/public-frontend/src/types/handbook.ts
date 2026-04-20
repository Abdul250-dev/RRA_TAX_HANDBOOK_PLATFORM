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
  blocks: HandbookTopicBlock[];
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
