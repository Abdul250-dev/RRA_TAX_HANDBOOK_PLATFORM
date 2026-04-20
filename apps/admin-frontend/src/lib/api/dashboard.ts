import { apiClient } from "./axios";
import type { ContentSummary, TopicSummary } from "./content";

export interface UserSummary {
  totalUsers: number;
  activeUsers: number;
  pendingUsers: number;
  suspendedUsers: number;
  deactivatedUsers: number;
}

export type { ContentSummary, TopicSummary };

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
