import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { AUTH_ROLE_COOKIE, AUTH_TOKEN_COOKIE } from "../../lib/api/auth";
import { canManageRoles } from "../../lib/authz";

export default async function RolesPage() {
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_TOKEN_COOKIE)?.value;
  const role = cookieStore.get(AUTH_ROLE_COOKIE)?.value;

  if (!token) {
    redirect("/login");
  }
  if (!canManageRoles(role)) {
    redirect("/dashboard");
  }

  return <main>Admin roles scaffold.</main>;
}
