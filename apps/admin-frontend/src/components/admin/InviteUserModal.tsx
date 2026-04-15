"use client";

import { useRef, useState } from "react";
import { X } from "lucide-react";
import { inviteUser } from "../../lib/api/users";
import type { InviteUserRequest, UserInviteResponse } from "../../types/user";

interface InviteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  token?: string;
}

export function InviteUserModal({ isOpen, onClose, onSuccess, token }: InviteUserModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setIsLoading(true);

    const formData = new FormData(e.currentTarget);
    const data: InviteUserRequest = {
      fullName: formData.get("fullName") as string,
      email: formData.get("email") as string,
      roleName: formData.get("roleName") as string,
      preferredLocale: (formData.get("preferredLocale") as string) || "EN",
    };

    // Validation
    if (!data.fullName.trim()) {
      setError("Full name is required");
      setIsLoading(false);
      return;
    }
    if (!data.email.trim()) {
      setError("Email is required");
      setIsLoading(false);
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
      setError("Please enter a valid email address");
      setIsLoading(false);
      return;
    }
    if (!data.roleName) {
      setError("Please select a role");
      setIsLoading(false);
      return;
    }

    try {
      const response = await inviteUser(data, token);
      if (response) {
        setSuccess(true);
        formRef.current?.reset();
        setTimeout(() => {
          onClose();
          onSuccess();
        }, 1500);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Failed to invite user. Please try again.";
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    formRef.current?.reset();
    setError(null);
    setSuccess(false);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="invite-user-modal-overlay">
      <div className="invite-user-modal">
        <div className="invite-user-modal-header">
          <h2>Invite New User</h2>
          <button
            type="button"
            onClick={handleClose}
            className="invite-user-modal-close"
            aria-label="Close invite user modal"
          >
            <X size={24} strokeWidth={2} />
          </button>
        </div>

        {success && (
          <div className="invite-user-modal-message invite-user-modal-success">
            <span>✓</span>
            <p>Invitation sent successfully! The user will receive an email with their invitation link.</p>
          </div>
        )}

        {error && (
          <div className="invite-user-modal-message invite-user-modal-error">
            <span>!</span>
            <p>{error}</p>
          </div>
        )}

        <form ref={formRef} onSubmit={handleSubmit} className="invite-user-form">
          <div className="form-group">
            <label htmlFor="fullName">Full Name</label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              placeholder="e.g. John Doe"
              disabled={isLoading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="e.g. john.doe@rra.gov.rw"
              disabled={isLoading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="roleName">Role</label>
            <select id="roleName" name="roleName" disabled={isLoading} required>
              <option value="">Select a role</option>
              <option value="ADMIN">Admin</option>
              <option value="SUPER_ADMIN">Super Admin</option>
              <option value="EDITOR">Editor</option>
              <option value="PUBLISHER">Publisher</option>
              <option value="REVIEWER">Reviewer</option>
              <option value="AUDITOR">Auditor</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="preferredLocale">Preferred Language</label>
            <select id="preferredLocale" name="preferredLocale" disabled={isLoading}>
              <option value="EN">English</option>
              <option value="FR">Français</option>
              <option value="RW">Kinyarwanda</option>
            </select>
          </div>

          <div className="invite-user-modal-actions">
            <button
              type="button"
              onClick={handleClose}
              className="invite-user-modal-button invite-user-modal-button-secondary"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="invite-user-modal-button invite-user-modal-button-primary"
              disabled={isLoading}
            >
              {isLoading ? "Sending Invitation..." : "Send Invitation"}
            </button>
          </div>
        </form>

        <div className="invite-user-modal-footer">
          <p>
            The user will receive an invitation email at the provided address. They'll have 24 hours to complete their profile and set their password.
          </p>
        </div>
      </div>
    </div>
  );
}
