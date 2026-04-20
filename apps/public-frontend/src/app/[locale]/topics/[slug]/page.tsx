import Link from "next/link";
import { notFound } from "next/navigation";

import { ApiRequestError, getHandbookTopicBySlug } from "../../../../lib/api/handbook";

function renderBlockBody(body: string | null) {
  if (!body) {
    return <p>No content available yet.</p>;
  }

  return body.split("\n").filter(Boolean).map((paragraph, index) => (
    <p key={`${paragraph.slice(0, 20)}-${index}`}>{paragraph}</p>
  ));
}

export default async function TopicPage({
  params,
}: {
  params: Promise<{ locale: string; slug: string }>;
}) {
  const { locale, slug } = await params;

  try {
    const topic = await getHandbookTopicBySlug(locale, slug);

    return (
      <div className="page-stack">
        <nav className="breadcrumb-bar" aria-label="Breadcrumb">
          <Link href={`/${locale}`}>Handbook</Link>
          <span className="bc-sep">/</span>
          <span>{topic.title}</span>
        </nav>

        <section className="page-intro">
          <span className="section-type">{topic.topicType.replaceAll("_", " ")}</span>
          <h1 className="page-heading">{topic.title}</h1>
          <p className="page-subtitle">
            {topic.summary || topic.introText || "Published topic content."}
          </p>
        </section>

        {topic.introText ? (
          <section className="content-panel content-panel-plain">
            <h2 className="section-h2">Introduction</h2>
            <div className="rich-copy">{renderBlockBody(topic.introText)}</div>
          </section>
        ) : null}

        {topic.blocks.map((block) => (
          <section key={block.id} className="content-panel content-panel-plain" id={block.anchorKey}>
            <div className="panel-heading">
              <h2 className="section-h2">{block.title}</h2>
              <span>{block.blockType.replaceAll("_", " ")}</span>
            </div>
            <div className="rich-copy">{renderBlockBody(block.body)}</div>
          </section>
        ))}
      </div>
    );
  } catch (error) {
    if (!(error instanceof ApiRequestError) || error.status === 404) {
      notFound();
    }

    notFound();
  }
}
