"use client";

import { useRouter } from "next/navigation";
import { useState, useRef, useEffect } from "react";
import { MoreVertical } from "lucide-react";
import { InviteUserModal } from "../../components/admin/InviteUserModal";
import { DataTable } from "../../components/admin/DataTable";
import { ConfirmDialog } from "../../components/admin/ConfirmDialog";
import type { User, UserStatus, UserSummary } from "../../types/user";

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

function formatDate(dateString?: string) {
  if (!dateString) return "—";
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  } catch {
    return "—";
  }
}

interface ActionMenuProps {
  user: User;
  onEdit: (user: User) => void;
  onDeactivate: (user: User) => void;
  onReactivate: (user: User) => void;
  onRemove: (user: User) => void;
  onSuspend: (user: User) => void;
  onCancel: (user: User) => void;
  onResend: (user: User) => void;
  onRestore: (user: User) => void;
}

function ActionMenu({
  user,
  onEdit,
  onDeactivate,
  onReactivate,
  onRemove,
  onSuspend,
  onCancel,
  onResend,
  onRestore,
}: ActionMenuProps) {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen]);

  return (
    <div className="action-menu-wrapper" ref={menuRef}>
      <button
        className="action-menu-button"
        onClick={() => setIsOpen(!isOpen)}
        title="Actions"
        type="button"
      >
        <MoreVertical size={18} strokeWidth={2} />
      </button>

      {isOpen && (
        <div className="action-menu-dropdown">
          {/* Edit/Update - Available for all statuses */}
          <button
            className="action-menu-item"
            onClick={() => {
              onEdit(user);
              setIsOpen(false);
            }}
            type="button"
          >
            <span className="action-menu-icon">✎</span>
            Edit / Update
          </button>

          {/* ACTIVE Status Actions */}
          {user.status === "ACTIVE" && (
            <>
              <button
                className="action-menu-item action-menu-item-warning"
                onClick={() => {
                  onDeactivate(user);
                  setIsOpen(false);
                }}
                type="button"
              >
                <span className="action-menu-icon">⊘</span>
                Deactivate
              </button>

              <button
                className="action-menu-item action-menu-item-warning"
                onClick={() => {
                  onSuspend(user);
                  setIsOpen(false);
                }}
                type="button"
              >
                <span className="action-menu-icon">⫸</span>
                Suspend
              </button>

              <button
                className="action-menu-item action-menu-item-danger"
                onClick={() => {
                  onRemove(user);
                  setIsOpen(false);
                }}
                type="button"
              >
                <span className="action-menu-icon">✕</span>
                Remove
              </button>
            </>
          )}

          {/* PENDING Status Actions */}
          {user.status === "PENDING" && (
            <>
              <button
                className="action-menu-item action-menu-item-success"
                onClick={() => {
                  onResend(user);
                  setIsOpen(false);
                }}
                type="button"
              >
                <span className="action-menu-icon">↻</span>
                Resend Invite
              </button>

              <button
                className="action-menu-item action-menu-item-danger"
                onClick={() => {
                  onCancel(user);
                  setIsOpen(false);
                }}
                type="button"
              >
                <span className="action-menu-icon">✕</span>
                Cancel Invite
              </button>
            </>
          )}

          {/* SUSPENDED Status Actions */}
          {user.status === "SUSPENDED" && (
            <button
              className="action-menu-item action-menu-item-success"
              onClick={() => {
                onReactivate(user);
                setIsOpen(false);
              }}
              type="button"
            >
              <span className="action-menu-icon">↻</span>
              Reactivate
            </button>
          )}

          {/* DEACTIVATED Status Actions */}
          {user.status === "DEACTIVATED" && (
            <button
              className="action-menu-item action-menu-item-success"
              onClick={() => {
                onRestore(user);
                setIsOpen(false);
              }}
              type="button"
            >
              <span className="action-menu-icon">↻</span>
              Restore
            </button>
          )}
        </div>
      )}
    </div>
  );
}

interface UsersPageClientProps {
  summary: UserSummary;
  users: User[];
  currentStatus: string;
  currentPage: number;
  pageSize: number;
  params: {
    status?: string;
    search?: string;
  };
  statusParam?: string;
  hasNextPage: boolean;
  token?: string;
}

