const isServer = typeof window === "undefined";
const baseUrl = isServer
  ? (process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081")
  : "";

interface ApiClientOptions {
  body?: BodyInit;
  data?: Record<string, any>;
  headers?: HeadersInit;
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  token?: string;
}

export async function apiClient<T>(path: string, options: ApiClientOptions = {}): Promise<T> {
  const { body: rawBody, data, headers, method = "GET", token } = options;

  const body = rawBody ?? (data ? JSON.stringify(data) : undefined);

  const response = await fetch(`${baseUrl}${path}`, {
    method,
    body,
    cache: "no-store",
    headers: {
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
  });

  if (!response.ok) {
    let errorMessage = `API request failed for ${path} with status ${response.status}`;

    try {
      const errorPayload = (await response.json()) as { message?: string };
      if (errorPayload.message) {
        errorMessage = errorPayload.message;
      }
    } catch {
      // Keep the generic message when the backend response is not JSON.
    }

    throw new Error(errorMessage);
  }

  return (await response.json()) as T;
}
