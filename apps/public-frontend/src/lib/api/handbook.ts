import type {
  HomepageContent,
  HandbookSectionDetail,
  HandbookSectionSummary,
  HandbookTopicDetail,
} from "../../types/handbook";

const API_BASE_URL =
  process.env.API_BASE_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8081";

export class ApiRequestError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

function toBackendLocale(locale: string) {
  if (locale === "fr") return "FR";
  if (locale === "rw") return "KIN";
  return "EN";
}

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    next: { revalidate: 60 },
  });

  if (!response.ok) {
    throw new ApiRequestError(`Request failed with status ${response.status}`, response.status);
  }

  return response.json() as Promise<T>;
}

export async function getHandbookSections(locale: string) {
  return fetchJson<HandbookSectionSummary[]>(
    `/api/public/sections?locale=${toBackendLocale(locale)}`,
  );
}

export async function getHomepageContent(locale: string) {
  return fetchJson<HomepageContent>(
    `/api/public/homepage?locale=${toBackendLocale(locale)}`,
  );
}

export async function getHandbookSectionBySlug(locale: string, slug: string) {
  return fetchJson<HandbookSectionDetail>(
    `/api/public/sections/${encodeURIComponent(slug)}?locale=${toBackendLocale(locale)}`,
  );
}

export async function getHandbookTopicBySlug(locale: string, slug: string) {
  return fetchJson<HandbookTopicDetail>(
    `/api/public/topics/${encodeURIComponent(slug)}?locale=${toBackendLocale(locale)}`,
  );
}
