"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Bell, LogOut, Settings } from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

import { useAuth } from "../../hooks/useAuth";
import { clearSession } from "../../lib/api/auth";
import { routes } from "../../lib/constants/routes";

export function AdminHeader() {
  const router = useRouter();
  const pathname = usePathname();
  const { username } = useAuth();
  const searchRef = useRef<HTMLFormElement | null>(null);
  const [query, setQuery] = useState("");
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const normalizedUsername = username?.trim() ?? "";
  const isEmailUsername = normalizedUsername.includes("@");
  const adminName = isEmailUsername ? "Admin User" : normalizedUsername || "RRA Admin";
  const adminEmail = isEmailUsername ? normalizedUsername : "admin@example.com";
  const avatarLabel = isEmailUsername ? adminEmail : adminName;
  const initials = avatarLabel
    .replace(/[^a-zA-Z0-9 ]/g, " ")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("");

  const navigationItems = useMemo(
    () => [
      { href: routes.dashboard, label: "Dashboard" },
      { href: routes.users, label: "Users" },
      { href: routes.content, label: "Content" },
      { href: routes.roles, label: "Roles" },
      { href: routes.auditLogs, label: "Audit Logs" },
      { href: routes.settings, label: "Settings" },
    ],
    [],
  );

  const searchItems = useMemo(
    () => [
      {
        href: routes.dashboard,
        label: "Dashboard",
        description: "Overview, queues, and publishing flow",
        keywords: ["overview", "home", "stats", "summary", "queue"],
      },
      {
        href: routes.users,
        label: "Users",
        description: "Manage admin users and access",
        keywords: ["accounts", "team", "members", "permissions"],
      },
      {
        href: routes.content,
        label: "Content",
        description: "Review and publish handbook content",
        keywords: ["content", "topics", "posts", "handbook"],
      },
      {
        href: routes.roles,
        label: "Roles",
        description: "Control responsibilities and privileges",
        keywords: ["permissions", "access", "security"],
      },
      {
        href: routes.auditLogs,
        label: "Audit Logs",
        description: "Track changes and admin activity",
        keywords: ["history", "activity", "logs", "events"],
      },
      {
        href: routes.settings,
        label: "Settings",
        description: "Adjust platform and admin preferences",
        keywords: ["preferences", "config", "system"],
      },
    ],
    [],
  );

  const trimmedQuery = query.trim().toLowerCase();
  const searchResults = useMemo(() => {
    if (!trimmedQuery) {
      return searchItems.slice(0, 5);
    }

    return searchItems.filter((item) => {
      const haystack = [item.label, item.description, ...item.keywords].join(" ").toLowerCase();
      return haystack.includes(trimmedQuery);
    });
  }, [searchItems, trimmedQuery]);

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      const target = event.target as Node;

      if (searchRef.current && !searchRef.current.contains(target)) {
        setIsSearchOpen(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    return () => document.removeEventListener("mousedown", handlePointerDown);
  }, []);

  function handleLogout() {
    clearSession();
    router.replace(routes.login);
    router.refresh();
  }

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const firstResult = searchResults[0];
    if (!firstResult) {
      return;
    }

    setIsSearchOpen(false);
    router.push(firstResult.href);
  }

  function handleSearchSelect(href: string) {
    setQuery("");
    setIsSearchOpen(false);
    router.push(href);
  }

  return (
    <header className="admin-header">
      <div className="header-left">
        <div className="header-brand">
          <div className="header-brand-logo" aria-hidden="true">
            <Image
              alt="RRA Logo"
              className="rounded-full object-contain"
              height={36}
              priority
              src="/assets/bg_rra_logo.png"
              width={36}
            />
          </div>
          <div className="header-brand-copy">
            <span className="header-brand-name">RRA Admin</span>
            <span className="header-brand-subtitle">Tax Handbook Platform</span>
          </div>
        </div>
      </div>

      <nav className="header-nav" aria-label="Primary">
        {navigationItems.map((item) => {
          const isActive =
            pathname === item.href ||
            (item.href !== routes.dashboard && pathname?.startsWith(item.href));

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`header-nav-link${isActive ? " header-nav-link-active" : ""}`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="header-actions">
        <form
          className="header-search"
          onSubmit={handleSearchSubmit}
          ref={searchRef}
          role="search"
        >
          <label className="search-shell" aria-label="Search admin pages">
            <span className="search-icon" aria-hidden="true">
            
            </span>
            <input
              onChange={(event) => setQuery(event.target.value)}
              onFocus={() => setIsSearchOpen(true)}
              placeholder=" Search ..."
              type="search"
              value={query}
            />
          </label>

          {isSearchOpen ? (
            <div className="search-results" role="listbox" aria-label="Search results">
              {searchResults.length > 0 ? (
                searchResults.map((item) => (
                  <button
                    key={item.href}
                    className="search-result-item"
                    onClick={() => handleSearchSelect(item.href)}
                    type="button"
                  >
                    <span className="search-result-label">{item.label}</span>
                    <span className="search-result-description">{item.description}</span>
                  </button>
                ))
              ) : (
                <div className="search-empty-state">
                  No matching page found. Try users, content, roles, or logs.
                </div>
              )}
            </div>
          ) : null}
        </form>

      

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              aria-label="Open profile menu"
              className="rounded-full"
              size="icon"
              variant="ghost"
            >
              <Avatar className="h-[38px] w-[38px]">
                <AvatarFallback>{initials || "RA"}</AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>

          <DropdownMenuContent align="end" className="admin-profile-dropdown" sideOffset={12}>
            <div className="profile-dropdown-header">
              <strong>{adminName}</strong>
              <span>{adminEmail}</span>
            </div>

            <DropdownMenuSeparator />

            <DropdownMenuGroup>
              <DropdownMenuItem
                className="profile-dropdown-item"
                onSelect={() => handleSearchSelect(routes.settings)}
              >
                <span className="profile-item-icon" aria-hidden="true">
                  <Settings size={18} strokeWidth={1.9} />
                </span>
                <span className="profile-item-label">Settings</span>
              </DropdownMenuItem>

              <DropdownMenuItem className="profile-dropdown-item">
                <span className="profile-item-icon" aria-hidden="true">
                  <Bell size={18} strokeWidth={1.9} />
                </span>
                <span className="profile-item-label">Notifications</span>
                <span className="profile-item-badge">4</span>
              </DropdownMenuItem>
            </DropdownMenuGroup>

            <DropdownMenuSeparator />

            <DropdownMenuGroup>
              <DropdownMenuItem
                className="profile-dropdown-item profile-dropdown-item-logout"
                onSelect={handleLogout}
              >
                <span className="profile-item-icon" aria-hidden="true">
                  <LogOut size={18} strokeWidth={1.9} />
                </span>
                <span className="profile-item-label">Log out</span>
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
