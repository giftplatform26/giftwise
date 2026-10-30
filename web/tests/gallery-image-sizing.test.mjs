
import test from 'node:test';
import assert from 'node:assert/strict';
import { fittedDimensions } from '../lib/gallery-image-optimizer.mjs';

test('large phone photo fits gallery dimensions', () => {
  assert.deepEqual(
    fittedDimensions(5712, 4284, 2400, 4_320_000),
    { width: 2400, height: 1800 }
  );
});

test('small images are not enlarged', () => {
  assert.deepEqual(
    fittedDimensions(800, 600, 2400, 4_320_000),
    { width: 800, height: 600 }
  );
});

test('wide, tall and square images remain within limits', () => {
  for (const [w, h] of [[12000,1000], [1000,12000], [6000,6000]]) {
    const result = fittedDimensions(w, h, 2400, 4_320_000);
    assert.ok(result.width <= 2400);
    assert.ok(result.height <= 2400);
    assert.ok(result.width * result.height <= 4_320_000);
  }
});
