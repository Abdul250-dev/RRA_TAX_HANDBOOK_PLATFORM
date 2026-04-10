"use client";

import { useEffect, useState } from "react";

import { getStoredSession, type LoginResult } from "../lib/api/auth";

export function useAuth() {
  const [session, setSession] = useState<LoginResult | null>(null);

  useEffect(() => {
    setSession(getStoredSession());
  }, []);

  return {
    session,
    isAuthenticated: Boolean(session?.token),
    role: session?.role ?? null,
    username: session?.username ?? null,
  };
}
