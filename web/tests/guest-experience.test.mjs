import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const read=path=>readFileSync(new URL(`../${path}`,import.meta.url),'utf8');
const invitation=read('components/focused/Invitation.tsx');
const actions=read('components/GuestActions.tsx');
const celebration=read('components/GuestCelebration.tsx');
const board=read('components/GuestGiftBoard.tsx');
const localization=read('components/LocalizationProvider.tsx');

test('hero has no duplicate Contribute button but keeps exactly one group contribution entry',()=>{
  assert.doesNotMatch(actions,/guest-action-primary/);
  assert.equal((actions.match(/<GroupContribution /g)||[]).length,1);
  assert.match(actions,/features\.GIFTING_ENABLED!==false&&features\.GROUP_GIFTING&&<GroupContribution token=\{token\} compact\/>/);
});
test('gift cards are compact: one action row, no per-card legal disclosure or empty image box',()=>{
  assert.doesNotMatch(invitation,/focusGiftDetails/);
  assert.match(invitation,/\{g\.imageUrl&&<div className="focusGiftIllustration">/);
  assert.match(invitation,/<div className="focusGiftActions">/);
  assert.match(invitation,/launchLabel=\{localizeText\('Gift with a group'\)\}/);
  assert.match(invitation,/!preview&&!fullyGifted&&event\.features\?\.GROUP_GIFTING/);
});
test('no manual refresh buttons or duplicate brand footer on the guest page',()=>{
  assert.doesNotMatch(board,/Refresh gifts/);
  assert.doesNotMatch(celebration,/Refresh wishes/);
  assert.doesNotMatch(invitation,/focusInvitationFooter/);
  assert.match(celebration,/visibilitychange/);
});
test('RSVP and wishes show a single heading each',()=>{
  assert.doesNotMatch(celebration,/Will you join us\? · RSVP/);
  assert.doesNotMatch(celebration,/THE INVITATION/);
});
test('below-the-fold sections are lazy and invitation + progress load in parallel',()=>{
  for(const name of ['GuestGiftBoard','GroupContribution','CommercialPlacements','GuestContributionCheckout','GuestCelebration','GuestDiscovery'])
    assert.match(invitation,new RegExp(`const ${name}=dynamic\\(\\(\\)=>import\\('\\.\\./${name}'\\)\\)`),name);
  assert.match(invitation,/const progress=loadProgress\(controller\.signal\);fetch\(`\/backend\/api\/public\/events\//);
});
test('language bar stays quiet for guests and only appears with a real choice',()=>{
  assert.doesNotMatch(localization,/setError\(e instanceof Error\?e\.message:'Language settings are unavailable'\)/);
  assert.match(localization,/catalog\.languages\.length<2\)return null/);
  assert.match(localization,/Date\.now\(\)-last>CATALOG_REFRESH_MS/);
});
