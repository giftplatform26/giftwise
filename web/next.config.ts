import type { NextConfig } from 'next';

// Server-side backend origin. Browsers always call same-origin /backend/* and Next proxies it.
// Uses the same variable as the production kit (/etc/giftwise/web.env). Rewrites are resolved
// at build time, so set it before `next build` when deploying outside the release kit.
const backend = process.env.GIFTWISE_BACKEND_ORIGIN || 'http://localhost:8080';
const target = new URL(backend);
if (!['http:', 'https:'].includes(target.protocol) || target.username || target.password || target.search || target.hash || target.pathname !== '/') {
  throw new Error('GIFTWISE_BACKEND_ORIGIN must be an HTTP(S) origin without a path or credentials.');
}

// Content-Security-Policy. Next.js App Router streams its payload in inline <script> tags, so script-src needs
// 'unsafe-inline' (a nonce would force every page to render dynamically); the policy still pins script, frame and
// connect origins to this site and Razorpay, and blocks plugins, <base> hijacking, foreign form posts and framing.
// Development adds 'unsafe-eval' and websockets for React Refresh / HMR.
const dev = process.env.NODE_ENV !== 'production';
const razorpay = 'https://*.razorpay.com';
const csp = [
  "default-src 'self'",
  `script-src 'self' 'unsafe-inline'${dev ? " 'unsafe-eval'" : ''} ${razorpay}`,
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob: https:",
  "font-src 'self' data:",
  "media-src 'self' data: blob:",
  `connect-src 'self' ${razorpay}${dev ? ' ws: wss:' : ''}`,
  `frame-src 'self' ${razorpay}`,
  "worker-src 'self' blob:",
  "object-src 'none'",
  "base-uri 'self'",
  `form-action 'self' ${razorpay}`,
  "frame-ancestors 'self'",
].join('; ');

const nextConfig: NextConfig = {
  poweredByHeader: false,
  output: 'standalone',
  // Baseline hardening for pages. /backend/* is excluded because the API sets its own headers
  // (Spring Security) and duplicated X-Frame-Options values can be treated as invalid.
  async headers() {
    return [{
      source: '/:path((?!backend/).*)',
      headers: [
        { key: 'Content-Security-Policy', value: csp },
        { key: 'X-Content-Type-Options', value: 'nosniff' },
        { key: 'X-Frame-Options', value: 'SAMEORIGIN' },
        { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
        { key: 'Permissions-Policy', value: 'camera=(self), microphone=(), geolocation=()' },
      ],
    }];
  },
  async rewrites() {
    return [{ source: '/backend/:path*', destination: `${target.origin}/:path*` }];
  },
};

export default nextConfig;
