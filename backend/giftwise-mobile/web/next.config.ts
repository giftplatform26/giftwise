import type { NextConfig } from 'next';
import path from 'node:path';
// Server-only backend origin. The browser always uses same-origin /backend requests.
const backend = process.env.GIFTWISE_BACKEND_URL || 'http://localhost:8080';
const target = new URL(backend);
if (!['http:', 'https:'].includes(target.protocol) || target.username || target.password || target.search || target.hash || target.pathname !== '/') {
  throw new Error('GIFTWISE_BACKEND_URL must be an HTTP(S) origin without a path or credentials.');
}
const config: NextConfig = {
  poweredByHeader: false,
  outputFileTracingRoot: path.resolve(process.cwd()),
  async rewrites() { return [{ source: '/backend/:path*', destination: `${target.origin}/:path*` }]; },
};
export default config;
