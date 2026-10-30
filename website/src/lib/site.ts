function publicUrl(value: string | undefined, fallback: string) {
  try { const url = new URL(value || fallback); return ['http:', 'https:'].includes(url.protocol) ? url.origin : fallback; }
  catch { return fallback; }
}
export const site = {
  name: 'GiftWise',
  url: publicUrl(process.env.NEXT_PUBLIC_SITE_URL, 'http://localhost:3001'),
  appUrl: publicUrl(process.env.NEXT_PUBLIC_APP_URL, 'http://localhost:3000'),
  contactEmail: process.env.NEXT_PUBLIC_CONTACT_EMAIL || '',
  socials: {
    instagram: process.env.NEXT_PUBLIC_INSTAGRAM_URL || '',
    linkedin: process.env.NEXT_PUBLIC_LINKEDIN_URL || '',
    x: process.env.NEXT_PUBLIC_X_URL || '',
  },
};
export const createEventUrl = `${site.appUrl}/events/new`;
export const loginUrl = `${site.appUrl}/login`;
