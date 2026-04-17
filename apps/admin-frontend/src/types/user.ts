export type UserStatus = "ACTIVE" | "PENDING" | "SUSPENDED" | "DEACTIVATED";

export interface User {
  id: number;
  employeeId: string;
  userCode: string;
  username: string | null;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phoneNumber?: string | null;
  department?: string | null;
  position?: string | null;
  roleName: string;
  preferredLocale: string;
  source: string;
  status: UserStatus;
  createdAt?: string;
  isActive?: boolean;
  isLocked?: boolean;
  failedLoginAttempts?: number;
  lastLoginAt?: string | null;
}

export interface UserSummary {
  totalUsers: number;
  activeUsers: number;
  pendingUsers: number;
  suspendedUsers: number;
  deactivatedUsers: number;
}

export interface UserInviteResponse {
  userId: number;
  email: string;
  inviteToken?: string | null;
  expiresAt?: string | null;
  status: UserStatus;
}

export interface InviteUserRequest {
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  roleName: string;
  preferredLocale?: string;
  phoneNumber?: string;
  department?: string;
  position?: string;
}

export interface UserActivity {
  id: number;
  action: string;
  actor: string;
  targetEmail: string;
  details: string;
  createdAt: string;
}
