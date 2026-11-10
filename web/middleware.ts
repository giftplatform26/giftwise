import { NextResponse, type NextRequest } from 'next/server';
import { isAdminPath, isNativeAppUserAgent } from './lib/native-app.mjs';

// Administration is web-only: refuse admin pages and admin APIs to the GiftWise mobile app.
export function middleware(request: NextRequest) {
  if (isNativeAppUserAgent(request.headers.get('user-agent')) && isAdminPath(request.nextUrl.pathname)) {
    return new NextResponse('This page is not available in the GiftWise app.', {
      status: 404,
      headers: { 'Cache-Control': 'no-store', 'Content-Type': 'text/plain; charset=utf-8' },
    });
  }
  return NextResponse.next();
}

// Match broadly so encoded or traversal variants (e.g. /%61dmin) are normalised by isAdminPath.
export const config = { matcher: ['/((?!_next/static|_next/image|favicon.ico).*)'] };
