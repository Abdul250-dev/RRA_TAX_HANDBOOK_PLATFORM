import type { User, UserActivity, UserInviteResponse, UserSummary } from "../../types/user";
import { apiClient } from "./axios";

interface ApiResponse<T> {
  message: string;
  data: T;
}

export async function getUsers(token?: string, params?: { status?: string; search?: string }) {
  const query = new URLSearchParams();

  if (params?.status) {
    query.set("status", params.status);
  }

  if (params?.search) {
    query.set("search", params.search);
  }

  const suffix = query.toString() ? `?${query.toString()}` : "";
  return apiClient<User[]>(`/api/users${suffix}`, { token });
}

export async function getUserSummary(token?: string) {
  return apiClient<UserSummary>("/api/users/summary", { token });
}

export async function getUserActivity(id: number, token?: string) {
  return apiClient<UserActivity[]>(`/api/users/${id}/activity`, { token });
}

export async function removeUser(id: number, token?: string) {
  return apiClient<ApiResponse<string>>(`/api/users/${id}`, {
    method: "DELETE",
    token,
  });
}

export async function suspendUser(id: number, token?: string) {
  return apiClient<ApiResponse<User>>(`/api/users/${id}/suspend`, {
    method: "POST",
    token,
  });
}

export async function reactivateUser(id: number, token?: string) {
  return apiClient<ApiResponse<User>>(`/api/users/${id}/reactivate`, {
    method: "POST",
    token,
  });
}

export async function restoreUser(id: number, token?: string) {
  return apiClient<ApiResponse<UserInviteResponse>>(`/api/users/${id}/restore`, {
    method: "POST",
    token,
  });
}

export async function resendInvite(id: number, token?: string) {
  return apiClient<ApiResponse<UserInviteResponse>>(`/api/users/${id}/resend`, {
    method: "POST",
    token,
  });
}
