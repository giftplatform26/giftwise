import test from 'node:test';
import assert from 'node:assert/strict';

import {
  normalizeBackgroundTransform,
  backgroundImageRect,
  moveBackground,
  zoomBackground,
  resetBackgroundTransform
} from '../lib/background-position.mjs';

test('legacy designs receive default positioning', () => {
  assert.deepEqual(normalizeBackgroundTransform(undefined), {
    x: 0, y: 0, zoom: 1
  });
});

test('image covers the canvas without gaps', () => {
  const rect = backgroundImageRect(1200,800,600,800);
  assert.ok(rect.x <= 0);
  assert.ok(rect.y <= 0);
  assert.ok(rect.x + rect.width >= 600);
  assert.ok(rect.y + rect.height >= 800);
});

test('zoom increases rendered image dimensions', () => {
  const original = backgroundImageRect(1200,800,600,800);
  const zoomed = backgroundImageRect(
    1200,800,600,800,{x:0,y:0,zoom:1.5}
  );
  assert.ok(zoomed.width > original.width);
  assert.ok(zoomed.height > original.height);
});

test('drag updates position immutably', () => {
  const original = {x:0,y:0,zoom:1};
  const moved = moveBackground(original,35,-20);

  assert.deepEqual(moved,{x:35,y:-20,zoom:1});
  assert.deepEqual(original,{x:0,y:0,zoom:1});
});

test('position is clamped to avoid empty edges', () => {
  const rect = backgroundImageRect(
    1200,800,600,800,{x:9999,y:-9999,zoom:1}
  );

  assert.ok(rect.x <= 0);
  assert.ok(rect.y <= 0);
  assert.ok(rect.x + rect.width >= 600);
  assert.ok(rect.y + rect.height >= 800);
});

test('zoom stays between 100 and 300 percent', () => {
  assert.equal(zoomBackground({},0.1).zoom,1);
  assert.equal(zoomBackground({},8).zoom,3);
});

test('invalid input cannot produce NaN coordinates', () => {
  const t = normalizeBackgroundTransform({
    x:NaN,y:Infinity,zoom:-10
  });

  assert.deepEqual(t,{x:0,y:0,zoom:1});
});

test('reset restores center and original zoom', () => {
  assert.deepEqual(resetBackgroundTransform(),{
    x:0,y:0,zoom:1
  });
});

test('invalid dimensions are rejected', () => {
  assert.throws(
    () => backgroundImageRect(0,800,600,800),
    /Invalid background dimensions/
  );
});
