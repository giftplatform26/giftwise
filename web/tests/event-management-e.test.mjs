// Batches E1–E3: every new switch is labelled in the admin screens, wired into the menu and has its pages.
import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync, existsSync} from 'node:fs';

const read = (p) => readFileSync(new URL(`../${p}`, import.meta.url), 'utf8');
const CODES = {GUEST_HOUSEHOLDS: '/guests', GUEST_TRAVEL: '/travel', SHARED_ALBUM: '/shared-album', GUEST_FEEDBACK: '/feedback', VENDOR_BOOKINGS: '/vendors', BUDGET_SHARING: null};

test('each new module has a platform-admin and an event-admin label', () => {
  const admin = read('app/admin/page.tsx'), event = read('components/AdminEventModules.tsx');
  for (const code of Object.keys(CODES)) {
    assert.match(admin, new RegExp(`${code}:'[^']+'`), `admin label for ${code}`);
    assert.match(event, new RegExp(`${code}:'[^']+'`), `event label for ${code}`);
  }
});

test('each module with a page appears in the host menu only when switched on, and has host + helper routes', () => {
  const nav = read('components/HostWorkspace.tsx');
  for (const [code, path] of Object.entries(CODES)) {
    if (!path) continue;
    assert.ok(nav.includes(`features.${code}?[{href:base+'${path}'`), `menu entry for ${code}`);
    assert.ok(existsSync(new URL(`../app/events/[id]${path}/page.tsx`, import.meta.url)), `host page ${path}`);
    assert.ok(existsSync(new URL(`../app/helping/[id]${path}/page.tsx`, import.meta.url)), `helper page ${path}`);
  }
});

test('guest-facing pages exist: travel form, album, feedback, budget invite, printable passes', () => {
  for (const p of ['app/t/[token]/page.tsx', 'app/a/[token]/page.tsx', 'app/e/[token]/feedback/page.tsx', 'app/budget-invite/[token]/page.tsx', 'app/events/[id]/passes/print/page.tsx'])
    assert.ok(existsSync(new URL(`../${p}`, import.meta.url)), p);
});

test('the budget page becomes read-only for view-only family and shows sharing only to the owner', () => {
  const page = read('app/events/[id]/budget/page.tsx');
  assert.match(page, /readOnly=b\?\.access==='VIEW'/);
  assert.match(page, /owner&&<BudgetSharing/);
});
