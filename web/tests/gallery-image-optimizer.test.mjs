import test from 'node:test';
import assert from 'node:assert/strict';
import {fittedDimensions,MAX_UPLOAD_BYTES} from '../lib/gallery-image-optimizer.mjs';
test('keeps small images at native size',()=>assert.deepEqual(fittedDimensions(500,600),{width:500,height:600}));
test('fits large images under dimension and pixel limits',()=>{const d=fittedDimensions(8000,6000);assert.ok(d.width<=4096&&d.height<=4096&&d.width*d.height<=12000000);});
test('rejects invalid dimensions',()=>assert.throws(()=>fittedDimensions(0,10)));
test('upload limit remains 2 MiB',()=>assert.equal(MAX_UPLOAD_BYTES,2097152));
