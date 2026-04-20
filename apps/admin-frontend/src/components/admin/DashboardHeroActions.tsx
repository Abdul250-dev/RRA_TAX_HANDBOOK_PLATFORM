"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";

import { InviteUserModal } from "./InviteUserModal";
import { canManageUsers } from "../../lib/authz";

export function DashboardHeroActions({ role, token }: { role?: string | null; token?: string }) {
  const router = useRouter();
  const [isInviteOpen, setIsInviteOpen] = useState(false);
  const showInviteAction = canManageUsers(role);

  return (
    <>
      <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
        <Link className="pill-button" href="/articles/create">
          Create Content
        </Link>
        {showInviteAction ? (
          <button
            className="pill-button pill-button-secondary"
            type="button"
            onClick={() => setIsInviteOpen(true)}
          >
            Invite User
          </button>
        ) : null}
      </div>

      {showInviteAction ? (
        <InviteUserModal
          isOpen={isInviteOpen}
          onClose={() => setIsInviteOpen(false)}
          onSuccess={() => router.refresh()}
          token={token}
        />
      ) : null}
    </>
  );
}
