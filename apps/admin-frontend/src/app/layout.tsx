import "./globals.css";

import { siteConfig } from "../lib/constants/site";

export const metadata = {
  title: `${siteConfig.shortName} | ${siteConfig.name}`,
  description: siteConfig.description,
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body suppressHydrationWarning>{children}</body>
    </html>
  );
}
