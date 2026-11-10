export const QR_MARGIN = 8;
export const MIN_QR_SIZE = 160;
export const MIN_TEXT_WIDTH = 30;
export const MIN_TEXT_HEIGHT = 20;
import {validStickerElement} from './invitation-stickers.mjs';

const finite = n => typeof n === 'number' && Number.isFinite(n);

export function protectedQrRect(qr) {
  return {
    x: qr.x - QR_MARGIN,
    y: qr.y - QR_MARGIN,
    w: qr.w + QR_MARGIN * 2,
    h: qr.h + QR_MARGIN * 2
  };
}

export function intersects(a, b) {
  return a.x < b.x + b.w &&
         a.x + a.w > b.x &&
         a.y < b.y + b.h &&
         a.y + a.h > b.y;
}

export function validateSafeLayout(design) {
  if (!design || !finite(design.width) || !finite(design.height) ||
      !Array.isArray(design.elements)) return false;

  const elements = design.elements;
  const qrs = elements.filter(e => e.id === 'qr' && e.type === 'qr');

  if (qrs.length !== 1 ||
      elements.filter(e => e.type === 'qr').length !== 1 ||
      new Set(elements.map(e => e.id)).size !== elements.length) return false;

  const qr = qrs[0];

  for (const e of elements) {
    if (!['text', 'qr', 'sticker'].includes(e.type) ||
        (e.type === 'sticker' && !validStickerElement(e)) ||
        ![e.x,e.y,e.w,e.h].every(finite) ||
        e.w <= 0 || e.h <= 0 ||
        (e.type === 'sticker' && (e.w < 24 || e.h < 24)) ||
        e.x < 0 || e.y < 0 ||
        e.x + e.w > design.width ||
        e.y + e.h > design.height) return false;
  }

  if (qr.w < MIN_QR_SIZE || qr.h < MIN_QR_SIZE) return false;

  const zone = protectedQrRect(qr);

  if (zone.x < 0 || zone.y < 0 ||
      zone.x + zone.w > design.width ||
      zone.y + zone.h > design.height) return false;

  return elements.every(e =>
    e.id === 'qr' || !intersects(e, zone)
  );
}

export function safeUpdateElement(design, id, patch) {
  if (!design?.elements?.some(e => e.id === id)) return design;

  const next = {
    ...design,
    elements: design.elements.map(e =>
      e.id === id ? {...e, ...patch} : e
    )
  };

  return validateSafeLayout(next) ? next : design;
}

export function safeAddText(design, element) {
  if (!element || element.type !== 'text' ||
      design.elements.some(e => e.id === element.id)) return design;

  const next = {
    ...design,
    elements: [...design.elements, element]
  };

  return validateSafeLayout(next) ? next : design;
}
