import test from 'node:test';
import assert from 'node:assert/strict';
import {calendarInvitation,socialLinks} from '../lib/invitation-sharing.mjs';
import {normalizeFeatures} from '../lib/event-features.mjs';
test('calendar uses all-day dates and exclusive end date across year boundary',()=>{
 const value=calendarInvitation({token:'test',title:'Together, with love;\nCelebrate',date:'2026-12-31',url:'https://example.test/e/test',now:new Date('2026-10-30T12:00:00Z')});
 assert.match(value,/DTSTART;VALUE=DATE:20261231/);assert.match(value,/DTEND;VALUE=DATE:20270101/);
 assert.ok(value.includes('SUMMARY:Together\\, with love\\;\\nCelebrate'));assert.match(value,/DTSTAMP:20261030T120000Z/);
});
test('calendar rejects invalid dates',()=>{for(const date of [null,'2026-02-30','invalid'])assert.equal(calendarInvitation({token:'test',title:'Party',date,url:'https://example.test'}),null)});
test('sharing encodes invitation URL and title without exposing personal details',()=>{
 const url='https://example.test/e/token?x=one&y=two';const links=socialLinks(url,'A & B');
 assert.equal(new URL(links.facebook).searchParams.get('u'),url);assert.equal(new URL(links.x).searchParams.get('url'),url);
 assert.ok(new URL(links.whatsapp).searchParams.get('text').includes(url));assert.match(links.email,/^mailto:/);
});
test('social sharing requires an explicit boolean grant',()=>{
 assert.equal(normalizeFeatures({}).SOCIAL_SHARING,false);assert.equal(normalizeFeatures({SOCIAL_SHARING:'true'}).SOCIAL_SHARING,false);assert.equal(normalizeFeatures({SOCIAL_SHARING:true}).SOCIAL_SHARING,true);
});
