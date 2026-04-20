export type AppRole =
  | "ADMIN"
  | "EDITOR"
  | "REVIEWER"
  | "PUBLISHER"
  | "AUDITOR"
  | "CONTENT_OFFICER"
  | "VIEWER"
  | "PUBLIC";

export function canManageUsers(role?: string | null) {
  return role === "ADMIN";
}

export function canManageRoles(role?: string | null) {
  return role === "ADMIN";
}

export function canViewAuditLogs(role?: string | null) {
  return role === "ADMIN" || role === "AUDITOR";
}

export function canViewContent(role?: string | null) {
  return ["ADMIN", "EDITOR", "REVIEWER", "PUBLISHER", "AUDITOR", "CONTENT_OFFICER", "VIEWER"].includes(
    role ?? "",
  );
}

export function canCreateContent(role?: string | null) {
  return role === "ADMIN" || role === "EDITOR" || role === "CONTENT_OFFICER";
}

export function canReviewContent(role?: string | null) {
  return role === "ADMIN" || role === "REVIEWER";
}

export function canPublishContent(role?: string | null) {
  return role === "ADMIN" || role === "PUBLISHER";
}
