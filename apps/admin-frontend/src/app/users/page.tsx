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
