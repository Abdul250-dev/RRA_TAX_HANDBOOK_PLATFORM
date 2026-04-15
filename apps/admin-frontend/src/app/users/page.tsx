import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { Pencil, Trash2 } from "lucide-react";

import { AdminLayout } from "../../components/layout/AdminLayout";
import { DataTable } from "../../components/admin/DataTable";
import { AUTH_TOKEN_COOKIE } from "../../lib/api/auth";
import { getUsers, getUserSummary } from "../../lib/api/users";
import type { User, UserStatus, UserSummary } from "../../types/user";

const fallbackUsers: User[] = [
  {
    id: 1,
    userCode: "AMUK",
    fullName: "Aline Mukamana",
    email: "aline.mukamana@rra.gov.rw",
    roleName: "ADMIN",
    preferredLocale: "EN",
    source: "LOCAL",
    status: "ACTIVE",
  },
];

const fallbackSummary: UserSummary = {
  totalUsers: 1,
  activeUsers: 1,
  invitedUsers: 0,
  suspendedUsers: 0,
  removedUsers: 0,
};

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

async function getUsersData(status?: string, page: number = 0, pageSize: number = 10) {
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect("/login");
  }

  try {
    const [users, summary] = await Promise.all([
      getUsers(token, { status: status || undefined, page, pageSize }),
      getUserSummary(token),
    ]);
    return { summary, users };
  } catch {
    return { summary: fallbackSummary, users: fallbackUsers };
  }
}

interface UsersPageProps {
  searchParams: Promise<{
    status?: string;
    page?: string;
    pageSize?: string;
  }>;
}

export default async function UsersPage({ searchParams }: UsersPageProps) {
  const params = await searchParams;
  const currentStatus = params.status || "All";
  const currentPage = parseInt(params.page || "0", 10);
  const pageSize = parseInt(params.pageSize || "10", 10);
  const statusParam = params.status ? (params.status === "All" ? undefined : params.status) : undefined;

  const { summary, users } = await getUsersData(statusParam, currentPage, pageSize);

  // Calculate total pages based on the filtered results
  // For accurate pagination, we'd need total count from backend, but using current data for now
  const totalResults = users.length === pageSize ? (currentPage + 1) * pageSize + 1 : (currentPage * pageSize) + users.length;

  const statusFilters = [
    { label: "All", value: "All" },
    { label: "Active", value: "ACTIVE" },
    { label: "Invited", value: "INVITED" },
    { label: "Suspended", value: "SUSPENDED" },
    { label: "Removed", value: "REMOVED" },
  ];

  return (
    <AdminLayout>
      <main className="dashboard-stack">
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
            <button className="pill-button" type="button">
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
                    <button className="icon-action-button" title={actionLabel(row.status)} type="button">
                      <Pencil size={18} strokeWidth={1.9} />
                    </button>
                    <button className="icon-action-button icon-action-button-danger" title="Remove user" type="button">
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
              Showing {users.length === 0 ? 0 : currentPage * pageSize + 1}-{currentPage * pageSize + users.length} of ~{totalResults} results
            </span>

            <div className="users-pagination">
              <span>Rows</span>
              <button className="users-page-size" type="button">
                {pageSize}
                <span aria-hidden="true"></span>
              </button>
              <a
                href={currentPage > 0 ? `?status=${params.status || ""}${params.status ? "&" : "?"}page=${currentPage - 1}`.replace("?&", "?") : "#"}
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
      </main>
    </AdminLayout>
  );
}
