export type AppRole = "ADMIN" | "EDITOR" | "REVIEWER" | "PUBLISHER" | "AUDITOR";

export function canManageUsers(role?: string | null) {
  return role === "ADMIN";
}

export function canManageRoles(role?: string | null) {
  return role === "ADMIN";
}

export function canViewAuditLogs(role?: string | null) {
  return role === "ADMIN" || role === "AUDITOR";
}
