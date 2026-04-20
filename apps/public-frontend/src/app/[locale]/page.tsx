import Link from "next/link";

import { getHandbookSections } from "../../lib/api/handbook";
import { routes } from "../../lib/constants/routes";
import type { HandbookSectionSummary } from "../../types/handbook";

function buildSectionTree(sections: HandbookSectionSummary[]) {
  return sections
    .filter((section) => section.parentId === null)
    .map((section) => ({
      ...section,
      children: sections
        .filter((candidate) => candidate.parentId === section.id)
        .sort((a, b) => a.sortOrder - b.sortOrder),
    }))
    .sort((a, b) => a.sortOrder - b.sortOrder);
}

export default async function HomePage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  let topLevelSections: Array<HandbookSectionSummary & { children: HandbookSectionSummary[] }> = [];
  let loadFailed = false;

  try {
    const sections = await getHandbookSections(locale);
    topLevelSections = buildSectionTree(sections);
  } catch {
    loadFailed = true;
  }

  return (
    <div className="page-stack">
      <section className="page-intro">
        <h1 className="page-heading">Tax Handbook</h1>
        <p className="page-subtitle">
          Browse the Rwanda Revenue Authority handbook by section and open each
          published topic using the same information structure defined in the CMS.
        </p>
      </section>

      {loadFailed ? (
        <section className="content-panel">
          <h2 className="section-h2">Content unavailable</h2>
          <div className="rich-copy">
            <p>
              The handbook service could not be reached. Verify the backend is running and the
              public frontend has a valid API base URL.
            </p>
          </div>
        </section>
      ) : (
        <section className="home-grid">
          {topLevelSections.map((section) => (
            <article key={section.id} className="home-card">
              <span className="section-type">{section.type.replaceAll("_", " ")}</span>
              <h2 className="home-card-title">{section.name}</h2>
              <p className="home-card-desc">{section.summary || "Published handbook section."}</p>

              <div className="inner-nav">
                <Link className="inner-nav-item" href={routes.section(locale, section.slug)}>
                  Open section
                </Link>
                {section.children.map((child) => (
                  <Link
                    key={child.id}
                    className="inner-nav-item"
                    href={routes.section(locale, child.slug)}
                  >
                    {child.name}
                  </Link>
                ))}
              </div>
            </article>
          ))}
        </section>
      )}
    </div>
  );
}
