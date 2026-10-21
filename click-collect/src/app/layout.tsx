import type { Metadata, Viewport } from "next";
import { Fredoka, Nunito } from "next/font/google";
import { CartProvider } from "@/store/cart";
import { AuthSessionProvider } from "@/components/providers/AuthSessionProvider";
import { SiteHeader } from "@/components/SiteHeader";
import "./globals.css";

const fontDisplay = Fredoka({
  variable: "--font-display",
  subsets: ["latin"],
  weight: ["500", "600", "700"],
});

const fontBody = Nunito({
  variable: "--font-body",
  subsets: ["latin"],
  weight: ["400", "600", "700", "800"],
});

export const metadata: Metadata = {
  title: {
    default: "Kebaba — Click & Collect",
    template: "%s · Kebaba",
  },
  description:
    "Commandez en click & collect chez Kebaba. Compte client, paiement en ligne ou au retrait.",
  applicationName: "Kebaba",
  manifest: "/manifest.webmanifest",
  appleWebApp: {
    capable: true,
    title: "Kebaba",
    statusBarStyle: "default",
  },
  metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL ?? "https://kebaba.fr"),
};

export const viewport: Viewport = {
  themeColor: "#6b1220",
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="fr" className={`${fontDisplay.variable} ${fontBody.variable} h-full`}>
      <body className="min-h-full flex flex-col antialiased">
        <AuthSessionProvider>
          <CartProvider>
            <SiteHeader />
            <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-6">{children}</main>
            <footer className="border-t border-[var(--kebaba-bordeaux)]/15 py-6 text-center text-sm text-[var(--kebaba-ink-muted)]">
              Kebaba · Click & Collect ·{" "}
              <a href="https://kebaba.fr" className="font-semibold text-[var(--kebaba-bordeaux)]">
                kebaba.fr
              </a>
            </footer>
          </CartProvider>
        </AuthSessionProvider>
      </body>
    </html>
  );
}
