import { bootstrapCsrf } from './host-api.mjs';
import { csrfToken } from './gift-api.mjs';

const base = '/backend/api/v1/events';
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export const INVITATION_STYLES = ['PHOTO_FRAME', 'FLORAL', 'CELEBRATION', 'MINIMAL'];
export const MAX_IMAGE_BYTES = 5 * 1024 * 1024; // UI safety limit; backend remains authoritative
function path(eventId) {
  if (!uuid.test(eventId)) throw Error('Invalid event identifier');
  return `${base}/${encodeURIComponent(eventId)}/qr/invitations`;
}
function designPath(eventId, designId) {
  if (!uuid.test(designId)) throw Error('Invalid invitation identifier');
  return `${path(eventId)}/${encodeURIComponent(designId)}/png`;
}
function errorFor(status) {
  if (status === 401) return 'Sign in to manage invitations';
  if (status === 403) return 'Access denied or CSRF verification failed';
  if (status === 404) return 'Invitation or event not found';
  if (status === 409) return 'This event can no longer be edited';
  if (status === 413) return 'Image exceeds server upload limit';
  if (status === 400) return 'Invalid invitation details or image';
  return `Invitation request failed (HTTP ${status})`;
}
export function validateInvitation({ name, style, greeting = '', image = null }) {
  const cleanName = (name || '').trim();
  if (!cleanName || cleanName.length > 100) throw Error('Design name must be 1–100 characters');
  if (!INVITATION_STYLES.includes(style)) throw Error('Choose a supported invitation style');
  if (typeof greeting !== 'string' || greeting.length > 180) throw Error('Greeting must be at most 180 characters');
  if (image) {
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(image.type)) throw Error('Choose a PNG, JPEG or WebP image');
    if (!Number.isFinite(image.size) || image.size <= 0 || image.size > MAX_IMAGE_BYTES) throw Error('Image must be between 1 byte and 5 MB');
  }
  return { name: cleanName, style, greeting, image };
}
export async function listInvitations(fetcher, eventId) {
  const r = await fetcher(path(eventId), { credentials: 'include', cache: 'no-store', headers: { Accept: 'application/json' } });
  if (!r.ok) throw Error(errorFor(r.status));
  return r.json();
}
export async function createInvitation(fetcher, eventId, input, { cookie } = {}) {
  const v = validateInvitation(input);
  await bootstrapCsrf(fetcher);
  const token = csrfToken(cookie ?? (typeof document !== 'undefined' ? document.cookie : ''));
  if (!token) throw Error('CSRF cookie missing. Reload and try again.');
  const body = new FormData();
  body.append('name', v.name);
  body.append('style', v.style);
  body.append('greeting', v.greeting);
  if (v.image) body.append('image', v.image);
  const r = await fetcher(path(eventId), {
    method: 'POST', credentials: 'include', cache: 'no-store',
    headers: { Accept: 'application/json', 'X-XSRF-TOKEN': token }, body
  });
  if (!r.ok) throw Error(errorFor(r.status));
  return r.json();
}
export async function fetchInvitationPng(fetcher, eventId, designId) {
  const r = await fetcher(designPath(eventId, designId), {
    credentials: 'include', cache: 'no-store', headers: { Accept: 'image/png' }
  });
  if (!r.ok) throw Error(errorFor(r.status));
  const blob = await r.blob();
  if (blob.type && blob.type !== 'image/png') throw Error('Unexpected invitation image format');
  return blob;
}
