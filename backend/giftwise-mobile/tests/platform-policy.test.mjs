import test from 'node:test';
import assert from 'node:assert/strict';
import { validateOrigin, navigationTarget, deepLinkPath, parseNativeMessage } from '../src/platform/policy.mjs';
const origin = 'https://celebrate.example';
test('release configuration requires an HTTPS origin without credentials or paths', () => {
  assert.equal(validateOrigin(origin), origin);
  for (const url of [undefined, 'http://celebrate.example', 'https://user:password@celebrate.example', origin+'/login', origin+'?token=x', origin+'#x', 'javascript:alert(1)', 'https://celebrate.example\\@evil.test']) assert.throws(() => validateOrigin(url));
  assert.equal(validateOrigin('http://10.0.2.2:3100', true), 'http://10.0.2.2:3100');
});
test('navigation uses exact origins and confines checkout exceptions to HTTPS', () => {
  assert.equal(navigationTarget(origin+'/events', origin), 'internal');
  assert.equal(navigationTarget('https://celebrate.example.evil.test/admin', origin), 'external');
  assert.equal(navigationTarget('https://checkout.example/pay', origin, ['https://checkout.example']), 'checkout');
  assert.equal(navigationTarget('http://checkout.example/pay', origin, ['http://checkout.example']), 'external');
  for (const url of ['javascript:alert(1)', 'file:///etc/passwd','data:text/html,hello','intent://anything']) assert.equal(navigationTarget(url,origin),'blocked');
  assert.equal(navigationTarget('upi://pay?pa=host%40bank', origin), 'external');
});
test('deep links preserve invitation and group paths without trusting foreign origins', () => {
  assert.equal(deepLinkPath('giftwise://open/e/token?lang=en',origin),'/e/token?lang=en');
  assert.equal(deepLinkPath(origin+'/groups/token/id',origin),'/groups/token/id');
  assert.equal(deepLinkPath('https://evil.test/admin',origin),null);
  assert.equal(deepLinkPath('giftwise://evil/admin',origin),null);
  assert.equal(deepLinkPath('giftwise://open//evil.test',origin),null);
});
test('native exports reject untrusted messages and sanitize filenames', () => {
  const raw=JSON.stringify({type:'export',name:'../../receipt.png',mime:'image/png',base64:'aGVsbG8='});
  const result=parseNativeMessage(raw,origin+'/events',origin);
  assert.equal(result.type,'export'); assert.equal(result.name,'------receipt.png');
  assert.throws(()=>parseNativeMessage(raw,'https://evil.test',origin));
  assert.throws(()=>parseNativeMessage('x'.repeat(18_000_001),origin,origin));
  assert.throws(()=>parseNativeMessage(JSON.stringify({type:'export',mime:'text/html',base64:'aGVsbG8='}),origin,origin));
  assert.throws(()=>parseNativeMessage(JSON.stringify({type:'share',text:'hello',url:'https://evil.test'}),origin,origin));
  assert.throws(()=>parseNativeMessage(JSON.stringify({type:'export',mime:'image/png',base64:'bad?'}),origin,origin));
});
test('share and clipboard return bounded content without authentication fields', () => {
  assert.deepEqual(parseNativeMessage(JSON.stringify({type:'share',text:'Join us',url:origin+'/e/test',token:'ignored'}),origin,origin),{type:'share',text:'Join us',url:origin+'/e/test'});
  assert.deepEqual(parseNativeMessage(JSON.stringify({type:'clipboard',text:'host@bank'}),origin,origin),{type:'clipboard',text:'host@bank'});
});
