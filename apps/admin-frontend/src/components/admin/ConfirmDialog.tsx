"use client";

import React, { useEffect } from "react";

interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  isDangerous?: boolean;
  onConfirm: () => void | Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export function ConfirmDialog({
  isOpen,
  title,
  message,
  confirmText = "Confirm",
  cancelText = "Cancel",
  isDangerous = false,
  onConfirm,
  onCancel,
  isLoading = false,
}: ConfirmDialogProps) {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !isLoading) {
        onCancel();
      }
    };

    if (isOpen) {
      document.addEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "hidden";
    }

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "unset";
    };
  }, [isOpen, onCancel, isLoading]);

  if (!isOpen) {
    return null;
  }

  return (
    <>
      <div className="dialog-overlay" onClick={() => !isLoading && onCancel()} />
      <div className="dialog-container">
        <div className="dialog-content">
          <h2 className="dialog-title">{title}</h2>
          <p className="dialog-message">{message}</p>
          
          <div className="dialog-actions">
            <button
              type="button"
              onClick={onCancel}
              className="dialog-button dialog-button-secondary"
              disabled={isLoading}
            >
              {cancelText}
            </button>
            <button
              type="button"
              onClick={onConfirm}
              className={isDangerous ? "dialog-button dialog-button-danger" : "dialog-button"}
              disabled={isLoading}
            >
              {isLoading ? "Processing..." : confirmText}
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
