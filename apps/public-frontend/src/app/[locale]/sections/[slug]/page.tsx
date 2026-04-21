import { SectionLandingPage } from "../../../../components/handbook/SectionLandingPage";

export default async function SectionPage({
  params,
}: {
  params: Promise<{ locale: string; slug: string }>;
}) {
  const { locale, slug } = await params;
  return <SectionLandingPage locale={locale} slug={slug} />;
}
