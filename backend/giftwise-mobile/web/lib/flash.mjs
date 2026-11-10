export const FLASH_KEY = 'gift-platform-flash-v1';
export const TYPES = Object.freeze(['success', 'error', 'warning', 'info']);
export function validateFlash(type, message) {
  if (!TYPES.includes(type)) throw new Error('Unsupported notification type');
  if (typeof message !== 'string' || !message.trim() || message.length > 240) throw new Error('Invalid notification message');
  return {type, message: message.trim()};
}
export function saveFlash(storage, type, message) {
  const flash = validateFlash(type, message);
  try { storage?.setItem(FLASH_KEY, JSON.stringify(flash)); return true; } catch { return false; }
}
export function consumeFlash(storage) {
  if (!storage) return null;
  let raw;
  try { raw = storage.getItem(FLASH_KEY); storage.removeItem(FLASH_KEY); } catch { return null; }
  if (!raw) return null;
  try { const parsed = JSON.parse(raw); return validateFlash(parsed.type, parsed.message); } catch { return null; }
}
