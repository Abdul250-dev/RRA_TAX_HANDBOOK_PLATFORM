import Link from "next/link";
import { notFound } from "next/navigation";

import { ApiRequestError, getHandbookSectionBySlug } from "../../lib/api/handbook";
import { routes } from "../../lib/constants/routes";

export async function SectionLandingPage({
  locale,
  slug,
}: {
  locale: string;
  slug: string;
}) {
  try {
    const section = await getHandbookSectionBySlug(locale, slug);

    return (
      <div className="page-stack">
        <nav className="breadcrumb-bar" aria-label="Breadcrumb">
          <Link href={`/${locale}`}>Handbook</Link>
          <span className="bc-sep">/</span>
          <span>{section.name}</span>
        </nav>

        <section className="page-intro">
          <span className="section-type">{section.type.replaceAll("_", " ")}</span>
          <h1 className="page-heading">{section.name}</h1>
          <p className="page-subtitle">{section.summary || "Published handbook section."}</p>
        </section>

        {section.children.length ? (
          <section className="content-panel">
            <h2 className="section-h2">Child sections</h2>
            <div className="inner-nav">
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
          </section>
        ) : null}

        {section.topics.length ? (
          <section className="content-panel">
            <h2 className="section-h2">Topics</h2>
            <div className="topic-list">
              {section.topics.map((topic) => (
                <Link key={topic.id} className="topic-row" href={routes.topic(locale, topic.slug)}>
                  <div>
                    <strong>{topic.title}</strong>
                    <p>{topic.summary || "Open this topic to read the full content."}</p>
                  </div>
                  <span>{topic.topicType.replaceAll("_", " ")}</span>
                </Link>
              ))}
            </div>
          </section>
        ) : null}
      </div>
    );
  } catch (error) {
    if (!(error instanceof ApiRequestError) || error.status === 404) {
      notFound();
    }

    notFound();
  }
}
