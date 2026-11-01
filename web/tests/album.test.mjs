import test from 'node:test';
import assert from 'node:assert/strict';
import {memoryIndex,memoryDuration,mergeMemories} from '../lib/album.mjs';
test('slideshow wraps in both directions and handles an empty album',()=>{
 assert.equal(memoryIndex(0,-1,4),3);assert.equal(memoryIndex(3,1,4),0);assert.equal(memoryIndex(0,1,0),0);
});
test('long wishes get reading time without exceeding 45 seconds',()=>{
 assert.equal(memoryDuration('With love',15),15000);assert.ok(memoryDuration('love '.repeat(70),6)>20000);assert.equal(memoryDuration('love '.repeat(400),6),45000);
});
test('loading more moments preserves order without repeating overlapping memories',()=>{
 const a={id:'a'},b={id:'b'},c={id:'c'};assert.deepEqual(mergeMemories([a,b],[b,c,c]),[a,b,c]);
});
