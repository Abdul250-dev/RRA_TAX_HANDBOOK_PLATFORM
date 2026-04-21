import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { ContentPageClient } from "../../components/admin/ContentPageClient";
import { AdminLayout } from "../../components/layout/AdminLayout";
import { AUTH_ROLE_COOKIE, AUTH_TOKEN_COOKIE } from "../../lib/api/auth";
import {
  getAdminHomepage,
  getAdminContentSummary,
  getAdminPublishQueueDetails,
  getAdminReviewQueueDetails,
  getAdminSections,
  getAdminTopics,
  type AdminSection,
  type ContentSummary,
  type AdminHomepageContent,
  type LocaleCode,
  type TopicDetail,
  type TopicSummary,
} from "../../lib/api/content";
import { canViewContent } from "../../lib/authz";

const fallbackContentSummary: ContentSummary = {
  totalTopics: 0,
  draftTopics: 0,
  reviewTopics: 0,
  approvedTopics: 0,
  publishedTopics: 0,
  archivedTopics: 0,
  totalSections: 0,
  draftSections: 0,
  publishedSections: 0,
  archivedSections: 0,
};

async function safeRead<T>(request: Promise<T>, fallback: T) {
  try {
    return await request;
  } catch {
    return fallback;
  }
}

async function getContentData(token: string, role: string | null, locale: LocaleCode, status?: string) {
  const [homepage, summary, sections, topics] = await Promise.all([
    safeRead(getAdminHomepage(token, locale), {
      kicker: "",
      title: "",
      subtitle: "",
      searchLabel: "",
      helpLabel: "",
      status: "DRAFT",
      updatedAt: null,
      cards: [],
    } as AdminHomepageContent),
    safeRead(getAdminContentSummary(token), fallbackContentSummary),
    safeRead(getAdminSections(token, locale), [] as AdminSection[]),
    safeRead(getAdminTopics(token, locale, status), [] as TopicSummary[]),
  ]);

  const reviewQueue =
    role === "ADMIN" || role === "REVIEWER" || role === "AUDITOR" || role === "VIEWER"
      ? await safeRead(getAdminReviewQueueDetails(token, locale), [] as TopicDetail[])
      : ([] as TopicDetail[]);

  const publishQueue =
    role === "ADMIN" || role === "PUBLISHER" || role === "AUDITOR" || role === "VIEWER"
      ? await safeRead(getAdminPublishQueueDetails(token, locale), [] as TopicDetail[])
      : ([] as TopicDetail[]);

  return { homepage, publishQueue, reviewQueue, sections, summary, topics };
}

interface ContentPageProps {
  searchParams: Promise<{
    locale?: string;
    search?: string;
    status?: string;
    tab?: string;
  }>;
}

export default async function ContentPage({ searchParams }: ContentPageProps) {
  const params = await searchParams;
  const locale = ["EN", "FR", "KIN"].includes(params.locale ?? "") ? (params.locale as LocaleCode) : "EN";
  const status = params.status && params.status !== "ALL" ? params.status : undefined;
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_TOKEN_COOKIE)?.value;
  const role = cookieStore.get(AUTH_ROLE_COOKIE)?.value ?? null;

  if (!token) {
    redirect("/login");
  }

  if (!canViewContent(role)) {
    redirect("/dashboard");
  }

  const data = await getContentData(token, role, locale, status);

  return (
    <AdminLayout>
      <main className="dashboard-stack">
        <ContentPageClient
          locale={locale}
          params={params}
          role={role}
          token={token}
          {...data}
        />
      </main>
    </AdminLayout>
  );
}
