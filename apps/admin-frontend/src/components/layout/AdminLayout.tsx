import { AdminHeader } from "./AdminHeader";

export function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="admin-shell">
      <div className="admin-main">
        <AdminHeader />
        <div className="page-content">{children}</div>
      </div>
    </div>
  );
}
