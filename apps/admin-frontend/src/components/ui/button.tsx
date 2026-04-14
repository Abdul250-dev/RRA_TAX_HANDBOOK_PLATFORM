"use client";

import * as React from "react";

import { cn } from "@/lib/utils/cn";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "default" | "ghost";
  size?: "default" | "icon";
};

const variantClasses: Record<NonNullable<ButtonProps["variant"]>, string> = {
  default:
    "bg-[#334d7a] text-white shadow-sm hover:bg-[#2b426a] focus-visible:ring-[#334d7a]/25",
  ghost: "bg-transparent text-inherit hover:bg-black/5 focus-visible:ring-black/10",
};

const sizeClasses: Record<NonNullable<ButtonProps["size"]>, string> = {
  default: "h-10 px-4 py-2",
  icon: "h-10 w-10 p-0",
};

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, type = "button", variant = "default", size = "default", ...props }, ref) => (
    <button
      ref={ref}
      className={cn(
        "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors outline-none focus-visible:ring-4 disabled:pointer-events-none disabled:opacity-50",
        variantClasses[variant],
        sizeClasses[size],
        className,
      )}
      type={type}
      {...props}
    />
  ),
);
Button.displayName = "Button";
