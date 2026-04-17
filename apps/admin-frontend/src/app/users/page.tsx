import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { AdminLayout } from "../../components/layout/AdminLayout";
import { UsersPageClient } from "../../components/admin/UsersPageClient";
import { AUTH_TOKEN_COOKIE } from "../../lib/api/auth";
import { getUsers, getUserSummary } from "../../lib/api/users";
import type { UserSummary } from "../../types/user";

const fallbackSummary: UserSummary = {
  totalUsers: 1,
  activeUsers: 1,
  invitedUsers: 0,
  suspendedUsers: 0,
  removedUsers: 0,
};

async function getUsersData(status?: string, search?: string, page: number = 0, pageSize: number = 10) {
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect("/login");
  }

  try {
    const [users, summary] = await Promise.all([
      getUsers(token, { status: status || undefined, search: search || undefined, page, pageSize }),
      getUserSummary(token),
    ]);
    return { summary, users, token };
  } catch {
    return { summary: fallbackSummary, users: [], token };
  }
}

interface UsersPageProps {
  searchParams: Promise<{
    status?: string;
    search?: string;
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
  const searchQuery = params.search || undefined;

  const { summary, users, token } = await getUsersData(statusParam, searchQuery, currentPage, pageSize);
export default async function UsersPage() {
  const { summary, users } = await getUsersData();
  const visibleUsers = users.slice(0, 10);

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
            <span className="users-summary-label">Pending</span>
            <strong>{summary.pendingUsers}</strong>
            <p>Users awaiting invite acceptance through the invite flow.</p>
          </article>

          <article className="users-summary-card">
            <span className="users-summary-label">Suspended / Deactivated</span>
            <strong>{summary.suspendedUsers + summary.deactivatedUsers}</strong>
            <p>Restricted accounts that can be reactivated or restored.</p>
          </article>
        </section>

        <section className="panel-card users-table-panel">
          <div className="users-filter-strip" role="tablist" aria-label="User status filter">
            <button className="users-filter-pill users-filter-pill-active" type="button">
              All
            </button>
            <button className="users-filter-pill" type="button">
              Active
            </button>
            <button className="users-filter-pill" type="button">
              Pending
            </button>
            <button className="users-filter-pill" type="button">
              Suspended
            </button>
            <button className="users-filter-pill" type="button">
              Deactivated
            </button>
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
                header: "Employee ID",
                render: (row: User) => <span className="user-code-cell">{row.employeeId}</span>,
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
            rows={visibleUsers}
          />

  const hasNextPage = users.length === pageSize;

  return (
    <AdminLayout>
      <main className="dashboard-stack">
        <UsersPageClient
          summary={summary}
          users={users}
          currentStatus={currentStatus}
          currentPage={currentPage}
          pageSize={pageSize}
          params={params}
          statusParam={statusParam}
          hasNextPage={hasNextPage}
          token={token}
        />
      </main>
    </AdminLayout>
  );
}
