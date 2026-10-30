import test from 'node:test';
import assert from 'node:assert/strict';
import {eventBannerPhrase} from '../lib/focused.mjs';
const base='/events/11111111-1111-4111-8111-111111111111';
test('each celebration page has its own brief banner phrase',()=>{
 const cases=[['/gifts','Curate Joy'],[base,'Plan Beautifully'],[base+'/celebration','Gather Together'],[base+'/contributions','Shared Generosity'],[base+'/qr','Create Wonder'],[base+'/preview','First Glimpse'],[base+'/notifications','Stay Connected'],[base+'/thank-you','Share Gratitude']];
 for(const [path,phrase] of cases){assert.equal(eventBannerPhrase(path),phrase);assert.equal(phrase.split(' ').length,2)}
 assert.equal(new Set(cases.map(([path])=>eventBannerPhrase(path))).size,cases.length);
});
test('trailing slashes and an unfamiliar section have readable labels',()=>{assert.equal(eventBannerPhrase(base+'/preview/'),'First Glimpse');assert.equal(eventBannerPhrase(base+'/future-section'),'Celebrate Together')});
