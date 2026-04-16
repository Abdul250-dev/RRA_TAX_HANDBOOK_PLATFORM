import { apiClient } from "./axios";

export interface UserSummary {
  totalUsers: number;
  activeUsers: number;
  pendingUsers: number;
  suspendedUsers: number;
  deactivatedUsers: number;
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

export interface TopicSummary {
  id: number;
  sectionId: number;
  title: string;
  slug: string;
  summary: string;
  topicType: string;
  status: string;
  sortOrder: number;
}

export async function getUserSummary(token?: string) {
  return apiClient<UserSummary>("/api/users/summary", { token });
}

export async function getContentSummary(token?: string) {
  return apiClient<ContentSummary>("/api/admin/content/summary", { token });
}

export async function getReviewQueue(token?: string) {
  return apiClient<TopicSummary[]>("/api/admin/content/topics/review-queue?locale=EN", { token });
}

export async function getPublishQueue(token?: string) {
  return apiClient<TopicSummary[]>("/api/admin/content/topics/publish-queue?locale=EN", { token });
}
