export const routes = {
  home: "/",
  guide: (locale: string, slug: string) => `/${locale}/guides/${slug}`,
  section: (locale: string, slug: string) => `/${locale}/sections/${slug}`,
  topic: (locale: string, slug: string) => `/${locale}/topics/${slug}`,
};
