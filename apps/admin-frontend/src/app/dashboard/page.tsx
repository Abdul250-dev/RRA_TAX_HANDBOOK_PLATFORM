import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { DashboardStats } from "../../components/admin/DashboardStats";
import { DashboardHeroActions } from "../../components/admin/DashboardHeroActions";
import { DataTable } from "../../components/admin/DataTable";
import { FilterBar } from "../../components/admin/FilterBar";
import { AdminLayout } from "../../components/layout/AdminLayout";
import {
  getContentSummary,
  getPublishQueue,
  getReviewQueue,
  getUserSummary,
  type ContentSummary,
  type TopicSummary,
  type UserSummary,
} from "../../lib/api/dashboard";
import { AUTH_TOKEN_COOKIE } from "../../lib/api/auth";

const fallbackUserSummary: UserSummary = {
  totalUsers: 84,
  activeUsers: 52,
  pendingUsers: 11,
  suspendedUsers: 6,
  deactivatedUsers: 15,
};

const fallbackContentSummary: ContentSummary = {
  totalTopics: 132,
  draftTopics: 29,
  reviewTopics: 12,
  approvedTopics: 19,
  publishedTopics: 58,
  archivedTopics: 14,
  totalSections: 16,
  draftSections: 5,
  publishedSections: 9,
  archivedSections: 2,
};

const fallbackReviewQueue: TopicSummary[] = [
  {
    id: 1,
    sectionId: 3,
    title: "Value Added Tax Returns",
    slug: "value-added-tax-returns",
    summary: "Awaiting reviewer checks on declarations, filing examples, and deadlines.",
    topicType: "GUIDE",
    status: "REVIEW",
    sortOrder: 1,
  },
  {
    id: 2,
    sectionId: 4,
    title: "Income Tax Penalties",
    slug: "income-tax-penalties",
    summary: "Draft updated with new compliance notes and revised fine explanations.",
    topicType: "ARTICLE",
    status: "REVIEW",
    sortOrder: 2,
  },
];

const fallbackPublishQueue: TopicSummary[] = [
  {
    id: 3,
    sectionId: 5,
    title: "PAYE Monthly Filing",
    slug: "paye-monthly-filing",
    summary: "Approved and ready for publishing in all three supported languages.",
    topicType: "GUIDE",
    status: "APPROVED",
    sortOrder: 1,
  },
  {
    id: 4,
    sectionId: 5,
    title: "Capital Gains Tax Overview",
    slug: "capital-gains-tax-overview",
    summary: "Approved content package with cross-links to domestic taxes landing pages.",
    topicType: "ARTICLE",
    status: "APPROVED",
    sortOrder: 2,
  },
];

function formatCount(value: number) {
  return new Intl.NumberFormat("en-US").format(value);
}

function TopicStatusBadge({ status }: { status: string }) {
  const tone =
    status === "APPROVED" || status === "PUBLISHED"
      ? "status-approved"
      : status === "REVIEW"
        ? "status-review"
        : "status-draft";

  return <span className={`status-badge ${tone}`}>{status}</span>;
}

function QueuePanel({
  items,
  subtitle,
  title,
}: {
  items: TopicSummary[];
  subtitle: string;
  title: string;
}) {
  return (
    <section className="panel-card">
      <div className="panel-header">
        <div>
          <h2 className="panel-title">{title}</h2>
          <p className="panel-subtitle">{subtitle}</p>
        </div>
      </div>

      <div className="queue-list">
        {items.map((item) => (
          <article key={item.id} className="queue-item">
            <div style={{ display: "flex", justifyContent: "space-between", gap: "12px" }}>
              <strong>{item.title}</strong>
              <TopicStatusBadge status={item.status} />
            </div>
            <div className="queue-meta">
              <span>{item.topicType}</span>
              <span>Section #{item.sectionId}</span>
              <span>{item.slug}</span>
            </div>
            <div className="metric-muted">{item.summary}</div>
          </article>
        ))}
      </div>
    </section>
  );
}

