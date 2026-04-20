export const routes = {
  home: "/",
  section: (locale: string, slug: string) => `/${locale}/sections/${slug}`,
  topic: (locale: string, slug: string) => `/${locale}/topics/${slug}`,
};
