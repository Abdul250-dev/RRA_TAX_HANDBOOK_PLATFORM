"use client";

import { useRouter } from "next/navigation";

import { clearSession } from "../../lib/api/auth";
import { routes } from "../../lib/constants/routes";
import { useAuth } from "../../hooks/useAuth";

export function AdminHeader() {
  const router = useRouter();
  const { role, username } = useAuth();

  function handleLogout() {
    clearSession();
    router.replace(routes.login);
    router.refresh();
  }

  return (
    <header className="admin-header">
      <label className="search-shell" aria-label="Search">
        <span aria-hidden="true">Q</span>
        <input placeholder="Search users, topics, queues..." type="search" />
      </label>

      <div className="header-actions">
        <button className="icon-button" type="button" aria-label="Switch language">
          EN
        </button>
        <button className="icon-button" type="button" aria-label="Notifications">
          1
        </button>
        <div className="user-chip" aria-label="Current user">
          <span className="avatar">RA</span>
          <div>
            <div style={{ fontWeight: 700 }}>{username ?? "RRA Admin"}</div>
            <div style={{ color: "var(--text-muted)", fontSize: "0.82rem" }}>
              {role ?? "Authorized User"}
            </div>
          </div>
        </div>
        <button className="logout-button" onClick={handleLogout} type="button">
          Logout
        </button>
      </div>
    </header>
  );
}
