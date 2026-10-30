import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const component=readFileSync(new URL('../components/GroupMobileVerification.tsx',import.meta.url),'utf8');
const page=readFileSync(new URL('../app/groups/[token]/[groupId]/page.tsx',import.meta.url),'utf8');
test('member account security includes mobile OTP test',()=>{assert.match(page,/GroupMobileVerification token=\{token\} groupId=\{groupId\}/);});
test('mobile OTP explicitly never claims verified ownership',()=>{assert.match(component,/does not verify ownership/);assert.match(component,/local development only/);});
test('mobile OTP uses CSRF and authenticated session',()=>{assert.match(component,/X-XSRF-TOKEN/);assert.match(component,/credentials:'include'/);});
test('mobile OTP is linked to member group and challenge',()=>{assert.match(component,/challengeId:challenge,code,groupId/);});
