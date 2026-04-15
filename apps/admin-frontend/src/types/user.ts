export type UserStatus = "ACTIVE" | "INVITED" | "SUSPENDED" | "REMOVED";

export interface User {
  id: number;
  userCode: string;
  fullName: string;
  email: string;
  roleName: string;
  preferredLocale: string;
  source: string;
  status: UserStatus;
}

export interface UserSummary {
  totalUsers: number;
  activeUsers: number;
  invitedUsers: number;
  suspendedUsers: number;
  removedUsers: number;
}

export interface UserInviteResponse {
  id: number;
  email: string;
  inviteToken: string;
  expiresAt: string;
  status: UserStatus;
}

export interface InviteUserRequest {
  fullName: string;
  email: string;
  roleName: string;
  preferredLocale?: string;
}

export interface UserActivity {
  id: number;
  action: string;
  actor: string;
  targetEmail: string;
  details: string;
  createdAt: string;
}
