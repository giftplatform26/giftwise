import type { Metadata, Viewport } from 'next';
import { site } from '@/lib/site';
import './globals.css';
export const metadata: Metadata = {
  metadataBase: new URL(site.url),
  title: 'GiftWise — Gift What Matters. Not What Gets Forgotten.',
  description: 'Stop receiving random gifts. Create a personal wishlist, share a QR invitation, and let friends contribute towards gifts you will truly love and use.',
  applicationName: 'GiftWise',
  keywords: ['gift wishlist', 'group gifting', 'wedding wishlist', 'housewarming gifts', 'birthday wishlist', 'QR invitation', 'meaningful gifts'],
  alternates: { canonical: '/' },
  openGraph: { title: 'GiftWise — A little less random. A lot more meaningful.', description: 'Your wishlist. Your moment. Gifts that become part of your life.', type: 'website', locale: 'en_IN', url: '/', siteName: 'GiftWise', images: [{ url: '/images/og-giftwise.jpg', width: 1200, height: 630, alt: 'From unwanted gifts to meaningful moments with GiftWise' }] },
  twitter: { card: 'summary_large_image', title: 'GiftWise — Gift What Matters.', description: 'Create your wishlist. Bring your people together. Receive gifts you will truly love.', images: ['/images/og-giftwise.jpg'] },
  robots: { index: true, follow: true },
  icons: { icon: '/icon.svg', apple: '/icon.svg' },
};
export const viewport: Viewport = { width: 'device-width', initialScale: 1, themeColor: '#F8F9FC' };
export default function RootLayout({ children }: { children: React.ReactNode }) {
  const schema = { '@context': 'https://schema.org', '@type': 'WebSite', name: 'GiftWise', url: site.url, description: 'A thoughtful way to create event wishlists and bring people together around meaningful gifts.' };
  return <html lang="en"><head><link rel="preload" href="/fonts/manrope-variable.ttf" as="font" type="font/ttf" crossOrigin="anonymous" /></head><body><script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(schema).replace(/</g, '\\u003c') }} />{children}</body></html>;
}
