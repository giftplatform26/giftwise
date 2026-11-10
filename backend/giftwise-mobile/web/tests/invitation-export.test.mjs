
import test from 'node:test';
import assert from 'node:assert/strict';
import {
  FONT_CHOICES,
  safeFont,
  wrapLines,
  validateExport
} from '../lib/invitation-export.mjs';
import {initialDesign} from '../lib/design.mjs';

test('twelve font choices are available',()=>{
  assert.equal(FONT_CHOICES.length,12);
});

test('unknown font falls back to known safe family',()=>{
  assert.equal(safeFont('evil'),FONT_CHOICES[0].value);
});

test('valid invitation needs real QR URL',()=>{
  assert.equal(validateExport(initialDesign,'blob:real'),true);
  assert.throws(
    ()=>validateExport(initialDesign,''),
    /Authenticated QR/
  );
});

test('rejects missing or duplicate QR',()=>{
  assert.throws(
    ()=>validateExport(
      {...initialDesign,elements:[]},
      'blob:real'
    )
  );

  assert.throws(
    ()=>validateExport(
      {
        ...initialDesign,
        elements:[
          ...initialDesign.elements,
          initialDesign.elements.find(e=>e.id==='qr')
        ]
      },
      'blob:real'
    )
  );
});

test('wrapLines retains explicit paragraphs',()=>{
  const ctx={
    measureText:s=>({width:s.length*10})
  };
  assert.deepEqual(
    wrapLines(ctx,'Hello world\nNext',55),
    ['Hello','world','Next']
  );
});
