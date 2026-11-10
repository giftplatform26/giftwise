
import test from 'node:test';
import assert from 'node:assert/strict';

import {
  BACKGROUND_GALLERY,
  BACKGROUND_CATEGORIES,
  filterBackgrounds,
  findBackground
} from '../lib/background-gallery.mjs';

test('expanded background collection is available',()=>{
  assert.ok(BACKGROUND_GALLERY.length>=48);
});

test('background paths are unique and local',()=>{
  const paths=BACKGROUND_GALLERY.map(b=>b.src);
  assert.equal(new Set(paths).size,BACKGROUND_GALLERY.length);
  for(const p of paths)
    assert.match(p,/^\/invitation-backgrounds\/[a-z-]+\.svg$/);
});

test('all built-in backgrounds are free',()=>{
  assert.ok(BACKGROUND_GALLERY.every(b=>b.tier==='FREE'));
});

test('occasion recommendations show all designs, matching first',()=>{
  const items=filterBackgrounds('WEDDING');
  assert.equal(items.length,BACKGROUND_GALLERY.length);
  assert.equal(items[0].occasion,'WEDDING');
});

test('category filter and missing design lookup',()=>{
  assert.ok(BACKGROUND_CATEGORIES.includes('Floral'));
  assert.ok(
    filterBackgrounds('OTHER','Floral')
      .every(b=>b.category==='Floral')
  );
  assert.equal(findBackground('unknown'),null);
});
