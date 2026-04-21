"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  Archive,
  CheckCircle2,
  Clock,
  FilePlus2,
  FolderPlus,
  RotateCcw,
  Send,
  UploadCloud,
  XCircle,
} from "lucide-react";
import { useMemo, useState } from "react";
import type { FormEvent } from "react";

import {
  createAdminSection,
  createAdminTopic,
  createAdminTopicBlock,
  processScheduledPublishes,
  transitionAdminTopic,
  updateAdminHomepage,
  type AdminSection,
  type AdminHomepageContent,
  type ContentSummary,
  type LocaleCode,
  type TopicDetail,
  type TopicSummary,
  type TopicWorkflowAction,
} from "../../lib/api/content";
import { canCreateContent, canPublishContent, canReviewContent } from "../../lib/authz";

type ContentTab = "topics" | "sections" | "homepage" | "queues" | "roles";

interface ContentPageClientProps {
  homepage: AdminHomepageContent;
  locale: LocaleCode;
  params: {
    search?: string;
    status?: string;
    tab?: string;
  };
  publishQueue: TopicDetail[];
  reviewQueue: TopicDetail[];
  role?: string | null;
  sections: AdminSection[];
  summary: ContentSummary;
  token: string;
  topics: TopicSummary[];
}

const statusFilters = ["ALL", "DRAFT", "REVIEW", "APPROVED", "PUBLISHED", "ARCHIVED"];
const localeOptions: LocaleCode[] = ["EN", "FR", "KIN"];

const roleDetails = [
  {
    name: "EDITOR",
    scope: "Create sections, create drafts, add content blocks, and submit topics for review.",
  },
  {
    name: "CONTENT_OFFICER",
    scope: "Operational content creation role with the same drafting workflow as editors.",
  },
  {
    name: "REVIEWER",
    scope: "Review submitted topics, request changes, and approve content for publishing.",
  },
  {
    name: "PUBLISHER",
    scope: "Publish approved content, schedule releases, unpublish, and archive old topics.",
  },
  {
    name: "ADMIN",
    scope: "Full content workflow access, including create, review, publish, and override actions.",
  },
  {
    name: "AUDITOR",
    scope: "Read-only content oversight for compliance and monitoring.",
  },
  {
    name: "VIEWER",
    scope: "Read-only access to content lists, statuses, and queues.",
  },
];

function slugify(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function pageUrl(params: Record<string, string | undefined>) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value && value !== "ALL") query.set(key, value);
  });
  const serialized = query.toString();
  return serialized ? `/content?${serialized}` : "/content";
}

function formatCount(value: number) {
  return new Intl.NumberFormat("en-US").format(value);
}

function statusClassName(status: string) {
  if (status === "APPROVED" || status === "PUBLISHED") return "status-approved";
  if (status === "REVIEW") return "status-review";
  if (status === "ARCHIVED") return "user-status-removed";
  return "status-draft";
}

function permittedActions(role?: string | null, status?: string): TopicWorkflowAction[] {
  const actions: TopicWorkflowAction[] = [];
  if ((role === "ADMIN" || role === "EDITOR" || role === "CONTENT_OFFICER") && (status === "DRAFT" || status === "REVIEW")) {
    actions.push("SUBMIT_FOR_REVIEW");
  }
  if ((role === "ADMIN" || role === "REVIEWER") && status === "REVIEW") {
    actions.push("REQUEST_CHANGES", "APPROVE");
  }
  if ((role === "ADMIN" || role === "PUBLISHER") && status === "APPROVED") {
    actions.push("SCHEDULE_PUBLISH", "PUBLISH", "ARCHIVE");
  }
  if ((role === "ADMIN" || role === "PUBLISHER") && status === "PUBLISHED") {
    actions.push("UNPUBLISH", "ARCHIVE");
  }
  return actions;
}

