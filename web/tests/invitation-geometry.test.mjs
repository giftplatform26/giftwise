import test from 'node:test';
import assert from 'node:assert/strict';

import {initialDesign} from '../lib/design.mjs';
import {
  QR_MARGIN, MIN_QR_SIZE, protectedQrRect,
  intersects, validateSafeLayout,
  safeUpdateElement, safeAddText
} from '../lib/invitation-geometry.mjs';

const fresh = () => structuredClone(initialDesign);

test('default invitation respects protected QR zone', () => {
  assert.equal(validateSafeLayout(fresh()), true);
});

test('QR has a quiet zone', () => {
  const qr = fresh().elements.find(e => e.id === 'qr');
  const zone = protectedQrRect(qr);
  assert.equal(zone.w, qr.w + QR_MARGIN * 2);
  assert.equal(zone.h, qr.h + QR_MARGIN * 2);
});

test('touching edges is not an overlap', () => {
  assert.equal(intersects(
    {x:0,y:0,w:10,h:10},
    {x:10,y:0,w:10,h:10}
  ), false);
});

test('text cannot move over QR', () => {
  const d = fresh();
  const next = safeUpdateElement(d,'title',{x:200,y:450});
  assert.equal(next,d);
});

test('text can move to a valid location', () => {
  const d = fresh();
  const next = safeUpdateElement(d,'title',{x:40,y:80});
  assert.notEqual(next,d);
  assert.equal(validateSafeLayout(next),true);
});

test('QR cannot move over existing text', () => {
  const d = fresh();
  assert.equal(safeUpdateElement(d,'qr',{x:190,y:300}),d);
});

test('QR cannot be made too small', () => {
  const d = fresh();
  assert.equal(safeUpdateElement(d,'qr',{w:MIN_QR_SIZE-1}),d);
});

test('QR cannot leave invitation boundaries', () => {
  const d = fresh();
  assert.equal(safeUpdateElement(d,'qr',{x:-1}),d);
});

test('QR cannot lose quiet-zone margin', () => {
  const d = fresh();
  assert.equal(safeUpdateElement(d,'qr',{x:0}),d);
});

test('QR cannot be deleted', () => {
  const d = fresh();
  const changed = structuredClone(d);
  changed.elements = changed.elements.filter(e=>e.id!=='qr');
  assert.equal(validateSafeLayout(changed),false);
});

test('new text cannot cover QR', () => {
  const d = fresh();
  const next = safeAddText(d,{
    id:'new-text',type:'text',text:'Hello',
    x:200,y:450,w:100,h:50
  });
  assert.equal(next,d);
});

test('overlapping imported layouts are rejected', () => {
  const d = fresh();
  d.elements.find(e=>e.id==='title').y=450;
  assert.equal(validateSafeLayout(d),false);
});

test('duplicate QR is rejected', () => {
  const d = fresh();
  d.elements.push({...d.elements.find(e=>e.id==='qr')});
  assert.equal(validateSafeLayout(d),false);
});

test('invalid geometry is rejected', () => {
  const d = fresh();
  d.elements.find(e=>e.id==='title').x=NaN;
  assert.equal(validateSafeLayout(d),false);
});
