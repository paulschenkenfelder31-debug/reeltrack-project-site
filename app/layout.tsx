import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "ReelTrack — Android Film Diary",
  description:
    "ReelTrack is a private, non-commercial Android app for discovering films, building a watchlist, rating movies and keeping a personal viewing diary.",
  icons: {
    icon: "./favicon.svg",
    shortcut: "./favicon.svg",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">{children}</body>
    </html>
  );
}
