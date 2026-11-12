import type { Metadata } from 'next';
import { notFound } from 'next/navigation';
import { occasionBySlug, occasionPages } from '@/lib/occasions';
import { OccasionLanding } from '@/components/OccasionLanding';

export const dynamicParams = false;
export function generateStaticParams() { return occasionPages.map(o => ({ slug: o.slug })); }

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const o = occasionBySlug((await params).slug);
  if (!o) return {};
  return { title: o.title, description: o.description, alternates: { canonical: `/occasions/${o.slug}/` },
    openGraph: { title: o.title, description: o.description, type: 'website', url: `/occasions/${o.slug}/`, images: [{ url: '/images/og-giftwise.jpg', width: 1200, height: 630, alt: o.heading }] } };
}

export default async function OccasionPage({ params }: { params: Promise<{ slug: string }> }) {
  const o = occasionBySlug((await params).slug);
  if (!o) notFound();
  const faq = { '@context': 'https://schema.org', '@type': 'FAQPage', mainEntity: o.faqs.map(([q, a]) => ({ '@type': 'Question', name: q, acceptedAnswer: { '@type': 'Answer', text: a } })) };
  return <><script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faq).replace(/</g, '\\u003c') }} /><OccasionLanding page={o} /></>;
}
