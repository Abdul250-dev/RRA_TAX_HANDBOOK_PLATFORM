import { notFound } from "next/navigation";

import { TopicDetailPage } from "../../../../components/handbook/TopicDetailPage";
import { ApiRequestError, getPublicGuideBySlug } from "../../../../lib/api/handbook";

export default async function GuideDetailPage({
  params,
}: {
  params: Promise<{ locale: string; slug: string }>;
}) {
  const { locale, slug } = await params;

  try {
    const guide = await getPublicGuideBySlug(locale, slug);

    return <TopicDetailPage locale={locale} topic={guide} />;
  } catch (error) {
    if (!(error instanceof ApiRequestError) || error.status === 404) {
      notFound();
    }

    notFound();
  }
}
