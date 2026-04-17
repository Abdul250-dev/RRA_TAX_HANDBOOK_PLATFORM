"use client";

import { useState } from "react";
import { acceptInvite } from "../../lib/api/users";

interface InviteFormProps {
  token: string;
  inviteeName: string;
  username?: string | null;
}

export function InviteForm({ token, inviteeName, username }: InviteFormProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    const formData = new FormData(e.currentTarget);
    const password = formData.get("password") as string;
    const confirmPassword = formData.get("confirmPassword") as string;

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setIsLoading(true);
    try {
      await acceptInvite(token, password);
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to set password. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  if (success) {
    return (
      <div className="login-panel-frame" style={{ textAlign: "center" }}>
        <div className="invite-user-modal-message invite-user-modal-success" style={{ margin: 0 }}>
          <span>✓</span>
          <p>Your account is ready! You can now <a href="/login" style={{ color: "var(--rra-blue-secondary)", fontWeight: 700 }}>sign in</a>.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="login-panel-frame">
      <div className="login-panel-intro">
        <h3>Welcome, {inviteeName}</h3>
        <p>Set a password to activate your account.</p>
        {username ? <p>Your username will be <strong>{username}</strong>.</p> : null}
      </div>

      {error && (
        <div className="login-error">{error}</div>
      )}

      <form className="login-form" onSubmit={handleSubmit}>
        <div className="login-field">
          <span>Password</span>
          <input
            name="password"
            type="password"
            placeholder="At least 8 characters"
            required
            disabled={isLoading}
          />
        </div>

        <div className="login-field">
          <span>Confirm Password</span>
          <input
            name="confirmPassword"
            type="password"
            placeholder="Repeat your password"
            required
            disabled={isLoading}
          />
        </div>

        <div className="login-actions">
          <button type="submit" className="login-submit" disabled={isLoading}>
            {isLoading ? "Setting up…" : "Get Connected"}
          </button>
        </div>
      </form>
    </div>
  );
}
