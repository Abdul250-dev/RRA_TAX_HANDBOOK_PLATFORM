import { SectionLandingPage } from "../../../components/handbook/SectionLandingPage";
import { resolveHomepageBranchSlug } from "../../../lib/content/homepageBranches";

export default async function GeneralInformationPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  const slug = await resolveHomepageBranchSlug(locale, "general-information");

  return <SectionLandingPage locale={locale} slug={slug} />;
}
