import type { PublicSearchResponse } from "../../types/handbook";

const API_BASE_URL =
  process.env.API_BASE_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8081";

function toBackendLocale(locale: string) {
  if (locale === "fr") return "FR";
  if (locale === "rw") return "KIN";
  return "EN";
}

export class SearchRequestError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

export async function searchContent(locale: string, query: string) {
  const trimmedQuery = query.trim();

  if (trimmedQuery.length < 2) {
    return null;
  }

  const params = new URLSearchParams({
    q: trimmedQuery,
    locale: toBackendLocale(locale),
  });

  const response = await fetch(`${API_BASE_URL}/api/public/search?${params.toString()}`, {
    next: { revalidate: 30 },
  });

  if (!response.ok) {
    throw new SearchRequestError(`Search failed with status ${response.status}`, response.status);
  }

  return response.json() as Promise<PublicSearchResponse>;
}
