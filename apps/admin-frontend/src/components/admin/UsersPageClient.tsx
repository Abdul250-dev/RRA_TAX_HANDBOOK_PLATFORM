"use client";

import { useState } from "react";
import { Pencil, Trash2 } from "lucide-react";
import { InviteUserModal } from "../../components/admin/InviteUserModal";
import { DataTable } from "../../components/admin/DataTable";
import type { User, UserStatus, UserSummary } from "../../types/user";

function formatStatus(status: UserStatus) {
  return status.charAt(0) + status.slice(1).toLowerCase();
}

function statusClassName(status: UserStatus) {
  if (status === "ACTIVE") {
    return "user-status-active";
  }

  if (status === "INVITED") {
    return "user-status-invited";
  }

  if (status === "SUSPENDED") {
    return "user-status-suspended";
  }

  return "user-status-removed";
}

function roleClassName(roleName: string) {
  if (roleName === "ADMIN" || roleName === "SUPER_ADMIN") {
    return "user-role-admin";
  }

  if (roleName === "EDITOR" || roleName === "PUBLISHER") {
    return "user-role-editor";
  }

  if (roleName === "REVIEWER") {
    return "user-role-reviewer";
  }

  if (roleName === "AUDITOR") {
    return "user-role-auditor";
  }

  return "user-role-default";
}

function initials(name: string) {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("");
}

function actionLabel(status: UserStatus) {
  if (status === "INVITED") {
    return "Resend invite";
  }

  if (status === "SUSPENDED") {
    return "Reactivate";
  }

  if (status === "REMOVED") {
    return "Restore";
  }

  return "Edit";
}

interface UsersPageClientProps {
  summary: UserSummary;
  users: User[];
  currentStatus: string;
  currentPage: number;
  pageSize: number;
  params: {
    status?: string;
  };
  statusParam?: string;
  totalResults: number;
  token?: string;
}

