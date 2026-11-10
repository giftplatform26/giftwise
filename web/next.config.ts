import type { NextConfig } from 'next';

// Server-side backend origin. Browsers always call same-origin /backend/* and Next proxies it.
// Uses the same variable as the production kit (/etc/giftwise/web.env). Rewrites are resolved
// at build time, so set it before `next build` when deploying outside the release kit.
const backend = process.env.GIFTWISE_BACKEND_ORIGIN || 'http://localhost:8080';
const target = new URL(backend);
if (!['http:', 'https:'].includes(target.protocol) || target.username || target.password || target.search || target.hash || target.pathname !== '/') {
  throw new Error('GIFTWISE_BACKEND_ORIGIN must be an HTTP(S) origin without a path or credentials.');
}

const nextConfig: NextConfig = {
  poweredByHeader: false,
  output: 'standalone',
  async rewrites() {
    return [{ source: '/backend/:path*', destination: `${target.origin}/:path*` }];
  },
};

export default nextConfig;
