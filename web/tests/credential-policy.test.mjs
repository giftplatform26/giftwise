import {test} from 'node:test';
import assert from 'node:assert/strict';
import {passwordRules,usernameRules} from '../lib/credential-policy.mjs';
const valid=rules=>rules.every(([,ok])=>ok);
test('new credential boundaries and composition',()=>{
 assert.ok(valid(passwordRules('Abcdef1!')));
 for(const s of ['Ab1!','abcdef1!','ABCDEF1!','Abcdefg!','Abcdef12','Aa1!'+'é'.repeat(35)])assert.equal(valid(passwordRules(s)),false,s);
 for(const s of ['Ab_1','1234','a'.repeat(30)])assert.ok(valid(usernameRules(s)));
 for(const s of ['_abc','abc','a'.repeat(31),'ab-c'])assert.equal(valid(usernameRules(s)),false);
});
