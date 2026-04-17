"use client";

import { useState } from "react";
import { Button } from "../ui/button";

interface RoleFormProps {
  initialData?: { name: string; description: string };
  onSubmit: (data: { name: string; description: string }) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export function RoleForm({ initialData, onSubmit, onCancel, isLoading }: RoleFormProps) {
  const [name, setName] = useState(initialData?.name || "");
  const [description, setDescription] = useState(initialData?.description || "");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({ name, description });
  };

  return (
    <form onSubmit={handleSubmit} className="role-form">
      <div className="form-group">
        <label htmlFor="name">Role Name</label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          placeholder="Enter role name"
        />
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          required
          placeholder="Enter role description"
          rows={3}
        />
      </div>

      <div className="form-actions">
        <Button type="button" variant="ghost" onClick={onCancel} disabled={isLoading}>
          Cancel
        </Button>
        <Button type="submit" disabled={isLoading}>
          {isLoading ? "Saving..." : initialData ? "Update Role" : "Create Role"}
        </Button>
      </div>
    </form>
  );
}