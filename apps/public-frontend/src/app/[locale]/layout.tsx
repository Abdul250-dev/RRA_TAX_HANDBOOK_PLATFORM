import Link from "next/link";
import { notFound } from "next/navigation";

import { LanguageSwitcher } from "../../components/common/LanguageSwitcher";
import { routing } from "../../lib/i18n/routing";

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;

  if (!routing.locales.includes(locale)) {
    notFound();
  }

  return (
    <div className="site-shell">
      <header className="site-header">
        <div className="util-bar" aria-label="Language and search">
          <LanguageSwitcher locale={locale} />
          <button className="util-button" type="button">
            Search
          </button>
        </div>

        <div className="site-header-main">
          <Link className="site-logo" href={`/${locale}`} aria-label="RRA Tax Handbook home">
            <img
              className="site-logo-image"
              src="/assets/bg_rra_logo.png"
              alt="Rwanda Revenue Authority"
            />
          </Link>
          <div className="header-copy">
            <a
              className="back-link"
              href="https://www.rra.gov.rw"
              target="_blank"
              rel="noreferrer"
            >
              <span aria-hidden="true">←</span>
              <span>Go to rra.gov.rw</span>
            </a>
            <div className="header-title">
              <span>Tax Handbook-Edition 2025</span>
            </div>
          </div>
        </div>
      </header>

      <main className="site-main">{children}</main>

      <footer className="site-footer">
        <span>2025 RRA Tax Handbook. All Rights Reserved.</span>
        <div className="footer-links">
          <Link href={`/${locale}`}>Handbook</Link>
          <Link href={`/${locale}/articles`}>Articles</Link>
          <Link href={`/${locale}/downloads`}>Downloads</Link>
        </div>
      </footer>
    </div>
  );
}
