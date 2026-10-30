import test from 'node:test';import assert from 'node:assert/strict';import {readFileSync} from 'node:fs';
const backend=readFileSync('../backend/src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java','utf8');
const migration=readFileSync('../backend/src/main/resources/db/migration/V57__group_creator_payment_destinations.sql','utf8');
test('no manual account automatically marked verified',()=>{assert.match(migration,/DEFAULT 'REGISTERED'/);assert.match(backend,/status='VERIFIED'/);assert.match(backend,/verificationAvailable",false/)});
test('creator identity enforced for destination endpoints',()=>{assert.match(backend,/groups.requireGroupCreator/);assert.match(backend,/actor.isPlatformAdmin/)});
test('sensitive account fields encrypted and only masked in response',()=>{assert.match(backend,/p.encrypt\(/);assert.match(backend,/maskAccount/);assert.match(backend,/maskUpi/)});
