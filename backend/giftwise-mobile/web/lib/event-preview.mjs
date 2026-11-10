
import {activeLocale} from './localization.mjs';
export const OCCASION_STYLE = Object.freeze({
  WEDDING: { icon: '💍', eyebrow: 'A LOVE STORY', greeting: 'Together is a beautiful place to be', theme: 'wedding' },
  BIRTHDAY: { icon: '🎂', eyebrow: 'MAKE A WISH', greeting: 'A little more joy, a lot more magic', theme: 'birthday' },
  BABY_SHOWER: { icon: '🧸', eyebrow: 'A LITTLE MIRACLE', greeting: 'Tiny moments, endless love', theme: 'baby' },
  HOUSEWARMING: { icon: '🏡', eyebrow: 'HOME SWEET HOME', greeting: 'New beginnings, warm memories', theme: 'home' },
  ANNIVERSARY: { icon: '🥂', eyebrow: 'CELEBRATE LOVE', greeting: 'Every chapter deserves a celebration', theme: 'anniversary' },
  OTHER: { icon: '✨', eyebrow: 'YOU ARE INVITED', greeting: 'Come celebrate something wonderful', theme: 'other' }
});
export const VALID_UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export function validateEventId(id) { if (typeof id !== 'string' || !VALID_UUID.test(id)) throw Error('Invalid event link'); return id; }
export function occasionStyle(type) { return OCCASION_STYLE[type] || OCCASION_STYLE.OTHER; }
export function visibleGifts(gifts) {
  if (!Array.isArray(gifts)) throw Error('Invalid gift list');
  return gifts.filter(g => g && g.visibility === 'PUBLIC' && !['CANCELLED', 'ARCHIVED'].includes(g.status) && typeof g.name === 'string' && g.name.trim());
}
export function eventDateLabel(date) {
  if (!date) return 'Date to be announced';
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return 'Date to be announced';
  const [year, month, day] = date.split('-').map(Number);
  const parsed = new Date(Date.UTC(year, month - 1, day));
  if (parsed.getUTCFullYear() !== year || parsed.getUTCMonth() !== month - 1 || parsed.getUTCDate() !== day) return 'Date to be announced';
  return new Intl.DateTimeFormat(activeLocale("en-IN"), { timeZone: 'UTC', day: 'numeric', month: 'long', year: 'numeric' }).format(parsed);
}
export function moneyLabel(amount) {
  const n = Number(amount);
  if (!Number.isFinite(n) || n < 0) return 'Amount unavailable';
  return new Intl.NumberFormat(activeLocale("en-IN"), {style: 'currency', currency: 'INR', maximumFractionDigits: 2}).format(n);
}
