// Shared photo album: client-side downscaling so phone photos upload quickly and stay under the server limit.

/** Scales (w, h) to fit inside max × max, never enlarging. */
export function fitWithin(w, h, max) {
  if (!(w > 0) || !(h > 0)) return {width: 0, height: 0};
  const s = Math.min(1, max / Math.max(w, h));
  return {width: Math.round(w * s), height: Math.round(h * s)};
}

export const MAX_UPLOAD_BYTES = 5 * 1024 * 1024;

/**
 * Re-encodes an image file as a JPEG no larger than `max` pixels on its long edge. Returns the original file
 * when the browser can't decode it (for example HEIC outside Safari) and it is already small enough.
 */
export async function shrinkPhoto(file, max = 2400, quality = 0.85) {
  try {
    const bitmap = await createImageBitmap(file, {imageOrientation: 'from-image'});
    const {width, height} = fitWithin(bitmap.width, bitmap.height, max);
    const canvas = document.createElement('canvas');
    canvas.width = width; canvas.height = height;
    canvas.getContext('2d').drawImage(bitmap, 0, 0, width, height);
    bitmap.close?.();
    const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', quality));
    if (blob && blob.size < file.size) return new File([blob], (file.name || 'photo').replace(/\.[^.]+$/, '') + '.jpg', {type: 'image/jpeg'});
    return file;
  } catch {
    return file;
  }
}

export function friendlySize(bytes) {
  const n = Number(bytes) || 0;
  if (n >= 1024 * 1024 * 1024) return `${(n / 1024 / 1024 / 1024).toFixed(1)} GB`;
  if (n >= 1024 * 1024) return `${(n / 1024 / 1024).toFixed(1)} MB`;
  return `${Math.max(1, Math.round(n / 1024))} KB`;
}
