"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState } from "react";

import { staticSearchIndex } from "../../lib/content/staticHandbook";
import { LanguageSwitcher } from "../common/LanguageSwitcher";

export function PublicChrome({
  children,
  locale,
}: {
  children: React.ReactNode;
  locale: string;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [query, setQuery] = useState("");

  const matches = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (normalized.length < 2) return [];
    return staticSearchIndex
      .filter((item) => `${item.title} ${item.text}`.toLowerCase().includes(normalized))
      .slice(0, 8);
  }, [query]);

  useEffect(() => {
    if (!isSearchOpen) return;
    const timer = window.setTimeout(() => inputRef.current?.focus(), 20);
    return () => window.clearTimeout(timer);
  }, [isSearchOpen]);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") setIsSearchOpen(false);
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setIsSearchOpen(true);
      }
    }
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, []);

  function goToResult(href: string) {
    setIsSearchOpen(false);
    setQuery("");
    router.push(`/${locale}${href}`);
  }

  return (
    <div className="site-shell">
      <header className="site-header">
        <div className="util-bar" aria-label="Language and search">
          <LanguageSwitcher locale={locale} />
          <button className="util-button" onClick={() => setIsSearchOpen(true)} type="button">
            Search
          </button>
        </div>

        <div className="site-header-main">
          <Link className="site-logo" href={`/${locale}`} aria-label="RRA Tax Handbook home">
            <img className="site-logo-image" src="/assets/bg_rra_logo.png" alt="Rwanda Revenue Authority" />
          </Link>
          <div className="header-divider" aria-hidden="true" />
          <div className="header-title" aria-live="polite">
            <span>{pathname.includes("/topics/") ? "Tax Handbook Topic" : "Tax Handbook-Edition 2025"}</span>
          </div>
          <a className="back-link" href="https://www.rra.gov.rw" target="_blank" rel="noreferrer">
            Visit RRA
          </a>
          <button className="menu-btn" onClick={() => setIsSearchOpen(true)} type="button" aria-label="Search handbook">
            <span />
          </button>
        </div>
      </header>

      <main className="site-main">{children}</main>

      <footer className="site-footer">
        <span>Copyright © 2025 All Rights Reserved by Rwanda Revenue Authority</span>
        <div className="footer-links">
          <a href="https://www.rra.gov.rw" target="_blank" rel="noreferrer">
            About us
          </a>
          <Link href={`/${locale}/contact`}>Contact us</Link>
          <Link href={`/${locale}/search`}>Feedback</Link>
        </div>
      </footer>

      <div
        className={`search-overlay ${isSearchOpen ? "open" : ""}`}
        onClick={() => setIsSearchOpen(false)}
        role="presentation"
      >
        <section className="search-box" onClick={(event) => event.stopPropagation()} aria-label="Search handbook">
          <div className="search-box-header">
            <h2>Search the handbook</h2>
            <button type="button" onClick={() => setIsSearchOpen(false)}>
              Close
            </button>
          </div>
          <input
            ref={inputRef}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === "Enter" && matches[0]) goToResult(matches[0].href);
            }}
            placeholder="Search taxes, VAT, PAYE, declarations..."
            type="search"
          />
          <div className="search-results">
            {query.trim().length < 2 ? (
              <p>Type at least two characters to search.</p>
            ) : matches.length ? (
              matches.map((item) => (
                <button key={item.href} onClick={() => goToResult(item.href)} type="button">
                  <strong>{item.title}</strong>
                  <span>Click to view</span>
                </button>
              ))
            ) : (
              <p>No results found.</p>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
