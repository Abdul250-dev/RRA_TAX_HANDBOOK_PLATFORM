import Link from "next/link";

import { routes } from "../../lib/constants/routes";

const navigationItems = [
  { href: routes.dashboard, label: "Dashboard", icon: "DB", active: true },
  { href: routes.users, label: "Users", icon: "US" },
  { href: routes.content, label: "Content", icon: "CT" },
  { href: routes.roles, label: "Roles", icon: "RL" },
  { href: routes.auditLogs, label: "Audit Logs", icon: "AL" },
  { href: routes.settings, label: "Settings", icon: "ST" },
];

export function AdminSidebar() {
  return (
    <aside className="admin-sidebar">
      <div className="brand-lockup">
        <span className="brand-mark" aria-hidden="true" />
        <div className="brand-text">
          <span className="brand-title">RRA Admin</span>
          <span className="brand-subtitle">Tax Handbook Platform</span>
        </div>
      </div>

      <nav className="sidebar-nav" aria-label="Primary">
        {navigationItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`nav-item${item.active ? " nav-item-active" : ""}`}
          >
            <span className="nav-icon" aria-hidden="true">
              {item.icon}
            </span>
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>

      <section className="sidebar-note">
        <h3>Keep Publishing Clean</h3>
        <p>
          Reusable blocks, clear workflow states, and localized content will keep the
          handbook easy to maintain as the platform grows.
        </p>
        <button className="pill-button note-button" type="button">
          Open Guidelines
        </button>
      </section>
    </aside>
  );
}
