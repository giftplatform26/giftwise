import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';

// The app is a native shell around the main web application (gift-platform/web); there is no copy.
const web = new URL('../../../web/', import.meta.url);
const app = fs.readFileSync(new URL('../App.tsx', import.meta.url), 'utf8');

function pageExists(route) {
  const clean = route.split(/[?#]/, 1)[0].replace(/^\/+|\/+$/g, '');
  return fs.existsSync(new URL(`app/${clean ? clean + '/' : ''}page.tsx`, web));
}

test('the app uses the main web application rather than a bundled copy', () => {
  assert.equal(fs.existsSync(new URL('../web', import.meta.url)), false);
  const scripts = JSON.parse(fs.readFileSync(new URL('../package.json', import.meta.url), 'utf8')).scripts;
  for (const name of ['web', 'web:build', 'web:start', 'web:test']) assert.match(scripts[name], /--prefix \.\.\/\.\.\/web /, name);
});

test('every page opened from the native home screen exists in the web application', () => {
  const routes = [...app.matchAll(/(?:path: |open\(|\[".*?",\s*)['"](\/[^'"]*)['"]/g)].map(m => m[1]);
  assert.ok(routes.length >= 8, `expected native routes, found ${routes}`);
  for (const route of routes) assert.ok(pageExists(route), `App.tsx opens ${route}, but web/app has no page for it`);
});

test('the WebView identifies itself so the web app keeps administration web-only', () => {
  const workspace = fs.readFileSync(new URL('../src/platform/Workspace.tsx', import.meta.url), 'utf8');
  assert.match(workspace, /applicationNameForUserAgent=\{NATIVE_USER_AGENT\}/);
  const token = workspace.match(/NATIVE_USER_AGENT = '([A-Za-z]+)\//)?.[1];
  const shared = fs.readFileSync(new URL('lib/native-app.mjs', web), 'utf8');
  assert.equal(shared.match(/NATIVE_APP_TOKEN = '([A-Za-z]+)'/)?.[1], token);
});