function actionLabel(action: TopicWorkflowAction) {
  return action
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function renderParagraphs(value?: string | null) {
  const lines = value?.split("\n").map((line) => line.trim()).filter(Boolean) ?? [];
  if (lines.length === 0) return <p className="metric-muted">No content provided.</p>;

  return lines.map((line, index) => <p key={`${line.slice(0, 24)}-${index}`}>{line}</p>);
}

export function ContentPageClient({
  homepage,
  locale,
  params,
  publishQueue,
  reviewQueue,
  role,
  sections,
  summary,
  token,
  topics,
}: ContentPageClientProps) {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<ContentTab>((params.tab as ContentTab) || "topics");
  const [message, setMessage] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [isBusy, setIsBusy] = useState(false);
  const [detailTopic, setDetailTopic] = useState<TopicDetail | null>(null);
  const [scheduleByTopic, setScheduleByTopic] = useState<Record<number, string>>({});
  const [blockTopicId, setBlockTopicId] = useState<number | null>(null);
  const [blockForm, setBlockForm] = useState({ title: "", body: "" });
  const [sectionForm, setSectionForm] = useState({
    name: "",
    slug: "",
    summary: "",
    type: "MAIN",
    sortOrder: "1",
    parentId: "",
  });
  const [topicForm, setTopicForm] = useState({
    title: "",
    slug: "",
    summary: "",
    introText: "",
    sectionId: sections[0]?.id ? String(sections[0].id) : "",
    topicType: "GUIDE",
    sortOrder: "1",
    firstBlockTitle: "",
    firstBlockBody: "",
  });
  const [homepageForm, setHomepageForm] = useState({
    kicker: homepage.kicker,
    title: homepage.title,
    subtitle: homepage.subtitle,
    searchLabel: homepage.searchLabel,
    helpLabel: homepage.helpLabel,
    cards:
      homepage.cards.length > 0
        ? homepage.cards.map((card) => ({
            sectionId: String(card.sectionId),
            sortOrder: String(card.sortOrder),
            title: card.title,
            description: card.description,
          }))
        : Array.from({ length: 4 }, (_, index) => ({
            sectionId: sections[index]?.id ? String(sections[index].id) : "",
            sortOrder: String(index + 1),
            title: "",
            description: "",
          })),
  });

  const canCreate = canCreateContent(role);
  const canReview = canReviewContent(role);
  const canPublish = canPublishContent(role);
  const currentStatus = params.status || "ALL";
  const search = params.search?.trim().toLowerCase() ?? "";

  const sectionById = useMemo(() => new Map(sections.map((section) => [section.id, section])), [sections]);
  const visibleTopics = useMemo(() => {
    if (!search) return topics;
    return topics.filter((topic) =>
      [topic.title, topic.slug, topic.summary, topic.topicType, topic.status, sectionById.get(topic.sectionId)?.name]
        .join(" ")
        .toLowerCase()
        .includes(search),
    );
  }, [search, sectionById, topics]);

  async function runAction(topic: TopicSummary, action: TopicWorkflowAction) {
    setIsBusy(true);
    setMessage(null);
    try {
      const scheduledAt =
        action === "SCHEDULE_PUBLISH" && scheduleByTopic[topic.id]
          ? new Date(scheduleByTopic[topic.id]).toISOString()
          : undefined;
      await transitionAdminTopic(token, topic.id, action, scheduledAt);
      setMessage({ tone: "success", text: `${actionLabel(action)} completed for "${topic.title}".` });
      router.refresh();
    } catch (error) {
      setMessage({ tone: "error", text: error instanceof Error ? error.message : "Workflow action failed." });
    } finally {
      setIsBusy(false);
    }
  }

  async function handleProcessScheduled() {
    setIsBusy(true);
    setMessage(null);
    try {
      const result = await processScheduledPublishes(token);
      setMessage({ tone: "success", text: `${result.data.processedCount} scheduled publish item(s) processed.` });
      router.refresh();
    } catch (error) {
      setMessage({ tone: "error", text: error instanceof Error ? error.message : "Could not process scheduled publishes." });
    } finally {
      setIsBusy(false);
    }
  }

  async function handleCreateSection(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsBusy(true);
    setMessage(null);
    try {
      await createAdminSection(token, {
        locale,
        name: sectionForm.name,
        parentId: sectionForm.parentId ? Number(sectionForm.parentId) : null,
        slug: sectionForm.slug || slugify(sectionForm.name),
        sortOrder: Number(sectionForm.sortOrder || 1),
        summary: sectionForm.summary,
        type: sectionForm.type as "MAIN" | "GROUP" | "SUBGROUP",
      });
      setSectionForm({ name: "", slug: "", summary: "", type: "MAIN", sortOrder: "1", parentId: "" });
      setMessage({ tone: "success", text: "Section created as a draft." });
      router.refresh();
    } catch (error) {
      setMessage({ tone: "error", text: error instanceof Error ? error.message : "Could not create section." });
    } finally {
      setIsBusy(false);
    }
  }

  async function handleCreateTopic(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsBusy(true);
    setMessage(null);
    try {
      const created = await createAdminTopic(token, {
        introText: topicForm.introText,
        locale,
        sectionId: Number(topicForm.sectionId),
        slug: topicForm.slug || slugify(topicForm.title),
        sortOrder: Number(topicForm.sortOrder || 1),
        summary: topicForm.summary,
        title: topicForm.title,
        topicType: topicForm.topicType as "TAX_TOPIC" | "SERVICE_TOPIC" | "STATIC_TOPIC" | "LANDING_TOPIC" | "GUIDE",
      });

      if (topicForm.firstBlockBody.trim()) {
        await createAdminTopicBlock(token, created.data.id, {
          anchorKey: slugify(topicForm.firstBlockTitle || "overview"),
          blockType: "RICH_TEXT",
          body: topicForm.firstBlockBody,
          locale,
          sortOrder: 1,
          title: topicForm.firstBlockTitle || "Overview",
        });
      }

      setTopicForm({
        title: "",
        slug: "",
        summary: "",
        introText: "",
        sectionId: sections[0]?.id ? String(sections[0].id) : "",
        topicType: "GUIDE",
        sortOrder: "1",
        firstBlockTitle: "",
        firstBlockBody: "",
      });
      setMessage({ tone: "success", text: "Topic created as a draft." });
      router.refresh();
    } catch (error) {
      setMessage({ tone: "error", text: error instanceof Error ? error.message : "Could not create topic." });
    } finally {
      setIsBusy(false);
    }
  }

  async function handleAddBlock(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!blockTopicId) return;
    setIsBusy(true);
    setMessage(null);
    try {
      await createAdminTopicBlock(token, blockTopicId, {
        anchorKey: slugify(blockForm.title || "content-block"),
        blockType: "RICH_TEXT",
        body: blockForm.body,
        locale,
        sortOrder: 1,
        title: blockForm.title,
      });
      setBlockTopicId(null);
      setBlockForm({ title: "", body: "" });
      setMessage({ tone: "success", text: "Content block added to topic." });
      router.refresh();
    } catch (error) {
      setMessage({ tone: "error", text: error instanceof Error ? error.message : "Could not add content block." });
    } finally {
      setIsBusy(false);
    }
  }

  async function handleUpdateHomepage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsBusy(true);
    setMessage(null);
    try {
      await updateAdminHomepage(token, {
        locale,
        kicker: homepageForm.kicker,
        title: homepageForm.title,
        subtitle: homepageForm.subtitle,
        searchLabel: homepageForm.searchLabel,
        helpLabel: homepageForm.helpLabel,
        cards: homepageForm.cards.map((card, index) => ({
          sectionId: Number(card.sectionId),
          sortOrder: Number(card.sortOrder || index + 1),
          title: card.title,
          description: card.description,
        })),
      });
      setMessage({ tone: "success", text: "Homepage content updated." });
      router.refresh();
    } catch (error) {
      setMessage({ tone: "error", text: error instanceof Error ? error.message : "Could not update homepage." });
    } finally {
      setIsBusy(false);
    }
  }

  const summaryCards = [
    { label: "Draft", value: summary.draftTopics, helper: "Editable topics", icon: FilePlus2 },
    { label: "Review", value: summary.reviewTopics, helper: "Waiting for quality checks", icon: CheckCircle2 },
    { label: "Approved", value: summary.approvedTopics, helper: "Ready for publisher action", icon: UploadCloud },
    { label: "Published", value: summary.publishedTopics, helper: "Visible on the public frontend", icon: Send },
  ];

  return (
    <>
      <section className="page-hero content-hero">
        <div>
          <h1>Content Management</h1>
          <p>
            Manage public handbook sections and topics from draft creation through review, approval,
            scheduled publishing, public release, unpublishing, and archive.
          </p>
        </div>

        <div className="users-hero-actions">
          <Link className="pill-button pill-button-secondary" href="/articles">
            Legacy Articles
          </Link>
          {canPublish ? (
            <button className="pill-button" disabled={isBusy} onClick={handleProcessScheduled} type="button">
              Process Scheduled
            </button>
          ) : null}
        </div>
      </section>

      {message ? (
        <section className={`content-message content-message-${message.tone}`}>{message.text}</section>
      ) : null}

      <section className="content-summary-grid">
        {summaryCards.map((card) => {
          const Icon = card.icon;
          return (
            <article className="content-summary-card" key={card.label}>
              <div className="content-summary-icon" aria-hidden="true">
                <Icon size={22} strokeWidth={1.9} />
              </div>
              <span className="users-summary-label">{card.label}</span>
              <strong>{formatCount(card.value)}</strong>
              <p>{card.helper}</p>
            </article>
          );
        })}
      </section>

      <section className="content-control-panel">
        <div className="content-tabs" role="tablist" aria-label="Content workspace tabs">
          {(["topics", "sections", "homepage", "queues", "roles"] as ContentTab[]).map((tab) => (
            <button
              className={`content-tab ${activeTab === tab ? "content-tab-active" : ""}`}
              key={tab}
              onClick={() => setActiveTab(tab)}
              type="button"
            >
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
        </div>

        <div className="content-locale-strip" aria-label="Locale selector">
          {localeOptions.map((item) => (
            <Link
              className={`users-filter-pill ${locale === item ? "users-filter-pill-active" : ""}`}
              href={pageUrl({ locale: item, search: params.search, status: currentStatus, tab: activeTab })}
              key={item}
            >
              {item}
            </Link>
          ))}
        </div>
      </section>

      {canCreate && activeTab === "topics" ? (
        <section className="content-form-grid">
          <form className="content-form-card" onSubmit={handleCreateSection}>
            <div className="content-form-title">
              <FolderPlus size={20} aria-hidden="true" />
              <h2>Create Section</h2>
            </div>
            <label>
              <span>Name</span>
              <input
                required
                value={sectionForm.name}
                onChange={(event) =>
                  setSectionForm((current) => ({
                    ...current,
                    name: event.target.value,
                    slug: current.slug || slugify(event.target.value),
                  }))
                }
              />
            </label>
            <label>
              <span>Slug</span>
              <input
                required
                value={sectionForm.slug}
                onChange={(event) => setSectionForm((current) => ({ ...current, slug: slugify(event.target.value) }))}
              />
            </label>
            <div className="content-form-row">
              <label>
                <span>Type</span>
                <select
                  value={sectionForm.type}
                  onChange={(event) => setSectionForm((current) => ({ ...current, type: event.target.value }))}
                >
                  <option value="MAIN">MAIN</option>
                  <option value="GROUP">GROUP</option>
                  <option value="SUBGROUP">SUBGROUP</option>
                </select>
              </label>
              <label>
                <span>Sort</span>
                <input
                  min="1"
                  type="number"
                  value={sectionForm.sortOrder}
                  onChange={(event) => setSectionForm((current) => ({ ...current, sortOrder: event.target.value }))}
                />
              </label>
            </div>
            <label>
              <span>Summary</span>
              <textarea
                required
                rows={3}
                value={sectionForm.summary}
                onChange={(event) => setSectionForm((current) => ({ ...current, summary: event.target.value }))}
              />
            </label>
            <button className="pill-button" disabled={isBusy} type="submit">
              Create Section
            </button>
          </form>

          <form className="content-form-card" onSubmit={handleCreateTopic}>
            <div className="content-form-title">
              <FilePlus2 size={20} aria-hidden="true" />
              <h2>Create Topic</h2>
            </div>
            <div className="content-form-row">
              <label>
                <span>Section</span>
                <select
                  required
                  value={topicForm.sectionId}
                  onChange={(event) => setTopicForm((current) => ({ ...current, sectionId: event.target.value }))}
                >
                  {sections.map((section) => (
                    <option key={section.id} value={section.id}>
                      {section.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                <span>Topic Type</span>
                <select
                  value={topicForm.topicType}
                  onChange={(event) => setTopicForm((current) => ({ ...current, topicType: event.target.value }))}
                >
                  <option value="GUIDE">GUIDE</option>
                  <option value="TAX_TOPIC">TAX_TOPIC</option>
                  <option value="SERVICE_TOPIC">SERVICE_TOPIC</option>
                  <option value="STATIC_TOPIC">STATIC_TOPIC</option>
                  <option value="LANDING_TOPIC">LANDING_TOPIC</option>
                </select>
              </label>
            </div>
            <label>
              <span>Title</span>
              <input
                required
                value={topicForm.title}
                onChange={(event) =>
                  setTopicForm((current) => ({
                    ...current,
                    title: event.target.value,
                    slug: current.slug || slugify(event.target.value),
                  }))
                }
              />
            </label>
            <label>
              <span>Slug</span>
              <input
                required
                value={topicForm.slug}
                onChange={(event) => setTopicForm((current) => ({ ...current, slug: slugify(event.target.value) }))}
              />
            </label>
            <label>
              <span>Summary</span>
              <textarea
                required
                rows={2}
                value={topicForm.summary}
                onChange={(event) => setTopicForm((current) => ({ ...current, summary: event.target.value }))}
              />
            </label>
            <label>
              <span>Intro Text</span>
              <textarea
                required
                rows={2}
                value={topicForm.introText}
                onChange={(event) => setTopicForm((current) => ({ ...current, introText: event.target.value }))}
              />
            </label>
            <label>
              <span>First Block Body</span>
              <textarea
                rows={3}
                value={topicForm.firstBlockBody}
                onChange={(event) =>
                  setTopicForm((current) => ({
                    ...current,
                    firstBlockTitle: current.firstBlockTitle || "Overview",
                    firstBlockBody: event.target.value,
                  }))
                }
              />
            </label>
            <button className="pill-button" disabled={isBusy || sections.length === 0} type="submit">
              Create Topic
            </button>
          </form>
        </section>
      ) : null}

      {activeTab === "topics" ? (
        <section className="panel-card users-table-panel">
          <div className="users-filter-strip" role="tablist" aria-label="Topic status filter">
            {statusFilters.map((status) => (
              <Link
                className={`users-filter-pill ${currentStatus === status ? "users-filter-pill-active" : ""}`}
                href={pageUrl({ locale, search: params.search, status, tab: activeTab })}
                key={status}
              >
                {status === "ALL" ? "All" : status.charAt(0) + status.slice(1).toLowerCase()}
              </Link>
            ))}
          </div>

          <div className="content-table-wrapper">
            <table className="users-table content-table">
              <thead>
                <tr>
                  <th>Topic</th>
                  <th>Section</th>
                  <th>Status</th>
                  <th>Type</th>
                  <th>Schedule</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {visibleTopics.map((topic) => {
                  const actions = permittedActions(role, topic.status);
                  return (
                    <tr key={topic.id}>
                      <td>
                        <div className="users-table-copy">
                          <strong>{topic.title}</strong>
                          <span>{topic.slug}</span>
                        </div>
                      </td>
                      <td>{sectionById.get(topic.sectionId)?.name ?? `Section #${topic.sectionId}`}</td>
                      <td>
                        <span className={`status-badge ${statusClassName(topic.status)}`}>{topic.status}</span>
                      </td>
                      <td>{topic.topicType}</td>
                      <td>
                        {topic.scheduledPublishAt ? new Date(topic.scheduledPublishAt).toLocaleString() : "Not scheduled"}
                      </td>
                      <td>
                        <div className="content-action-stack">
                          {canCreate && topic.status === "DRAFT" ? (
                            <button
                              className="content-icon-action"
                              onClick={() => setBlockTopicId(topic.id)}
                              title="Add content block"
                              type="button"
                            >
                              <FilePlus2 size={16} aria-hidden="true" />
                              Add Block
                            </button>
                          ) : null}
                          {actions.includes("SCHEDULE_PUBLISH") ? (
                            <label className="content-schedule-field">
                              <Clock size={15} aria-hidden="true" />
                              <input
                                type="datetime-local"
                                value={scheduleByTopic[topic.id] ?? ""}
                                onChange={(event) =>
                                  setScheduleByTopic((current) => ({
                                    ...current,
                                    [topic.id]: event.target.value,
                                  }))
                                }
                              />
                            </label>
                          ) : null}
                          {actions.map((action) => (
                            <button
                              className={`content-icon-action content-action-${action.toLowerCase()}`}
                              disabled={isBusy || (action === "SCHEDULE_PUBLISH" && !scheduleByTopic[topic.id])}
                              key={action}
                              onClick={() => runAction(topic, action)}
                              type="button"
                            >
                              {action === "REQUEST_CHANGES" ? (
                                <XCircle size={16} aria-hidden="true" />
                              ) : action === "ARCHIVE" ? (
                                <Archive size={16} aria-hidden="true" />
                              ) : action === "UNPUBLISH" ? (
                                <RotateCcw size={16} aria-hidden="true" />
                              ) : (
                                <CheckCircle2 size={16} aria-hidden="true" />
                              )}
                              {actionLabel(action)}
                            </button>
                          ))}
                          {actions.length === 0 && !(canCreate && topic.status === "DRAFT") ? (
                            <span className="metric-muted">Read only</span>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {visibleTopics.length === 0 ? <div className="empty-state">No matching content topics found.</div> : null}
        </section>
      ) : null}

      {activeTab === "sections" ? (
        <section className="content-management-grid">
          {sections.map((section) => (
            <article className="content-area-card" key={section.id}>
              <div className="content-area-copy">
                <span>{section.type}</span>
                <h2>{section.name}</h2>
                <p>{section.summary}</p>
              </div>
              <div className="queue-meta">
                <span>{section.slug}</span>
                <span>Sort {section.sortOrder}</span>
                <span className={`status-badge ${statusClassName(section.status)}`}>{section.status}</span>
              </div>
            </article>
          ))}
        </section>
      ) : null}

      {activeTab === "homepage" ? (
        <section className="content-form-grid">
          <form className="content-form-card" onSubmit={handleUpdateHomepage}>
            <div className="content-form-title">
              <FolderPlus size={20} aria-hidden="true" />
              <h2>Homepage Content</h2>
            </div>
            <div className="queue-meta">
              <span>Status {homepage.status}</span>
              <span>
                Updated{" "}
                {homepage.updatedAt ? new Date(homepage.updatedAt).toLocaleString() : "Not published yet"}
              </span>
            </div>
            <label>
              <span>Kicker</span>
              <input
                required
                value={homepageForm.kicker}
                onChange={(event) => setHomepageForm((current) => ({ ...current, kicker: event.target.value }))}
              />
            </label>
            <label>
              <span>Title</span>
              <input
                required
                value={homepageForm.title}
                onChange={(event) => setHomepageForm((current) => ({ ...current, title: event.target.value }))}
              />
            </label>
            <label>
              <span>Subtitle</span>
              <textarea
                required
                rows={3}
                value={homepageForm.subtitle}
                onChange={(event) => setHomepageForm((current) => ({ ...current, subtitle: event.target.value }))}
              />
            </label>
            <div className="content-form-row">
              <label>
                <span>Search label</span>
                <input
                  required
                  value={homepageForm.searchLabel}
                  onChange={(event) => setHomepageForm((current) => ({ ...current, searchLabel: event.target.value }))}
                />
              </label>
              <label>
                <span>Help label</span>
                <input
                  required
                  value={homepageForm.helpLabel}
                  onChange={(event) => setHomepageForm((current) => ({ ...current, helpLabel: event.target.value }))}
                />
              </label>
            </div>

            {homepageForm.cards.map((card, index) => (
              <div className="content-form-card content-form-card-nested" key={`homepage-card-${index}`}>
                <div className="content-form-title">
                  <FilePlus2 size={18} aria-hidden="true" />
                  <h2>Homepage Card {index + 1}</h2>
                </div>
                <div className="content-form-row">
                  <label>
                    <span>Section</span>
                    <select
                      required
                      value={card.sectionId}
                      onChange={(event) =>
                        setHomepageForm((current) => ({
                          ...current,
                          cards: current.cards.map((item, itemIndex) =>
                            itemIndex === index ? { ...item, sectionId: event.target.value } : item,
                          ),
                        }))
                      }
                    >
                      <option value="">Select a section</option>
                      {sections.map((section) => (
                        <option key={section.id} value={section.id}>
                          {section.name}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    <span>Sort</span>
                    <input
                      min="1"
                      type="number"
                      value={card.sortOrder}
                      onChange={(event) =>
                        setHomepageForm((current) => ({
                          ...current,
                          cards: current.cards.map((item, itemIndex) =>
                            itemIndex === index ? { ...item, sortOrder: event.target.value } : item,
                          ),
                        }))
                      }
                    />
                  </label>
                </div>
                <label>
                  <span>Title</span>
                  <input
                    required
                    value={card.title}
                    onChange={(event) =>
                      setHomepageForm((current) => ({
                        ...current,
                        cards: current.cards.map((item, itemIndex) =>
                          itemIndex === index ? { ...item, title: event.target.value } : item,
                        ),
                      }))
                    }
                  />
                </label>
                <label>
                  <span>Description</span>
                  <textarea
                    required
                    rows={3}
                    value={card.description}
                    onChange={(event) =>
                      setHomepageForm((current) => ({
                        ...current,
                        cards: current.cards.map((item, itemIndex) =>
                          itemIndex === index ? { ...item, description: event.target.value } : item,
                        ),
                      }))
                    }
                  />
                </label>
              </div>
            ))}

            <button className="pill-button" disabled={isBusy} type="submit">
              Save Homepage
            </button>
          </form>
        </section>
      ) : null}

      {activeTab === "queues" ? (
        <div className="content-queue-grid">
          <QueuePanel
            items={reviewQueue}
            title="Review Queue"
            canAct={canReview}
            onAction={runAction}
            onView={setDetailTopic}
            busy={isBusy}
            role={role}
          />
          <QueuePanel
            items={publishQueue}
            title="Ready To Publish"
            canAct={canPublish}
            onAction={runAction}
            onView={setDetailTopic}
            busy={isBusy}
            role={role}
          />
        </div>
      ) : null}

      {activeTab === "roles" ? (
        <section className="content-role-grid">
          {roleDetails.map((item) => (
            <article className={`content-role-card ${role === item.name ? "content-role-card-active" : ""}`} key={item.name}>
              <span>{role === item.name ? "Current role" : "Workflow role"}</span>
              <h2>{item.name}</h2>
              <p>{item.scope}</p>
            </article>
          ))}
        </section>
      ) : null}

      {blockTopicId ? (
        <div className="dialog-overlay">
          <div className="dialog-container">
            <form className="dialog-content content-block-dialog" onSubmit={handleAddBlock}>
              <h2 className="dialog-title">Add Content Block</h2>
              <p className="dialog-message">Add the first rich-text block so this topic can move toward publishing.</p>
              <label>
                <span>Block Title</span>
                <input
                  required
                  value={blockForm.title}
                  onChange={(event) => setBlockForm((current) => ({ ...current, title: event.target.value }))}
                />
              </label>
              <label>
                <span>Body</span>
                <textarea
                  required
                  rows={5}
                  value={blockForm.body}
                  onChange={(event) => setBlockForm((current) => ({ ...current, body: event.target.value }))}
                />
              </label>
              <div className="dialog-actions">
                <button className="dialog-button dialog-button-secondary" onClick={() => setBlockTopicId(null)} type="button">
                  Cancel
                </button>
                <button className="dialog-button" disabled={isBusy} type="submit">
                  Add Block
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {detailTopic ? (
        <div className="dialog-overlay">
          <div className="dialog-container">
            <article className="dialog-content topic-detail-dialog">
              <div className="topic-detail-heading">
                <div>
                  <p className="topic-detail-kicker">Workflow read-through</p>
                  <h2 className="dialog-title">{detailTopic.title}</h2>
                </div>
                <span className={`status-badge ${statusClassName(detailTopic.status)}`}>{detailTopic.status}</span>
              </div>

              <div className="queue-meta">
                <span>{detailTopic.topicType}</span>
                <span>{detailTopic.slug}</span>
                <span>Section #{detailTopic.sectionId}</span>
                <span>
                  Updated{" "}
                  {detailTopic.lastUpdated ? new Date(detailTopic.lastUpdated).toLocaleString() : "not yet"}
                </span>
              </div>

              <section className="topic-detail-section">
                <h3>Summary</h3>
                {renderParagraphs(detailTopic.summary)}
              </section>

              <section className="topic-detail-section">
                <h3>Introduction</h3>
                {renderParagraphs(detailTopic.introText)}
              </section>

              <section className="topic-detail-section">
                <h3>Content Blocks</h3>
                {detailTopic.blocks.length > 0 ? (
                  <div className="topic-block-list">
                    {detailTopic.blocks.map((block) => (
                      <article className="topic-block-preview" key={block.id}>
                        <div className="queue-head">
                          <strong>{block.title}</strong>
                          <span>{block.blockType}</span>
                        </div>
                        <div className="queue-meta">
                          <span>{block.anchorKey}</span>
                          <span>Sort {block.sortOrder}</span>
                        </div>
                        <div className="topic-block-body">{renderParagraphs(block.body)}</div>
                      </article>
                    ))}
                  </div>
                ) : (
                  <p className="metric-muted">No blocks have been added yet.</p>
                )}
              </section>

              {detailTopic.relatedGuides.length > 0 ? (
                <section className="topic-detail-section">
                  <h3>Related Guides</h3>
                  <div className="topic-related-list">
                    {detailTopic.relatedGuides.map((guide) => (
                      <span key={guide.id}>{guide.title}</span>
                    ))}
                  </div>
                </section>
              ) : null}

              <div className="dialog-actions">
                <button className="dialog-button dialog-button-secondary" onClick={() => setDetailTopic(null)} type="button">
                  Close
                </button>
              </div>
            </article>
          </div>
        </div>
      ) : null}
    </>
  );
}

function QueuePanel({
  busy,
  items,
  onAction,
  onView,
  role,
  title,
}: {
  busy: boolean;
  canAct: boolean;
  items: TopicDetail[];
  onAction: (topic: TopicSummary, action: TopicWorkflowAction) => void;
  onView: (topic: TopicDetail) => void;
  role?: string | null;
  title: string;
}) {
  return (
    <section className="panel-card content-queue-panel">
      <div className="panel-header">
        <div>
          <h2 className="panel-title">{title}</h2>
          <p className="panel-subtitle">Items that need the next workflow decision.</p>
        </div>
      </div>
      <div className="queue-list">
        {items.map((item) => (
          <article className="queue-item" key={item.id}>
            <div className="queue-head">
              <strong>{item.title}</strong>
              <span className={`status-badge ${statusClassName(item.status)}`}>{item.status}</span>
            </div>
            <div className="queue-meta">
              <span>{item.topicType}</span>
              <span>Section #{item.sectionId}</span>
              <span>{item.slug}</span>
            </div>
            <p className="metric-muted">{item.summary}</p>
            <div className="content-action-stack content-action-row">
              <button className="content-icon-action" onClick={() => onView(item)} type="button">
                <FilePlus2 size={16} aria-hidden="true" />
                Read Content
              </button>
              {permittedActions(role, item.status).map((action) =>
                action === "SCHEDULE_PUBLISH" ? null : (
                  <button
                    className="content-icon-action"
                    disabled={busy}
                    key={action}
                    onClick={() => onAction(item, action)}
                    type="button"
                  >
                    <CheckCircle2 size={16} aria-hidden="true" />
                    {actionLabel(action)}
                  </button>
                ),
              )}
            </div>
          </article>
        ))}
      </div>
      {items.length === 0 ? <div className="empty-state">No items in this queue.</div> : null}
    </section>
  );
}
