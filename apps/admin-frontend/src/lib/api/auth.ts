import { apiClient } from "./axios";

export const AUTH_TOKEN_COOKIE = "rra_admin_token";
export const AUTH_ROLE_COOKIE = "rra_admin_role";
export const AUTH_USER_COOKIE = "rra_admin_user";

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  username: string;
  token: string;
  role: string;
}

function writeCookie(name: string, value: string, days = 1) {
  const expiresAt = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString();
  document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expiresAt}; path=/; SameSite=Lax`;
}

export function persistSession(session: LoginResult) {
  writeCookie(AUTH_TOKEN_COOKIE, session.token);
  writeCookie(AUTH_ROLE_COOKIE, session.role);
  writeCookie(AUTH_USER_COOKIE, session.username);
  localStorage.setItem("rra-admin-session", JSON.stringify(session));
}

export function clearSession() {
  document.cookie = `${AUTH_TOKEN_COOKIE}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; SameSite=Lax`;
  document.cookie = `${AUTH_ROLE_COOKIE}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; SameSite=Lax`;
  document.cookie = `${AUTH_USER_COOKIE}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; SameSite=Lax`;
  localStorage.removeItem("rra-admin-session");
}

export function getStoredSession(): LoginResult | null {
  const rawSession = localStorage.getItem("rra-admin-session");

  if (!rawSession) {
    return null;
  }

  try {
    return JSON.parse(rawSession) as LoginResult;
  } catch {
    clearSession();
    return null;
  }
}

export async function login(payload: LoginPayload) {
  const session = await apiClient<LoginResult>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });

  persistSession(session);
  return session;
}
