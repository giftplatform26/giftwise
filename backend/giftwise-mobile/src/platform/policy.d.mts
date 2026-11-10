export function isAdminPath(pathname: string): boolean;
export function validateOrigin(value: string | undefined, development?: boolean): string;
export function navigationTarget(value: string, origin: string, checkoutOrigins?: string[]): 'internal' | 'checkout' | 'external' | 'blocked';
export function deepLinkPath(value: string, origin: string): string | null;
export function parseNativeMessage(raw: string, messageURL: string, origin: string): { type: 'share'; text: string; url: string } | { type: 'clipboard'; text: string } | { type: 'export'; name: string; mime: string; base64: string };
