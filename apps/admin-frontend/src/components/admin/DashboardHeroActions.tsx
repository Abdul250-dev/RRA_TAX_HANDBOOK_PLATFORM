"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { InviteUserModal } from "./InviteUserModal";

export function DashboardHeroActions({ token }: { token?: string }) {
  const router = useRouter();
  const [isInviteOpen, setIsInviteOpen] = useState(false);

  return (
    <>
      <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
        <button className="pill-button" type="button">
          Create Content
        </button>
        <button
          className="pill-button pill-button-secondary"
          type="button"
          onClick={() => setIsInviteOpen(true)}
        >
          Invite User
        </button>
      </div>

      <InviteUserModal
        isOpen={isInviteOpen}
        onClose={() => setIsInviteOpen(false)}
        onSuccess={() => router.refresh()}
        token={token}
      />
    </>
  );
}
