import { apiClient } from "./axios";

export type ContentStatus = "DRAFT" | "REVIEW" | "APPROVED" | "PUBLISHED" | "ARCHIVED";
export type LocaleCode = "EN" | "FR" | "KIN";
export type TopicType = "TAX_TOPIC" | "SERVICE_TOPIC" | "STATIC_TOPIC" | "LANDING_TOPIC" | "GUIDE";
export type SectionType = "MAIN" | "GROUP" | "SUBGROUP";
export type TopicWorkflowAction =
  | "SUBMIT_FOR_REVIEW"
  | "REQUEST_CHANGES"
  | "APPROVE"
  | "SCHEDULE_PUBLISH"
  | "PUBLISH"
  | "UNPUBLISH"
  | "ARCHIVE";

export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface ContentSummary {
  totalTopics: number;
  draftTopics: number;
  reviewTopics: number;
  approvedTopics: number;
  publishedTopics: number;
  archivedTopics: number;
  totalSections: number;
  draftSections: number;
  publishedSections: number;
  archivedSections: number;
}

export interface AdminSection {
  id: number;
  parentId: number | null;
  name: string;
  slug: string;
  summary: string;
  type: SectionType;
  sortOrder: number;
  status: string;
}

export interface TopicSummary {
  id: number;
  sectionId: number;
  title: string;
  slug: string;
  summary: string;
  topicType: string;
  status: string;
  sortOrder: number;
  scheduledPublishAt?: string | null;
}

export interface TopicDetail extends TopicSummary {
  introText: string;
  blocks: TopicBlock[];
}

export interface TopicBlock {
  id: number;
  title: string;
  body: string;
  blockType: "RICH_TEXT" | "STEP_LIST" | "INFO_CARD" | "RELATED_LINKS";
  anchorKey: string;
  sortOrder: number;
}

export interface CreateSectionPayload {
  parentId?: number | null;
  type: SectionType;
  sortOrder: number;
  locale: LocaleCode;
  name: string;
  slug: string;
  summary: string;
}

export interface CreateTopicPayload {
  sectionId: number;
  topicType: TopicType;
  sortOrder: number;
  locale: LocaleCode;
  title: string;
  slug: string;
  summary: string;
  introText: string;
}

export interface CreateTopicBlockPayload {
  blockType: TopicBlock["blockType"];
  sortOrder: number;
  anchorKey: string;
  locale: LocaleCode;
  title: string;
  body: string;
}

export async function getAdminContentSummary(token?: string) {
  return apiClient<ContentSummary>("/api/admin/content/summary", { token });
}

export async function getAdminSections(token?: string, locale: LocaleCode = "EN") {
  return apiClient<AdminSection[]>(`/api/admin/content/sections?locale=${locale}`, { token });
}

export async function getAdminTopics(token?: string, locale: LocaleCode = "EN", status?: string) {
  const params = new URLSearchParams({ locale });
  if (status && status !== "ALL") params.set("status", status);
  return apiClient<TopicSummary[]>(`/api/admin/content/topics?${params.toString()}`, { token });
}

export async function getAdminReviewQueue(token?: string, locale: LocaleCode = "EN") {
  return apiClient<TopicSummary[]>(`/api/admin/content/topics/review-queue?locale=${locale}`, { token });
}

export async function getAdminPublishQueue(token?: string, locale: LocaleCode = "EN") {
  return apiClient<TopicSummary[]>(`/api/admin/content/topics/publish-queue?locale=${locale}`, { token });
}

export async function createAdminSection(token: string, payload: CreateSectionPayload) {
  return apiClient<ApiResponse<AdminSection>>("/api/admin/content/sections", {
    data: payload,
    method: "POST",
    token,
  });
}

export async function createAdminTopic(token: string, payload: CreateTopicPayload) {
  return apiClient<ApiResponse<TopicDetail>>("/api/admin/content/topics", {
    data: payload,
    method: "POST",
    token,
  });
}

export async function createAdminTopicBlock(token: string, topicId: number, payload: CreateTopicBlockPayload) {
  return apiClient<ApiResponse<TopicBlock>>(`/api/admin/content/topics/${topicId}/blocks`, {
    data: payload,
    method: "POST",
    token,
  });
}

export async function transitionAdminTopic(
  token: string,
  topicId: number,
  action: TopicWorkflowAction,
  scheduledAt?: string,
) {
  return apiClient<ApiResponse<unknown>>(`/api/admin/content/topics/${topicId}/workflow`, {
    data: { action, scheduledAt },
    method: "POST",
    token,
  });
}

export async function processScheduledPublishes(token: string) {
  return apiClient<ApiResponse<{ processedCount: number }>>("/api/admin/content/topics/workflow/process-scheduled", {
    method: "POST",
    token,
  });
}
