import { getHandbookSections, getHomepageContent } from "../api/handbook";

export type HomepageBranch = "general-information" | "taxes" | "other-services" | "guides";

const branchMatchers: Record<HomepageBranch, string[]> = {
  "general-information": ["general information", "general-information"],
  taxes: ["taxes administered in rwanda", "taxes in rwanda", "taxes"],
  "other-services": ["other services", "other-services"],
  guides: ["user guides and examples", "guides", "user-guides-and-examples"],
};

function normalize(value: string) {
  return value.trim().toLowerCase();
}

export function getHomepageBranchHref(locale: string, title: string, slug: string) {
  const normalizedTitle = normalize(title);
  const normalizedSlug = normalize(slug);
  const branch = Object.entries(branchMatchers).find(([, matchers]) =>
    matchers.includes(normalizedTitle) || matchers.includes(normalizedSlug),
  )?.[0] as HomepageBranch | undefined;

  return branch ? `/${locale}/${branch}` : `/${locale}/sections/${slug}`;
}

export async function resolveHomepageBranchSlug(locale: string, branch: HomepageBranch) {
  const matchers = branchMatchers[branch];

  try {
    const homepage = await getHomepageContent(locale);
    const card = homepage.cards.find((item) =>
      matchers.includes(normalize(item.title)) || matchers.includes(normalize(item.slug)),
    );
    if (card) return card.slug;
  } catch {
    // Fall through to sections lookup while the homepage endpoint is unavailable.
  }

  const sections = await getHandbookSections(locale);
  const section = sections.find((item) =>
    matchers.includes(normalize(item.name)) || matchers.includes(normalize(item.slug)),
  );

  return section?.slug ?? branch;
}
