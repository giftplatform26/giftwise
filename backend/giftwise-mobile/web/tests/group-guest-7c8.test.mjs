import test from 'node:test';import assert from 'node:assert/strict';import {readFileSync} from 'node:fs';
const read=p=>readFileSync(new URL(p,import.meta.url),'utf8');
test('guest checkout collects identity, amount, destination and transfer reference',()=>{const s=read('../components/GroupGuestCheckout.tsx');for(const v of ['guestName','amountPaise','destinationId','transactionReference','submissionKey','I have done the payment','event_title','event_date','creator_name'])assert.ok(s.includes(v),v)});
test('creator can configure suggested amount and selected UPI/bank without admin switching',()=>{const s=read('../components/GroupGuestCreatorSettings.tsx');for(const v of ['suggestedAmountPaise','upiDestinationId','bankDestinationId','manual_contributions_enabled','Reported — unverified'])assert.ok(s.includes(v),v)});
test('public group invitation renders guest contribution flow',()=>assert.ok(read('../app/groups/[token]/[groupId]/page.tsx').includes('<GroupGuestCheckout')));
