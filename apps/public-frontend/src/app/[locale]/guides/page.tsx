import Link from "next/link";

import { getPublicGuides } from "../../../lib/api/handbook";
import { routes } from "../../../lib/constants/routes";

export default async function GuidesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  const guides = await getPublicGuides(locale);

  return (
    <div className="page-stack">
      <nav className="breadcrumb-bar" aria-label="Breadcrumb">
        <Link href={`/${locale}`}>Handbook</Link>
        <span className="bc-sep">/</span>
        <span>User guides and examples</span>
      </nav>

      <section className="page-intro">
        <span className="section-type">GUIDES</span>
        <h1 className="page-heading">User guides and examples</h1>
        <p className="page-subtitle">
          Step-by-step guides and practical examples to help taxpayers understand filing,
          payment, and compliance procedures.
        </p>
      </section>

      <section className="content-panel">
        <h2 className="section-h2">Available guides</h2>
        {guides.length ? (
          <div className="topic-list">
            {guides.map((guide) => (
              <Link key={guide.id} className="topic-row" href={routes.guide(locale, guide.slug)}>
                <div>
                  <strong>{guide.title}</strong>
                  <p>{guide.summary || "Open this guide for practical steps and examples."}</p>
                </div>
                <span>{guide.topicType.replaceAll("_", " ")}</span>
              </Link>
            ))}
          </div>
        ) : (
          <div className="empty-state">No published guides are available yet.</div>
        )}
      </section>
    </div>
  );
}
