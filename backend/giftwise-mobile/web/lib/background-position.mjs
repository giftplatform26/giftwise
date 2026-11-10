export const DEFAULT_BACKGROUND_TRANSFORM = Object.freeze({
  x: 0,
  y: 0,
  zoom: 1
});

const finite = value =>
  typeof value === 'number' && Number.isFinite(value);

export function normalizeBackgroundTransform(value) {
  return {
    x: finite(value?.x) ? value.x : 0,
    y: finite(value?.y) ? value.y : 0,
    zoom: finite(value?.zoom)
      ? Math.min(3, Math.max(1, value.zoom))
      : 1
  };
}

export function backgroundImageRect(
  imageWidth,
  imageHeight,
  canvasWidth,
  canvasHeight,
  transform
) {
  if (![imageWidth,imageHeight,canvasWidth,canvasHeight]
      .every(v => finite(v) && v > 0)) {
    throw Error('Invalid background dimensions');
  }

  const t = normalizeBackgroundTransform(transform);

  const cover = Math.max(
    canvasWidth / imageWidth,
    canvasHeight / imageHeight
  );

  const width = imageWidth * cover * t.zoom;
  const height = imageHeight * cover * t.zoom;

  // Clamp translation to prevent empty canvas edges.
  const maxX = (width - canvasWidth) / 2;
  const maxY = (height - canvasHeight) / 2;

  const x = Math.max(-maxX, Math.min(maxX, t.x));
  const y = Math.max(-maxY, Math.min(maxY, t.y));

  return {
    x: (canvasWidth - width) / 2 + x,
    y: (canvasHeight - height) / 2 + y,
    width,
    height,
    offsetX: x,
    offsetY: y
  };
}

export function moveBackground(transform, dx, dy) {
  const t = normalizeBackgroundTransform(transform);

  if (!finite(dx) || !finite(dy)) return t;

  return {
    ...t,
    x: t.x + dx,
    y: t.y + dy
  };
}

export function zoomBackground(transform, zoom) {
  const t = normalizeBackgroundTransform(transform);

  return normalizeBackgroundTransform({
    ...t,
    zoom
  });
}

export function resetBackgroundTransform() {
  return {...DEFAULT_BACKGROUND_TRANSFORM};
}
