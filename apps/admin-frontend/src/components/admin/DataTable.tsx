import React from "react";
import { AlertTriangle, User } from "lucide-react";

import { cn } from "../../lib/utils/cn";

export interface DataColumn<T> {
  key: string;
  header: string;
  render: (row: T) => React.ReactNode;
  className?: string;
  headerClassName?: string;
}

interface DataTableProps<T> {
  columns: DataColumn<T>[];
  emptyMessage?: string;
  emptyDescription?: string;
  error?: string | null;
  loading?: boolean;
  loadingMessage?: string;
  rows: T[];
  className?: string;
  striped?: boolean;
}

export function DataTable<T>({
  columns,
  emptyMessage = "No records to display right now.",
  emptyDescription = "No data matches your current selection.",
  error = null,
  loading = false,
  loadingMessage = "Loading data...",
  rows,
  className,
  striped = false,
}: DataTableProps<T>) {
  if (loading) {
    return (
      <div
        className={cn(
          "overflow-hidden rounded-[24px] border border-slate-200/80 bg-white shadow-[0_12px_34px_rgba(15,23,42,0.06)]",
          className,
        )}
      >
        <div className="flex flex-col items-center justify-center px-6 py-12 text-center">
          <div className="h-10 w-10 animate-spin rounded-full border-2 border-slate-200 border-t-[var(--primary)]" />
          <p className="mt-4 text-sm font-medium text-slate-600">{loadingMessage}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div
        className={cn(
          "overflow-hidden rounded-[24px] border border-slate-200/80 bg-white shadow-[0_12px_34px_rgba(15,23,42,0.06)]",
          className,
        )}
      >
        <div className="flex flex-col items-center justify-center px-6 py-12 text-center">
          <AlertTriangle className="mb-4 h-12 w-12 text-rose-300" />
          <h3 className="text-lg font-semibold text-slate-900">Unable to load table</h3>
          <p className="mt-2 max-w-md text-sm leading-6 text-slate-500">{error}</p>
        </div>
      </div>
    );
  }

  if (!rows.length) {
    return (
      <div
        className={cn(
          "overflow-hidden rounded-[24px] border border-slate-200/80 bg-white shadow-[0_12px_34px_rgba(15,23,42,0.06)]",
          className,
        )}
      >
        <div className="flex flex-col items-center justify-center px-6 py-12 text-center">
          <User className="mb-4 h-12 w-12 text-slate-300" />
          <h3 className="text-lg font-semibold text-slate-900">{emptyMessage}</h3>
          <p className="mt-2 max-w-md text-sm leading-6 text-slate-500">{emptyDescription}</p>
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        "overflow-hidden rounded-[24px] border border-slate-200/80 bg-white shadow-[0_12px_34px_rgba(15,23,42,0.06)]",
        className,
      )}
    >
      <div className="overflow-x-auto rounded-[22px] border border-slate-200/70">
        <table className="w-full border-separate border-spacing-0 text-sm">
          <thead className="border-b border-slate-200/80 bg-[#eef8ff]">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  className={cn(
                    "px-4 py-3.5 text-left text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-600",
                    column.headerClassName,
                  )}
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200/80 bg-white">
            {rows.map((row, index) => (
              <tr
                key={index}
                className={cn(
                  "transition-colors hover:bg-[#f8fbfe]",
                  striped && index % 2 === 1 && "bg-slate-50/40",
                )}
              >
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className={cn("px-4 py-4 text-[13px] text-slate-700 align-middle", column.className)}
                  >
                    {column.render(row)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
