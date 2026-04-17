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
    employeeId: "EMP-0001",
    userCode: "AMUK",
    username: "aline.mukamana",
    firstName: "Aline",
    lastName: "Mukamana",
    fullName: "Aline Mukamana",
    email: "aline.mukamana@rra.gov.rw",
    phoneNumber: null,
    department: "Operations",
    position: "Platform Administrator",
    roleName: "ADMIN",
    preferredLocale: "EN",
    source: "LOCAL",
    status: "ACTIVE",
  },
  {
    id: 2,
    employeeId: "EMP-0002",
    userCode: "JHAB",
    username: "jean.habimana",
    firstName: "Jean Claude",
    lastName: "Habimana",
    fullName: "Jean Claude Habimana",
    email: "jean.habimana@rra.gov.rw",
    phoneNumber: null,
    department: "Content",
    position: "Content Editor",
    roleName: "EDITOR",
    preferredLocale: "EN",
    source: "LOCAL",
    status: "ACTIVE",
  },
  {
    id: 3,
    employeeId: "EMP-0003",
    userCode: "DUWI",
    username: "diane.uwimana",
    firstName: "Diane",
    lastName: "Uwimana",
    fullName: "Diane Uwimana",
    email: "diane.uwimana@rra.gov.rw",
    phoneNumber: null,
    department: "Review",
    position: "Content Reviewer",
    roleName: "REVIEWER",
    preferredLocale: "FR",
    source: "LOCAL",
    status: "PENDING",
  },
  {
    id: 4,
    employeeId: "EMP-0004",
    userCode: "ENSH",
    username: "eric.nshimiyimana",
    firstName: "Eric",
    lastName: "Nshimiyimana",
    fullName: "Eric Nshimiyimana",
    email: "eric.nshimiyimana@rra.gov.rw",
    phoneNumber: null,
    department: "Audit",
    position: "Audit Officer",
    roleName: "AUDITOR",
    preferredLocale: "EN",
    source: "LOCAL",
    status: "SUSPENDED",
  },
  {
    id: 5,
    employeeId: "EMP-0005",
    userCode: "SKAY",
    username: "sandrine.kayitesi",
    firstName: "Sandrine",
    lastName: "Kayitesi",
    fullName: "Sandrine Kayitesi",
    email: "sandrine.kayitesi@rra.gov.rw",
    phoneNumber: null,
    department: "Publishing",
    position: "Publishing Lead",
    roleName: "PUBLISHER",
    preferredLocale: "RW",
    source: "LOCAL",
    status: "DEACTIVATED",
  },
];

const fallbackSummary: UserSummary = {
  totalUsers: fallbackUsers.length,
  activeUsers: fallbackUsers.filter((user) => user.status === "ACTIVE").length,
  pendingUsers: fallbackUsers.filter((user) => user.status === "PENDING").length,
  suspendedUsers: fallbackUsers.filter((user) => user.status === "SUSPENDED").length,
  deactivatedUsers: fallbackUsers.filter((user) => user.status === "DEACTIVATED").length,
};

function formatStatus(status: UserStatus) {
  return status.charAt(0) + status.slice(1).toLowerCase();
}

function statusClassName(status: UserStatus) {
  if (status === "ACTIVE") {
    return "user-status-active";
  }

  if (status === "PENDING") {
    return "user-status-invited";
  }

  if (status === "SUSPENDED") {
    return "user-status-suspended";
  }

  return "user-status-removed";
}

function roleClassName(roleName: string) {
  if (roleName === "ADMIN") {
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
  if (status === "PENDING") {
    return "Resend invite";
  }

  if (status === "SUSPENDED") {
    return "Reactivate";
  }

  if (status === "DEACTIVATED") {
    return "Restore";
  }

  return "Edit";
}

async function getUsersData() {
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect("/login");
  }

  try {
    const [users, summary] = await Promise.all([getUsers(token), getUserSummary(token)]);
    return { summary, users };
  } catch {
    return { summary: fallbackSummary, users: fallbackUsers };
  }
}

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

          <div className="users-table-footer">
            <span>
              Showing 1-{visibleUsers.length} of {users.length} results
            </span>

            <div className="users-pagination">
              <span>Rows</span>
              <button className="users-page-size" type="button">
                10
                <span aria-hidden="true">⌄</span>
              </button>
              <button className="users-page-button users-page-button-muted" type="button">
                Previous
              </button>
              <button className="users-page-button users-page-button-active" type="button">
                1
              </button>
              <button className="users-page-button" type="button">
                2
              </button>
              <button className="users-page-button" type="button">
                3
              </button>
              <button className="users-page-button" type="button">
                Next
              </button>
            </div>
          </div>
        </section>
      </main>
    </AdminLayout>
  );
}
