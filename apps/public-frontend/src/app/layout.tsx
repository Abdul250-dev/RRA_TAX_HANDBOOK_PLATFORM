import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'RRA Tax Handbook',
  description: 'Rwanda Revenue Authority Tax Handbook - Edition 2025',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
