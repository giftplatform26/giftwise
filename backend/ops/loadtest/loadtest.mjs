#!/usr/bin/env node
// GiftWise load test: many simulated guests and hosts hitting the busiest endpoints at once.
// Usage: node loadtest.mjs --base http://localhost:8080 --token <public-invitation-token> [--event <eventId> --user <username> --password <pw>]
//        [--guests 200] [--seconds 60]
// Guests open the invitation, features, schedule and wishes; hosts (if credentials given) load register, check-in and dashboard data.
// Reports requests per second, p50/p95/p99 latency and error rate per endpoint. Point it at a staging copy, never production.
const args = Object.fromEntries(process.argv.slice(2).reduce((a, v, i, all) => (v.startsWith('--') ? [...a, [v.slice(2), all[i + 1]]] : a), []));
const base = (args.base || 'http://localhost:8080').replace(/\/$/, '');
const token = args.token, eventId = args.event, guests = Number(args.guests || 200), seconds = Number(args.seconds || 60);
if (!token) { console.error('Missing --token (the public invitation token).'); process.exit(2); }

const stats = new Map();
const record = (name, ms, ok) => { const s = stats.get(name) ?? {lat: [], errors: 0}; s.lat.push(ms); if (!ok) s.errors++; stats.set(name, s); };
async function hit(name, url, init) {
  const t = performance.now();
  try { const r = await fetch(url, init); await r.arrayBuffer(); record(name, performance.now() - t, r.status < 500 && r.status !== 429); }
  catch { record(name, performance.now() - t, false); }
}

async function hostSession() {
  if (!args.user || !args.password || !eventId) return null;
  const jar = new Map();
  const cookie = () => [...jar].map(([k, v]) => `${k}=${v}`).join('; ');
  const keep = r => { for (const c of r.headers.getSetCookie?.() ?? []) { const [kv] = c.split(';'); const i = kv.indexOf('='); jar.set(kv.slice(0, i), kv.slice(i + 1)); } };
  keep(await fetch(`${base}/api/auth/csrf`));
  const r = await fetch(`${base}/api/auth/login`, {method: 'POST', headers: {'Content-Type': 'application/json', Cookie: cookie(), 'X-XSRF-TOKEN': jar.get('XSRF-TOKEN') ?? ''}, body: JSON.stringify({identifier: args.user, password: args.password})});
  keep(r);
  if (!r.ok) { console.error(`Host login failed (${r.status}); running guest traffic only.`); return null; }
  return {headers: {Cookie: cookie()}};
}

const guestFlow = [
  ['invitation', `${base}/api/public/events/${token}`],
  ['features', `${base}/api/public/events/${token}/features`],
  ['schedule', `${base}/api/public/celebrations/${token}/functions`],
  ['wishes', `${base}/api/public/celebrations/${token}/wishes`],
  ['live-updates', `${base}/api/public/events/${token}/live-updates`],
];
const hostFlow = eventId ? [
  ['host:features', `${base}/api/v1/events/${eventId}/features`],
  ['host:register', `${base}/api/v1/events/${eventId}/register`],
  ['host:checkin', `${base}/api/v1/events/${eventId}/checkin`],
  ['host:rsvps', `${base}/api/v1/events/${eventId}/rsvps`],
  ['host:attention', `${base}/api/v1/me/attention`],
] : [];

const host = await hostSession();
const end = Date.now() + seconds * 1000;
const worker = async (flow, init) => { while (Date.now() < end) for (const [name, url] of flow) { if (Date.now() >= end) break; await hit(name, url, init); } };
console.log(`Running ${guests} guests${host ? ' + 10 hosts' : ''} for ${seconds}s against ${base} …`);
const started = Date.now();
await Promise.all([...Array.from({length: guests}, () => worker(guestFlow)), ...(host ? Array.from({length: 10}, () => worker(hostFlow, host)) : [])]);
const elapsed = (Date.now() - started) / 1000;

const pct = (arr, p) => { const s = [...arr].sort((a, b) => a - b); return s[Math.min(s.length - 1, Math.floor(p / 100 * s.length))] ?? 0; };
let total = 0, errors = 0;
console.log('\nendpoint                 requests   rps    p50ms   p95ms   p99ms  errors');
for (const [name, s] of stats) {
  total += s.lat.length; errors += s.errors;
  console.log(`${name.padEnd(24)} ${String(s.lat.length).padStart(8)} ${(s.lat.length / elapsed).toFixed(0).padStart(5)} ${pct(s.lat, 50).toFixed(0).padStart(8)} ${pct(s.lat, 95).toFixed(0).padStart(7)} ${pct(s.lat, 99).toFixed(0).padStart(7)} ${String(s.errors).padStart(7)}`);
}
console.log(`\nTotal ${total} requests in ${elapsed.toFixed(0)}s = ${(total / elapsed).toFixed(0)} req/s, error rate ${(errors / Math.max(1, total) * 100).toFixed(2)}%`);
process.exit(errors / Math.max(1, total) > 0.01 ? 1 : 0);
