"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const languageOptions = [
  { locale: "en", label: "English", flag: "UK" },
  { locale: "rw", label: "Kinyarwanda", flag: "RW" },
  { locale: "fr", label: "Francais", flag: "FR" },
] as const;

function buildLocaleHref(pathname: string, nextLocale: string) {
  const segments = pathname.split("/").filter(Boolean);

  if (segments.length === 0) {
    return `/${nextLocale}`;
  }

  segments[0] = nextLocale;
  return `/${segments.join("/")}`;
}

export function LanguageSwitcher({ locale }: { locale: string }) {
  const pathname = usePathname();
  const activeLanguage =
    languageOptions.find((option) => option.locale === locale) ?? languageOptions[0];

  return (
    <div className="lang-dropdown">
      <button className="util-button lang-trigger" type="button" aria-haspopup="menu">
        <span className={`flag-icon flag-${activeLanguage.flag.toLowerCase()}`} aria-hidden="true" />
        <span>{activeLanguage.label}</span>
        <svg width="9" height="9" viewBox="0 0 10 10" fill="currentColor" aria-hidden="true">
          <path d="M1 3l4 4 4-4" />
        </svg>
      </button>

      <div className="lang-menu" role="menu" aria-label="Language options">
        {languageOptions.map((option) => (
          <Link
            key={option.locale}
            href={buildLocaleHref(pathname, option.locale)}
            className="lang-menu-item"
            role="menuitem"
          >
            <span className={`flag-icon flag-${option.flag.toLowerCase()}`} aria-hidden="true" />
            <span>{option.label}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
