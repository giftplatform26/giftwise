import type { NextConfig } from 'next';
import { PHASE_PRODUCTION_BUILD } from 'next/constants';

// The export bakes these origins into canonical links, the sitemap, social cards, the QR and every app link.
// A production build must name them explicitly: https, or an explicit localhost origin (see `npm run build:local`).
function requireOrigin(name: string) {
  const value = process.env[name];
  let url: URL | null = null;
  try { url = value ? new URL(value) : null; } catch { url = null; }
  const local = url && url.protocol === 'http:' && ['localhost', '127.0.0.1'].includes(url.hostname);
  if (!url || !(url.protocol === 'https:' || local) || url.pathname !== '/' || url.search || url.hash || url.username || url.password) {
    throw new Error(`${name} must be set to the public https origin before \`next build\` (got ${value ? JSON.stringify(value) : 'nothing'}). For a local preview build run \`npm run build:local\`.`);
  }
}

export default function config(phase: string): NextConfig {
  if (phase === PHASE_PRODUCTION_BUILD) { requireOrigin('NEXT_PUBLIC_SITE_URL'); requireOrigin('NEXT_PUBLIC_APP_URL'); }
  return {
    output: 'export',
    images: { unoptimized: true },
    poweredByHeader: false,
    trailingSlash: true,
  };
}
