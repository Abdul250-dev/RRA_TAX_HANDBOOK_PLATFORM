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
    return { summary, users, token };
  } catch {
    return { summary: fallbackSummary, users: [], token };
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

  const { summary, users, token } = await getUsersData(statusParam, currentPage, pageSize);

  const totalResults = users.length === pageSize ? (currentPage + 1) * pageSize + 1 : (currentPage * pageSize) + users.length;

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
          totalResults={totalResults}
          token={token}
        />
      </main>
    </AdminLayout>
  );
}
