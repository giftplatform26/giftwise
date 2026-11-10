import {redirectIfSessionExpired} from './host-session.mjs';
/** Backend contract adapter. Does not equate local visibility with backend GiftStatus. */
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export function eventGiftPath(eventId, giftId) {
  if (!UUID.test(eventId)) throw Error('Valid event UUID required');
  if (giftId != null && !UUID.test(giftId)) throw Error('Valid gift UUID required');
  return `/backend/api/v1/events/${eventId}/gifts${giftId ? `/${giftId}` : ''}`;
}
export function toGiftRequest(gift) {
  const name = String(gift?.name ?? '').trim();
  const amount = Number(gift?.targetAmount);
  if (!name || name.length > 200) throw Error('Gift name must be 1–200 characters');
  if (!Number.isFinite(amount) || amount < 0.01 || amount > 9999999999999.99 || Math.abs(Math.round(amount * 100) - amount * 100) > 0.00001) throw Error('Invalid gift amount');
  const description = String(gift.description ?? '');
  if (description.length > 10000) throw Error('Description too long');
  const imageKey = gift.imageKey == null || gift.imageKey === '' ? null : String(gift.imageKey);
  if (imageKey && imageKey.length > 512) throw Error('Invalid image key');
  const fulfillmentOverride = gift.fulfillmentOverride || null;
  if (fulfillmentOverride !== null && !['DELIVER_WHEN_FUNDED', 'AFTER_EVENT_CONFIRMATION'].includes(fulfillmentOverride)) throw Error('Invalid fulfillment preference');
  const priority = gift.priority ?? 'MEDIUM';
  if (!['HIGH','MEDIUM','LOW'].includes(priority)) throw Error('Invalid gift priority');
  return {name, description, imageKey, targetAmount: amount.toFixed(2), fulfillmentOverride, priority,
    ...(gift.productUrl!==undefined?{productUrl:gift.productUrl}:{}),
    ...(gift.retailerLookupId?{retailerLookupId:gift.retailerLookupId}:{})};
}
export function csrfToken(cookie = '') {
  const match = cookie.split(';').map(v => v.trim()).find(v => v.startsWith('XSRF-TOKEN='));
  return match ? decodeURIComponent(match.slice('XSRF-TOKEN='.length)) : '';
}
export async function giftApiRequest(fetcher, {eventId, giftId, method = 'GET', gift, cookie = ''}) {
  const path = eventGiftPath(eventId, giftId);
  const unsafe = !['GET', 'HEAD', 'OPTIONS'].includes(method);
  const token = csrfToken(cookie);
  const headers = {'Accept': 'application/json'};
  if (gift !== undefined) headers['Content-Type'] = 'application/json';
  if (unsafe && token) headers['X-XSRF-TOKEN'] = token;
  const response = await fetcher(path, {method, credentials: 'include', headers, ...(gift === undefined ? {} : {body: JSON.stringify(toGiftRequest(gift))})});
  redirectIfSessionExpired(response,path);
  if (!response.ok) {
    if (response.status === 401) throw Error('Login required');
    if (response.status === 403) throw Error('Access denied or CSRF token missing');
    if (response.status === 404) throw Error('Event or gift not found');
    if (response.status === 409) throw Error('Gift or event was changed; reload and try again');
    throw Error(`Gift API failed (HTTP ${response.status})`);
  }
  return response.status === 204 ? null : response.json();
}
export const giftApi = {
  list: (fetcher, eventId) => giftApiRequest(fetcher, {eventId}),
  create: (fetcher, eventId, gift, cookie) => giftApiRequest(fetcher, {eventId, method:'POST', gift, cookie}),
  update: (fetcher, eventId, giftId, gift, cookie) => giftApiRequest(fetcher, {eventId, giftId, method:'PUT', gift, cookie}),
  cancel: (fetcher, eventId, giftId, cookie) => cancelGift(fetcher, eventId, giftId, cookie)
};
export async function cancelGift(fetcher, eventId, giftId, cookie='') {
  const token=csrfToken(cookie);
  const response=await fetcher(`${eventGiftPath(eventId,giftId)}/cancel`,{method:'POST',credentials:'include',headers:token?{'X-XSRF-TOKEN':token}:{}});
  redirectIfSessionExpired(response,`${eventGiftPath(eventId,giftId)}/cancel`);
  if (!response.ok) throw Error(`Cancel failed (HTTP ${response.status})`);
  return response.json();
}
