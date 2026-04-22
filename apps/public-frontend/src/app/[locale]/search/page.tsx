import Link from "next/link";
import type { ReactNode } from "react";

import { searchContent, SearchRequestError } from "../../../lib/api/search";
import { routes } from "../../../lib/constants/routes";
import type {
  PublicSearchDocumentResult,
  PublicSearchFaqResult,
  PublicSearchResponse,
  PublicSearchResult,
} from "../../../types/handbook";

function getResultHref(locale: string, result: PublicSearchResult, group: "sections" | "topics" | "guides") {
  if (group === "sections") {
    return routes.section(locale, result.slug);
  }
  if (group === "guides") {
    return routes.guide(locale, result.slug);
  }
  return routes.topic(locale, result.slug);
}

function ResultGroup({
  title,
  emptyText,
  children,
}: {
  title: string;
  emptyText: string;
  children: ReactNode;
}) {
  return (
    <section className="content-panel">
      <h2 className="section-h2">{title}</h2>
      {children || <p className="page-subtitle">{emptyText}</p>}
    </section>
  );
}

function LinkedResults({
  locale,
  group,
  results,
}: {
  locale: string;
  group: "sections" | "topics" | "guides";
  results: PublicSearchResult[];
}) {
  if (results.length === 0) {
    return null;
  }

  return (
    <div className="topic-list">
      {results.map((result) => (
        <Link
          key={`${group}-${result.id}`}
          className="topic-row"
          href={getResultHref(locale, result, group)}
        >
          <div>
            <strong>{result.title}</strong>
            <p>{result.summary || "Open this result to read more."}</p>
          </div>
          <span>{result.type.replaceAll("_", " ")}</span>
        </Link>
      ))}
    </div>
  );
}

function FaqResults({ results }: { results: PublicSearchFaqResult[] }) {
  if (results.length === 0) {
    return null;
  }

  return (
    <div className="rich-copy">
      {results.map((faq) => (
        <details key={faq.id}>
          <summary>{faq.question}</summary>
          <p>{faq.answer}</p>
        </details>
      ))}
    </div>
  );
}

function DocumentResults({ results }: { results: PublicSearchDocumentResult[] }) {
  if (results.length === 0) {
    return null;
  }

  return (
    <div className="inner-nav">
      {results.map((document) => (
        <a key={document.id} className="inner-nav-item" href={document.fileUrl}>
          {document.title}
        </a>
      ))}
    </div>
  );
}

function totalResults(results: PublicSearchResponse) {
  return (
    results.sections.length +
    results.topics.length +
    results.guides.length +
    results.faqs.length +
    results.documents.length
  );
}

export default async function SearchPage({
  params,
  searchParams,
}: {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ q?: string }>;
}) {
  const { locale } = await params;
  const { q = "" } = await searchParams;
  const query = q.trim();
  let results: PublicSearchResponse | null = null;
  let errorMessage: string | null = null;

  try {
    results = await searchContent(locale, query);
  } catch (error) {
    errorMessage = error instanceof SearchRequestError
      ? "Search is temporarily unavailable. Please try again in a moment."
      : "Something went wrong while searching the handbook.";
  }

  return (
    <div className="page-stack">
      <nav className="breadcrumb-bar" aria-label="Breadcrumb">
        <Link href={`/${locale}`}>Handbook</Link>
        <span className="bc-sep">/</span>
        <span>Search</span>
      </nav>

      <section className="page-intro">
        <span className="section-type">Search</span>
        <h1 className="page-heading">Search handbook</h1>
        <p className="page-subtitle">
          Search published handbook sections, topics, guides, FAQs, and documents.
        </p>
      </section>

      <form className="search-panel" action={`/${locale}/search`}>
        <label className="search-label" htmlFor="handbook-search">
          Search term
        </label>
        <div className="search-row">
          <input
            id="handbook-search"
            className="search-input"
            name="q"
            type="search"
            placeholder="Search tax, VAT, PAYE..."
            defaultValue={query}
          />
          <button className="search-submit" type="submit">
            Search
          </button>
        </div>
        <p className="page-subtitle">Enter at least two characters.</p>
      </form>

      {query.length === 0 ? (
        <section className="content-panel">
          <h2 className="section-h2">Start a search</h2>
          <p className="page-subtitle">
            Use keywords, tax names, acronyms, or service names to find published handbook content.
          </p>
        </section>
      ) : null}

      {query.length > 0 && query.length < 2 ? (
        <section className="content-panel">
          <h2 className="section-h2">Search term too short</h2>
          <p className="page-subtitle">Please enter at least two characters.</p>
        </section>
      ) : null}

      {errorMessage ? (
        <section className="content-panel">
          <h2 className="section-h2">Search unavailable</h2>
          <p className="page-subtitle">{errorMessage}</p>
        </section>
      ) : null}

      {results ? (
        <>
          <section className="content-panel">
            <h2 className="section-h2">Results for &quot;{results.query}&quot;</h2>
            <p className="page-subtitle">
              {totalResults(results)} result{totalResults(results) === 1 ? "" : "s"} found.
            </p>
          </section>

          {totalResults(results) === 0 ? (
            <section className="content-panel">
              <h2 className="section-h2">No results found</h2>
              <p className="page-subtitle">
                Try a different keyword or check the spelling of the tax or service name.
              </p>
            </section>
          ) : (
            <>
              <ResultGroup title="Sections" emptyText="No matching sections.">
                <LinkedResults locale={locale} group="sections" results={results.sections} />
              </ResultGroup>

              <ResultGroup title="Topics" emptyText="No matching topics.">
                <LinkedResults locale={locale} group="topics" results={results.topics} />
              </ResultGroup>

              <ResultGroup title="Guides" emptyText="No matching guides.">
                <LinkedResults locale={locale} group="guides" results={results.guides} />
              </ResultGroup>

              <ResultGroup title="FAQs" emptyText="No matching FAQs.">
                <FaqResults results={results.faqs} />
              </ResultGroup>

              <ResultGroup title="Documents" emptyText="No matching documents.">
                <DocumentResults results={results.documents} />
              </ResultGroup>
            </>
          )}
        </>
      ) : null}
    </div>
  );
}
