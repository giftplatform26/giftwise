import type { MetadataRoute } from 'next';
import { site } from '@/lib/site';
import { occasionPages } from '@/lib/occasions';
export const dynamic = 'force-static';
export default function sitemap(): MetadataRoute.Sitemap {
  return [{ url: site.url, changeFrequency: 'monthly', priority: 1 },
    ...occasionPages.map(o => ({ url: `${site.url}/occasions/${o.slug}/`, changeFrequency: 'monthly' as const, priority: 0.8 }))];
}
