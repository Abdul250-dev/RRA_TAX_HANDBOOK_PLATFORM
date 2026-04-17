"use client";

import { useMemo, useRef, useState } from "react";
import { X } from "lucide-react";

import { inviteUser } from "../../lib/api/users";
import type { InviteUserRequest } from "../../types/user";

interface InviteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  token?: string;
}

function generateUsername(firstName: string, employeeId: string) {
  const normalizedPrefix = firstName.trim().toLowerCase().replace(/[^a-z0-9]/g, "");
  const normalizedEmployeeId = employeeId.trim().toLowerCase().replace(/[^a-z0-9]/g, "");

  if (!normalizedPrefix || !normalizedEmployeeId) {
    return "";
  }

  return normalizedPrefix.slice(0, 3) + normalizedEmployeeId;
}

export function InviteUserModal({ isOpen, onClose, onSuccess, token }: InviteUserModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successUsername, setSuccessUsername] = useState<string | null>(null);
  const formRef = useRef<HTMLFormElement>(null);
  const [firstName, setFirstName] = useState("");
  const [employeeId, setEmployeeId] = useState("");

  const generatedUsername = useMemo(
    () => generateUsername(firstName, employeeId),
    [employeeId, firstName],
  );

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccessUsername(null);
    setIsLoading(true);

    const formData = new FormData(event.currentTarget);
    const data: InviteUserRequest = {
      employeeId: (formData.get("employeeId") as string) || "",
      firstName: (formData.get("firstName") as string) || "",
      lastName: (formData.get("lastName") as string) || "",
      email: (formData.get("email") as string) || "",
      roleName: (formData.get("roleName") as string) || "",
      preferredLocale: (formData.get("preferredLocale") as string) || "EN",
      phoneNumber: (formData.get("phoneNumber") as string) || "",
      department: (formData.get("department") as string) || "",
      position: (formData.get("position") as string) || "",
    };

    if (!data.employeeId.trim()) {
      setError("Employee ID is required");
      setIsLoading(false);
      return;
    }
    if (!data.firstName.trim()) {
      setError("First name is required");
      setIsLoading(false);
      return;
    }
    if (!data.lastName.trim()) {
      setError("Last name is required");
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
      await inviteUser(data, token);
      setSuccessUsername(generateUsername(data.firstName, data.employeeId));
      formRef.current?.reset();
      setFirstName("");
      setEmployeeId("");
      setTimeout(() => {
        onClose();
        onSuccess();
      }, 1800);
    } catch (submissionError) {
      const errorMessage = submissionError instanceof Error
        ? submissionError.message
        : "Failed to invite user. Please try again.";
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    formRef.current?.reset();
    setError(null);
    setSuccessUsername(null);
    setFirstName("");
    setEmployeeId("");
    onClose();
  };

  if (!isOpen) {
    return null;
  }

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

        {successUsername && (
          <div className="invite-user-modal-message invite-user-modal-success">
            <span>✓</span>
            <p>
              Invitation created successfully. Generated username: <strong>{successUsername}</strong>.
            </p>
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
            <label htmlFor="employeeId">Employee ID</label>
            <input
              id="employeeId"
              name="employeeId"
              type="text"
              placeholder="e.g. RRA-EDT-001"
              disabled={isLoading}
              onChange={(event) => setEmployeeId(event.target.value)}
              required
              value={employeeId}
            />
          </div>

          <div className="form-group">
            <label htmlFor="firstName">First Name</label>
            <input
              id="firstName"
              name="firstName"
              type="text"
              placeholder="e.g. Abdul"
              disabled={isLoading}
              onChange={(event) => setFirstName(event.target.value)}
              required
              value={firstName}
            />
          </div>

          <div className="form-group">
            <label htmlFor="lastName">Last Name</label>
            <input
              id="lastName"
              name="lastName"
              type="text"
              placeholder="e.g. Nshuti"
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
              placeholder="e.g. nshuti@rra.gov.rw"
              disabled={isLoading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="roleName">Role</label>
            <select id="roleName" name="roleName" disabled={isLoading} required>
              <option value="">Select a role</option>
              <option value="ADMIN">Admin</option>
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

          <div className="form-group">
            <label htmlFor="phoneNumber">Phone Number</label>
            <input
              id="phoneNumber"
              name="phoneNumber"
              type="tel"
              placeholder="e.g. +250788000010"
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="department">Department</label>
            <input
              id="department"
              name="department"
              type="text"
              placeholder="e.g. Content"
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="position">Position</label>
            <input
              id="position"
              name="position"
              type="text"
              placeholder="e.g. Editor"
              disabled={isLoading}
            />
          </div>

          <div className="invite-user-username-preview">
            <span>Generated username</span>
            <strong>{generatedUsername || "Complete employee ID and first name to preview"}</strong>
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
            The user will receive an invitation email with their activation link and generated username.
          </p>
        </div>
      </div>
    </div>
  );
}
