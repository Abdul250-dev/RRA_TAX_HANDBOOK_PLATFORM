import Link from "next/link";

import { routes } from "../../lib/constants/routes";
import type { HandbookTopicDetail } from "../../types/handbook";

function renderBlockBody(body: string | null) {
  if (!body) {
    return <p>No content available yet.</p>;
  }

  return body.split("\n").filter(Boolean).map((paragraph, index) => (
    <p key={`${paragraph.slice(0, 20)}-${index}`}>{paragraph}</p>
  ));
}

export function TopicDetailPage({
  locale,
  topic,
}: {
  locale: string;
  topic: HandbookTopicDetail;
}) {
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

      {topic.relatedGuides.length || topic.relatedFaqs.length || topic.relatedDocuments.length ? (
        <section className="content-panel">
          <h2 className="section-h2">Related resources</h2>

          {topic.relatedGuides.length ? (
            <div className="topic-list">
              {topic.relatedGuides.map((guide) => (
                <Link key={guide.id} className="topic-row" href={routes.topic(locale, guide.slug)}>
                  <div>
                    <strong>{guide.title}</strong>
                    <p>{guide.summary || "Open this guide for practical steps and examples."}</p>
                  </div>
                  <span>Guide</span>
                </Link>
              ))}
            </div>
          ) : null}

          {topic.relatedFaqs.length ? (
            <div className="rich-copy">
              {topic.relatedFaqs.map((faq) => (
                <details key={faq.id}>
                  <summary>{faq.question}</summary>
                  <p>{faq.answer}</p>
                </details>
              ))}
            </div>
          ) : null}

          {topic.relatedDocuments.length ? (
            <div className="inner-nav">
              {topic.relatedDocuments.map((document) => (
                <a key={document.id} className="inner-nav-item" href={document.fileUrl}>
                  {document.title}
                </a>
              ))}
            </div>
          ) : null}
        </section>
      ) : null}

      {topic.lastUpdated ? (
        <p className="last-updated">
          This page was last updated on:{" "}
          {new Intl.DateTimeFormat("en-GB").format(new Date(topic.lastUpdated))}
        </p>
      ) : null}
    </div>
  );
}
