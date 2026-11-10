import { NextResponse, type NextRequest } from 'next/server';
import { isAdminPath } from './lib/mobile-scope.mjs';
export function middleware(request: NextRequest) {
  if (isAdminPath(request.nextUrl.pathname)) {
    return new NextResponse('This page is not available in the mobile application.', { status: 404, headers: { 'Cache-Control': 'no-store', 'Content-Type': 'text/plain; charset=utf-8' } });
  }
  return NextResponse.next();
}
export const config = { matcher: ['/((?!_next/static|_next/image|favicon.ico).*)'] };