function ChartPanel({ contentSummary }: { contentSummary: ContentSummary }) {
  const contentMix = [
    contentSummary.draftTopics,
    contentSummary.reviewTopics,
    contentSummary.approvedTopics,
    contentSummary.publishedTopics,
    contentSummary.archivedTopics,
  ];

  const total = Math.max(contentMix.reduce((sum, value) => sum + value, 0), 1);
  const lineOne = [14, 26, 18, 32, 28, 40, 34, 48, 42, 54, 46, 62];
  const lineTwo = [18, 22, 28, 24, 34, 31, 38, 36, 44, 41, 52, 45];
  const pointString = (points: number[]) =>
    points.map((point, index) => `${(index / (points.length - 1)) * 100},${100 - point}`).join(" ");

  return (
    <section className="panel-card">
      <div className="panel-header">
        <div>
          <h2 className="panel-title">Publishing Flow</h2>
          <p className="panel-subtitle">
            A lightweight view of how content is moving from draft to public release.
          </p>
        </div>
        <FilterBar
          options={[
            { label: "Weekly", active: true },
            { label: "Monthly" },
            { label: "Quarterly" },
          ]}
        />
      </div>

      <div className="legend">
        <span>
          <span className="legend-dot" style={{ background: "var(--primary)" }} />
          Actual flow
        </span>
        <span>
          <span className="legend-dot" style={{ background: "var(--success)" }} />
          Capacity
        </span>
      </div>

      <div className="chart-shell">
        <div className="chart-grid">
          {["100", "75", "50", "25", "0"].map((label) => (
            <div key={label} className="chart-row">
              <span>{label}</span>
            </div>
          ))}
        </div>
        <div className="chart-line" aria-hidden="true">
          <svg viewBox="0 0 100 100" preserveAspectRatio="none">
            <defs>
              <linearGradient id="chart-fill" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stopColor="rgba(81,150,207,0.24)" />
                <stop offset="100%" stopColor="rgba(81,150,207,0.03)" />
              </linearGradient>
            </defs>
            <polygon fill="url(#chart-fill)" points={`0,100 ${pointString(lineOne)} 100,100`} />
            <polyline
              fill="none"
              points={pointString(lineOne)}
              stroke="var(--primary)"
              strokeWidth="2"
              vectorEffect="non-scaling-stroke"
            />
            <polyline
              fill="none"
              points={pointString(lineTwo)}
              stroke="var(--success)"
              strokeDasharray="4 4"
              strokeWidth="2"
              vectorEffect="non-scaling-stroke"
            />
          </svg>
        </div>
      </div>

      <div style={{ marginTop: "18px" }}>
        <DataTable
          columns={[
            {
              key: "status",
              header: "Stage",
              render: (row: { label: string }) => row.label,
            },
            {
              key: "count",
              header: "Topics",
              render: (row: { value: number }) => formatCount(row.value),
            },
            {
              key: "share",
              header: "Share",
              render: (row: { value: number }) => (
                <span className="metric-positive">{Math.round((row.value / total) * 100)}%</span>
              ),
            },
          ]}
          rows={[
            { label: "Draft", value: contentSummary.draftTopics },
            { label: "In Review", value: contentSummary.reviewTopics },
            { label: "Approved", value: contentSummary.approvedTopics },
            { label: "Published", value: contentSummary.publishedTopics },
            { label: "Archived", value: contentSummary.archivedTopics },
          ]}
        />
      </div>
    </section>
  );
}

async function getDashboardData() {
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_TOKEN_COOKIE)?.value as string;

  if (!token) {
    redirect("/login");
  }

  try {
    const [userSummary, contentSummary, reviewQueue, publishQueue] = await Promise.all([
      getUserSummary(token),
      getContentSummary(token),
      getReviewQueue(token),
      getPublishQueue(token),
    ]);

    return { contentSummary, publishQueue, reviewQueue, userSummary, token };
  } catch {
    return {
      contentSummary: fallbackContentSummary,
      publishQueue: fallbackPublishQueue,
      reviewQueue: fallbackReviewQueue,
      userSummary: fallbackUserSummary,
      token,
    };
  }
}

export default async function DashboardPage() {
  const { contentSummary, publishQueue, reviewQueue, userSummary, token } = await getDashboardData();

  const statItems = [
    {
      label: "Active Users",
      tone: "blue" as const,
      value: formatCount(userSummary.activeUsers),
      helper: `${formatCount(userSummary.pendingUsers)} pending and waiting to join`,
      icon: "US",
    },
    {
      label: "Published Topics",
      tone: "green" as const,
      value: formatCount(contentSummary.publishedTopics),
      helper: `${formatCount(contentSummary.totalSections)} live sections supporting public navigation`,
      icon: "PB",
    },
    {
      label: "Review Queue",
      tone: "orange" as const,
      value: formatCount(contentSummary.reviewTopics),
      helper: `${formatCount(contentSummary.approvedTopics)} pieces already approved for publication`,
      icon: "RV",
    },
    {
      label: "Suspended Accounts",
      tone: "blue" as const,
      value: formatCount(userSummary.suspendedUsers),
      helper: `${formatCount(userSummary.deactivatedUsers)} deactivated accounts kept for audit history`,
      icon: "AC",
    },
  ];

  const operationsRows = [
    {
      metric: "Users lifecycle",
      detail: `${formatCount(userSummary.totalUsers)} total managed accounts`,
      value: `${formatCount(userSummary.activeUsers)} active`,
      trend: `${formatCount(userSummary.pendingUsers)} pending`,
    },
    {
      metric: "Topics in production",
      detail: `${formatCount(contentSummary.totalTopics)} total content items`,
      value: `${formatCount(contentSummary.draftTopics)} draft`,
      trend: `${formatCount(contentSummary.reviewTopics)} under review`,
    },
    {
      metric: "Sections live",
      detail: `${formatCount(contentSummary.totalSections)} handbook sections created`,
      value: `${formatCount(contentSummary.publishedSections)} published`,
      trend: `${formatCount(contentSummary.archivedSections)} archived`,
    },
  ];

  return (
    <AdminLayout>
      <main className="dashboard-stack">
        <section className="page-hero">
          <div>
            <h1>Administrative Dashboard</h1>
            <p>
              A simple, reusable RRA control room for user access, multilingual content
              workflow, and publishing readiness across the handbook platform.
            </p>
          </div>

          <DashboardHeroActions token={token} />
        </section>

        <DashboardStats items={statItems} />
      
      </main>
    </AdminLayout>
  );
}
