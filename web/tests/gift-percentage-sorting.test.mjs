import test from 'node:test';
import assert from 'node:assert/strict';
import {sortWishlist} from '../lib/gift-sorting.mjs';
const gifts=[{id:'a',priority:'LOW',targetAmount:100},{id:'b',priority:'HIGH',targetAmount:200},{id:'c',priority:'MEDIUM',targetAmount:50}];
const progress={a:{contributedPaise:5000},b:{contributedPaise:20000},c:{contributedPaise:0}};
test('highest and lowest gifted percentage',()=>{
 assert.deepEqual(sortWishlist(gifts,'percentage-desc',progress).map(g=>g.id),['b','a','c']);
 assert.deepEqual(sortWishlist(gifts,'percentage-asc',progress).map(g=>g.id),['c','a','b']);
});
test('percentage uses each gift target, not absolute amount',()=>{
 const x=[{id:'x',targetAmount:1000},{id:'y',targetAmount:100}];
 const p={x:{contributedPaise:20000},y:{contributedPaise:5000}};
 assert.deepEqual(sortWishlist(x,'percentage-desc',p).map(g=>g.id),['y','x']);
});
test('missing progress, over target, and equal percentages',()=>{
 const p={a:{contributedPaise:999999},b:{contributedPaise:20000}};
 assert.deepEqual(sortWishlist(gifts,'percentage-desc',p).map(g=>g.id).slice(0,2),['b','a']);
 assert.equal(sortWishlist(gifts,'percentage-asc',{}).length,3);
});
test('sorting never mutates source array',()=>{
 sortWishlist(gifts,'percentage-desc',progress);
 assert.deepEqual(gifts.map(g=>g.id),['a','b','c']);
});
