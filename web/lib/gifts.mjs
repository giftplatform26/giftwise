export const GIFT_STATUSES = Object.freeze(['DRAFT', 'PUBLIC']);
export function normalizeProductUrl(value) {
  if (typeof value !== 'string' || value.length > 2048) throw Error('Invalid product link');
  let u; try { u = new URL(value.trim()); } catch { throw Error('Enter a valid HTTPS product link'); }
  if (u.protocol !== 'https:' || u.username || u.password) throw Error('Use an HTTPS product link without credentials');
  const host = u.hostname.toLowerCase().replace(/\.$/, '');
  if (!host.includes('.') || host === 'localhost' || host.endsWith('.localhost') || host.endsWith('.local') || host.endsWith('.internal') || host.endsWith('.test') || host.endsWith('.example') || host.endsWith('.invalid') || /^\d+\.\d+\.\d+\.\d+$/.test(host) || host.includes(':')) throw Error('Enter a public retailer website');
  u.hash = ''; return u.toString();
}
export function validateGift(input) {
  if (!input || typeof input !== 'object') throw Error('Gift is required');
  const name = String(input.name ?? '').trim();
  if (!name || name.length > 160) throw Error('Gift name must be 1–160 characters');
  const amount = Number(input.targetAmount);
  if (!Number.isFinite(amount) || Math.abs(amount * 100 - Math.round(amount * 100)) > 0.00001 || amount < 0.01 || amount > 10000000) throw Error('Enter a target from ₹0.01 to ₹1,00,00,000');
  const description = String(input.description ?? '').trim();
  if (description.length > 2000) throw Error('Description is too long');
  const productUrl = input.productUrl ? normalizeProductUrl(input.productUrl) : '';
  if (!GIFT_STATUSES.includes(input.status)) throw Error('Invalid visibility');
  const priority = input.priority ?? 'MEDIUM';
  if (!['HIGH','MEDIUM','LOW'].includes(priority)) throw Error('Invalid gift priority');
  return { name, description, targetAmount: amount, productUrl, status: input.status, priority, contributionsEnabled: input.status === 'PUBLIC' && input.contributionsEnabled === true };
}
export function createGift(input, id = crypto.randomUUID()) { return { ...validateGift(input), id, createdAt: new Date().toISOString() }; }
export function updateGift(gifts, id, input) { if (!gifts.some(g => g.id === id)) throw Error('Gift not found'); return gifts.map(g => g.id === id ? {...g, ...validateGift(input)} : g); }
export function publicGifts(gifts) { return gifts.filter(g => g.status === 'PUBLIC'); }
export function parseDraft(text) {
  if (text.length > 1000000) throw Error('Draft too large');
  const data = JSON.parse(text);
  if (!Array.isArray(data) || data.length > 100) throw Error('Invalid gift draft');
  const ids = new Set();
  return data.map(g => {
    if (typeof g.id !== 'string' || !/^[a-zA-Z0-9-]{1,80}$/.test(g.id) || ids.has(g.id)) throw Error('Invalid gift identifier');
    ids.add(g.id);
    return {...validateGift(g), id:g.id, createdAt: typeof g.createdAt === 'string' ? g.createdAt : ''};
  });
}