type DialogType = null | "deactivate" | "reactivate" | "remove" | "suspend" | "cancel" | "resend" | "restore";

export function UsersPageClient({
  summary,
  users,
  currentStatus,
  currentPage,
  pageSize,
  params,
  hasNextPage,
  token,
}: UsersPageClientProps) {
  const router = useRouter();
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [activeDialog, setActiveDialog] = useState<{
    type: DialogType;
    user: User | null;
    isLoading: boolean;
  }>({
    type: null,
    user: null,
    isLoading: false,
  });

  // Build a URL preserving status and search params, with a given page
  function pageUrl(page: number, overrideStatus?: string) {
    const p = new URLSearchParams();
    const status = overrideStatus ?? params.status;
    if (status && status !== "All") p.set("status", status);
    if (params.search) p.set("search", params.search);
    p.set("page", String(page));
    return `?${p.toString()}`;
  }

  const statusFilters = [
    { label: "All", value: "All" },
    { label: "Active", value: "ACTIVE" },
    { label: "Pending", value: "PENDING" },
    { label: "Suspended", value: "SUSPENDED" },
    { label: "Deactivated", value: "DEACTIVATED" },
  ];

  const handleInviteSuccess = () => {
    router.refresh();
  };

  const handleEdit = (user: User) => {
    router.push(`/users/${user.id}/edit`);
  };

  const handleDeactivateClick = (user: User) => {
    setActiveDialog({ type: "deactivate", user, isLoading: false });
  };

  const handleReactivateClick = (user: User) => {
    setActiveDialog({ type: "reactivate", user, isLoading: false });
  };

  const handleRemoveClick = (user: User) => {
    setActiveDialog({ type: "remove", user, isLoading: false });
  };

  const handleSuspendClick = (user: User) => {
    setActiveDialog({ type: "suspend", user, isLoading: false });
  };

  const handleCancelClick = (user: User) => {
    setActiveDialog({ type: "cancel", user, isLoading: false });
  };

  const handleResendClick = (user: User) => {
    setActiveDialog({ type: "resend", user, isLoading: false });
  };

  const handleRestoreClick = (user: User) => {
    setActiveDialog({ type: "restore", user, isLoading: false });
  };

  const handleDialogConfirm = async () => {
    if (!activeDialog.user || !token) return;

    setActiveDialog((prev) => ({ ...prev, isLoading: true }));

    try {
      const action = activeDialog.type;
      const endpoint = `/api/users/${activeDialog.user.id}/${action}`;

      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (response.ok) {
        setActiveDialog({ type: null, user: null, isLoading: false });
        router.refresh();
      } else {
        alert(`Failed to ${action} user. Please try again.`);
        setActiveDialog((prev) => ({ ...prev, isLoading: false }));
      }
    } catch (error) {
      console.error("Error:", error);
      alert("Error processing action. Please try again.");
      setActiveDialog((prev) => ({ ...prev, isLoading: false }));
    }
  };

  const handleDialogCancel = () => {
    setActiveDialog({ type: null, user: null, isLoading: false });
  };

  const getDialogConfig = () => {
    if (!activeDialog.user) return null;

    switch (activeDialog.type) {
      case "deactivate":
        return {
          title: "Deactivate User",
          message: `Are you sure you want to deactivate ${activeDialog.user.fullName}? They will no longer be able to sign in.`,
          confirmText: "Deactivate",
          isDangerous: true,
        };
      case "suspend":
        return {
          title: "Suspend User",
          message: `Are you sure you want to suspend ${activeDialog.user.fullName}? They will be temporarily unable to access the system.`,
          confirmText: "Suspend",
          isDangerous: true,
        };
      case "reactivate":
        return {
          title: "Reactivate User",
          message: `Are you sure you want to reactivate ${activeDialog.user.fullName}? They will be able to sign in again.`,
          confirmText: "Reactivate",
          isDangerous: false,
        };
      case "restore":
        return {
          title: "Restore User",
          message: `Are you sure you want to restore ${activeDialog.user.fullName}? They will be able to sign in again.`,
          confirmText: "Restore",
          isDangerous: false,
        };
      case "remove":
        return {
          title: "Remove User",
          message: `Are you sure you want to remove ${activeDialog.user.fullName}? This action will mark them as removed.`,
          confirmText: "Remove",
          isDangerous: true,
        };
      case "cancel":
        return {
          title: "Cancel Invite",
          message: `Are you sure you want to cancel the invite for ${activeDialog.user.fullName}? They will need to be invited again.`,
          confirmText: "Yes, Cancel Invite",
          isDangerous: true,
        };
      case "resend":
        return {
          title: "Resend Invite",
          message: `Are you sure you want to resend the invite to ${activeDialog.user.fullName}?`,
          confirmText: "Resend",
          isDangerous: false,
        };
      default:
        return null;
    }
  };

  const dialogConfig = getDialogConfig();

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
          {statusFilters.map((filter) => (
            <a
              key={filter.value}
              href={pageUrl(0, filter.value === "All" ? undefined : filter.value)}
              className={`users-filter-pill ${
                currentStatus === filter.value || (filter.value === "All" && currentStatus === "All")
                  ? "users-filter-pill-active"
                  : ""
              }`}
            >
              {filter.label}
            </a>
          ))}
        </div>

        <div className="users-table-wrapper">
          <table className="users-table">
            <thead>
              <tr>
                <th className="col-employee">Employee #</th>
                <th className="col-name">Name</th>
                <th className="col-email">Email</th>
                <th className="col-role">Role</th>
                <th className="col-status">Status</th>
                <th className="col-date">Created At</th>
                <th className="col-actions">Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="users-table-row">
                  <td className="col-employee">
                    <span className="employee-code">{user.userCode}</span>
                  </td>
                  <td className="col-name">
                    <div className="user-info">
                      <span className="user-avatar-badge">{initials(user.fullName)}</span>
                      <div className="user-name">{user.fullName}</div>
                    </div>
                  </td>
                  <td className="col-email">
                    <span className="user-email-cell">{user.email}</span>
                  </td>
                  <td className="col-role">
                    <span className={`user-role-pill ${roleClassName(user.roleName)}`}>
                      {user.roleName.replace(/_/g, " ")}
                    </span>
                  </td>
                  <td className="col-status">
                    <span className={`user-status-pill ${statusClassName(user.status)}`}>
                      {formatStatus(user.status)}
                    </span>
                  </td>
                  <td className="col-date">
                    <span className="created-date">{formatDate(user.createdAt)}</span>
                  </td>
                  <td className="col-actions">
                    <ActionMenu
                      user={user}
                      onEdit={handleEdit}
                      onDeactivate={handleDeactivateClick}
                      onReactivate={handleReactivateClick}
                      onRemove={handleRemoveClick}
                      onSuspend={handleSuspendClick}
                      onCancel={handleCancelClick}
                      onResend={handleResendClick}
                      onRestore={handleRestoreClick}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

          <div className="users-table-footer">
          <span>
            {params.search ? (
              <>Showing {users.length} result{users.length !== 1 ? "s" : ""} for "{params.search}"</>
            ) : (
              <>Showing {users.length === 0 ? 0 : currentPage * pageSize + 1}–{currentPage * pageSize + users.length} results</>
            )}
          </span>

          <div className="users-pagination">
            <a
              href={currentPage > 0 ? pageUrl(currentPage - 1) : "#"}
              className={`users-page-button users-page-nav-button ${
                currentPage === 0 ? "users-page-button-muted" : ""
              }`}
              aria-disabled={currentPage === 0}
            >
              ← Previous
            </a>

            <span className="users-page-button users-page-button-active">
              {currentPage + 1}
            </span>

            <a
              href={hasNextPage ? pageUrl(currentPage + 1) : "#"}
              className={`users-page-button users-page-nav-button ${
                !hasNextPage ? "users-page-button-muted" : ""
              }`}
              aria-disabled={!hasNextPage}
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

      {dialogConfig && (
        <ConfirmDialog
          isOpen={activeDialog.type !== null}
          title={dialogConfig.title}
          message={dialogConfig.message}
          confirmText={dialogConfig.confirmText}
          cancelText="Cancel"
          isDangerous={dialogConfig.isDangerous}
          isLoading={activeDialog.isLoading}
          onConfirm={handleDialogConfirm}
          onCancel={handleDialogCancel}
        />
      )}
    </>
  );
}
