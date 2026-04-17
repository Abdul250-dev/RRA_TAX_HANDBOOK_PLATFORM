import { apiClient } from "../../../lib/api/axios";
import { InviteForm } from "../../../components/auth/InviteForm";

interface InvitePreviewResponse {
  valid: boolean;
  expired: boolean;
  fullName: string;
  username: string | null;
  email: string;
  message: string;
}

interface AcceptInvitePageProps {
  searchParams: Promise<{ token?: string }>;
}

export default async function AcceptInvitePage({ searchParams }: AcceptInvitePageProps) {
  const { token } = await searchParams;

  if (!token) {
    return (
      <div className="login-page">
        <div className="login-error" style={{ maxWidth: 420 }}>
          Invalid or missing invitation link.
        </div>
      </div>
    );
  }

  let preview: InvitePreviewResponse | null = null;
  let fetchError: string | null = null;

  try {
    preview = await apiClient<InvitePreviewResponse>(`/api/auth/invite-preview?token=${encodeURIComponent(token)}`);
    if (!preview.valid) {
      fetchError = preview.expired
        ? "This invitation link has expired. Please contact your administrator."
        : preview.message;
    }
  } catch (err) {
    fetchError = err instanceof Error ? err.message : "This invitation link is invalid or has expired.";
  }

  return (
    <div className="login-page">
      {fetchError ? (
        <div className="login-error" style={{ maxWidth: 420 }}>{fetchError}</div>
      ) : (
        <InviteForm
          token={token}
          inviteeName={preview!.fullName}
          username={preview!.username}
        />
      )}
    </div>
  );
}