export function UsersPageClient({
  summary,
  users,
  currentStatus,
  currentPage,
  pageSize,
  params,
  statusParam,
  totalResults,
  token,
}: UsersPageClientProps) {
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const statusFilters = [
    { label: "All", value: "All" },
    { label: "Active", value: "ACTIVE" },
    { label: "Invited", value: "INVITED" },
    { label: "Suspended", value: "SUSPENDED" },
    { label: "Removed", value: "REMOVED" },
  ];

  const handleInviteSuccess = () => {
    setRefreshTrigger((prev) => prev + 1);
    // In a real app, you'd refetch the data here
  };

  return (
    <>
      <section className="page-hero">
        <div>
          <h1>Users</h1>
          <p>
            Manage system access using the statuses and actions supported by the existing
            backend user endpoints.
          </p>
        </div>

        <div className="users-hero-actions">
          <button className="pill-button pill-button-secondary" type="button">
            Export List
          </button>
          <button
            className="pill-button"
            type="button"
            onClick={() => setIsInviteModalOpen(true)}
          >
            Invite User
          </button>
        </div>
      </section>

      <section className="users-summary-grid">
        <article className="users-summary-card">
          <span className="users-summary-label">Total users</span>
          <strong>{summary.totalUsers}</strong>
          <p>Directory entries returned by the backend user service.</p>
        </article>

        <article className="users-summary-card">
          <span className="users-summary-label">Active</span>
          <strong>{summary.activeUsers}</strong>
          <p>Accounts able to sign in and perform their assigned roles.</p>
        </article>

        <article className="users-summary-card">
          <span className="users-summary-label">Invited</span>
          <strong>{summary.invitedUsers}</strong>
          <p>Users awaiting invite acceptance through the invite flow.</p>
        </article>

        <article className="users-summary-card">
          <span className="users-summary-label">Suspended / Removed</span>
          <strong>{summary.suspendedUsers + summary.removedUsers}</strong>
          <p>Restricted accounts that can be reactivated or restored.</p>
        </article>
      </section>

      <section className="panel-card users-table-panel">
        <div className="users-filter-strip" role="tablist" aria-label="User status filter">
          {statusFilters.map((filter) => (
            <a
              key={filter.value}
              href={filter.value === "All" ? "?page=0" : `?status=${filter.value}&page=0`}
              className={`users-filter-pill ${currentStatus === filter.label ? "users-filter-pill-active" : ""}`}
            >
              {filter.label}
            </a>
          ))}
        </div>

        <DataTable
          columns={[
            {
              key: "name",
              header: "Name",
              render: (row: User) => (
                <div className="user-name-cell">
                  <span className="user-avatar-badge">{initials(row.fullName)}</span>
                  <div className="users-table-identity">
                    <strong>{row.fullName}</strong>
                    <span>{row.email}</span>
                  </div>
                </div>
              ),
            },
            {
              key: "role",
              header: "Role",
              render: (row: User) => (
                <span className={`user-role-pill ${roleClassName(row.roleName)}`}>
                  {row.roleName.replace(/_/g, " ")}
                </span>
              ),
            },
            {
              key: "userCode",
              header: "User Code",
              render: (row: User) => <span className="user-code-cell">{row.userCode}</span>,
            },
            {
              key: "status",
              header: "Status",
              render: (row: User) => (
                <span className={`user-status-pill ${statusClassName(row.status)}`}>
                  {formatStatus(row.status)}
                </span>
              ),
            },
            {
              key: "locale",
              header: "Locale",
              render: (row: User) => <span className="metric-muted">{row.preferredLocale}</span>,
            },
            {
              key: "source",
              header: "Source",
              render: (row: User) => <span className="metric-muted">{row.source}</span>,
            },
            {
              key: "actions",
              header: "",
              render: (row: User) => (
                <div className="user-table-actions">
                  <button
                    className="icon-action-button"
                    title={actionLabel(row.status)}
                    type="button"
                  >
                    <Pencil size={18} strokeWidth={1.9} />
                  </button>
                  <button
                    className="icon-action-button icon-action-button-danger"
                    title="Remove user"
                    type="button"
                  >
                    <Trash2 size={18} strokeWidth={1.9} />
                  </button>
                </div>
              ),
            },
          ]}
          rows={users}
        />

        <div className="users-table-footer">
          <span>
            Showing {users.length === 0 ? 0 : currentPage * pageSize + 1}-
            {currentPage * pageSize + users.length} of ~{totalResults} results
          </span>

          <div className="users-pagination">
            <span>Rows</span>
            <button className="users-page-size" type="button">
              {pageSize}
              <span aria-hidden="true">⌄</span>
            </button>
            <a
              href={
                currentPage > 0
                  ? `?status=${params.status || ""}${params.status ? "&" : "?"}page=${currentPage - 1}`.replace("?&", "?")
                  : "#"
              }
              className={`users-page-button users-page-nav-button ${currentPage === 0 ? "users-page-button-muted" : ""}`}
            >
              ← Previous
            </a>
            {Array.from({ length: Math.min(3, Math.ceil(totalResults / pageSize)) }).map((_, i) => (
              <a
                key={i}
                href={`?status=${params.status || ""}${params.status ? "&" : "?"}page=${i}`.replace("?&", "?").replace("page=", statusParam ? "page=" : "page=")}
                className={`users-page-button ${currentPage === i ? "users-page-button-active" : ""}`}
              >
                {i + 1}
              </a>
            ))}
            <a
              href={`?status=${params.status || ""}${params.status ? "&" : "?"}page=${currentPage + 1}`.replace("?&", "?")}
              className="users-page-button users-page-nav-button"
            >
              Next →
            </a>
          </div>
        </div>
      </section>

      <InviteUserModal
        isOpen={isInviteModalOpen}
        onClose={() => setIsInviteModalOpen(false)}
        onSuccess={handleInviteSuccess}
        token={token}
      />
    </>
  );
}
