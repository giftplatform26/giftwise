// Information architecture for the platform administrator console (web only; see native-app.mjs).
// Every admin control lives in exactly one section. Sections are grouped by the job an administrator
// is doing, and each section is addressable as /admin#<id> so links, bookmarks and Back work.

export const ADMIN_GROUPS = [
  {id: 'start', label: ''},
  {id: 'access', label: 'People & access'},
  {id: 'money', label: 'Payments & revenue'},
  {id: 'growth', label: 'Growth'},
  {id: 'trust', label: 'Trust & safety'},
  {id: 'brand', label: 'Brand & content'},
  {id: 'me', label: ''}
];

export const ADMIN_SECTIONS = [
  {id: 'overview', group: 'start', label: 'Overview', title: 'Overview', description: 'Platform health, key numbers and shortcuts to every setting.'},
  {id: 'users', group: 'access', label: 'Users', title: 'Users', description: 'Every host and administrator account, with status, verified contacts and activity.'},
  {id: 'features', group: 'access', label: 'Platform features', title: 'Platform features', description: 'Turn whole features on or off for every host. A feature that is off here is off everywhere.'},
  {id: 'experiences', group: 'access', label: 'Site experiences', title: 'Site experiences', description: 'Site-wide extras such as the public demo, sharing previews and optional analytics.'},
  {id: 'hosts', group: 'access', label: 'Hosts & access', title: 'Hosts & access', description: 'Find a host and choose which features, payment methods, uploads and languages they can use.'},
  {id: 'events', group: 'access', label: 'Events', title: 'Events', description: 'Per-event switches for memory albums, Design with AI and celebration features.'},
  {id: 'payments', group: 'money', label: 'Payment methods', title: 'Payment methods', description: 'Switch UPI, bank transfers, Razorpay checkout and Razorpay Route on or off for everyone, then make exceptions for one event.'},
  {id: 'groups', group: 'money', label: 'Group gift payments', title: 'Group gift payments', description: 'See every group gift and decide whether its creator may switch the account that receives contributions.'},
  {id: 'monetization', group: 'money', label: 'Fees & partners', title: 'Fees & partners', description: 'Platform fees, pricing, affiliate partners, placements and collected revenue.'},
  {id: 'programs', group: 'growth', label: 'Growth programs', title: 'Growth programs', description: 'Referral rewards, the vendor directory and showcase submissions.'},
  {id: 'analytics', group: 'growth', label: 'Insights', title: 'Insights', description: 'Growth results, journey funnel and every tracked click with its outcome.'},
  {id: 'inbox', group: 'trust', label: 'Verification inbox', title: 'Verification inbox', description: 'Everything waiting to be verified: guest transfers, group contributions, receiving accounts and showcase submissions.'},
  {id: 'policies', group: 'trust', label: 'Sign-in & verification', title: 'Sign-in & verification', description: 'Application-wide verification switches and registration and recovery policies.'},
  {id: 'reports', group: 'trust', label: 'Money & moderation', title: 'Money & moderation reports', description: 'Reported group transfers, guestbook moderation and notification records.'},
  {id: 'activity', group: 'trust', label: 'Audit log', title: 'Audit log', description: 'Who changed what and when, for administration, accounts, gifts and payments.'},
  {id: 'system', group: 'trust', label: 'System health', title: 'System health', description: 'Production safeguards and branded email delivery.'},
  {id: 'appearance', group: 'brand', label: 'Branding', title: 'Branding', description: 'Platform name, logo and colours shown across the application and emails.'},
  {id: 'languages', group: 'brand', label: 'Languages', title: 'Languages', description: 'Supported languages, the default language and the translation workspace.'},
  {id: 'gallery', group: 'brand', label: 'Invitation gallery', title: 'Invitation gallery', description: 'Upload and publish invitation backgrounds and stickers for hosts.', href: '/admin/invitation-assets'},
  {id: 'account', group: 'me', label: 'My account', title: 'My account', description: 'Your profile picture, preferred language and contact verification.'}
];

const SECTION_IDS = new Set(ADMIN_SECTIONS.map(s => s.id));

export function adminSection(id) {
  return ADMIN_SECTIONS.find(s => s.id === id) || ADMIN_SECTIONS[0];
}

export function sectionHref(id, query = '') {
  const section = adminSection(id);
  if (section.href) return section.href;
  return `/admin#${section.id}${query ? '/' + encodeURIComponent(query) : ''}`;
}

/** Parses "#features/seating" into {section:'features', query:'seating'}. Unknown sections fall back to the overview. */
export function parseAdminHash(hash) {
  const raw = String(hash || '').replace(/^#/, '');
  const [id, ...rest] = raw.split('/');
  let query = '';
  try { query = decodeURIComponent(rest.join('/')); } catch { query = ''; }
  const section = SECTION_IDS.has(id) && !adminSection(id).href ? id : 'overview';
  return {section, query: section === 'overview' ? '' : query};
}

// Feature (module) categories, so 46 platform switches read as six short lists.
export const MODULE_CATEGORIES = [
  {id: 'guests', label: 'Guests & RSVP', codes: ['RSVP', 'GUEST_ATTENDANCE', 'GUEST_HOUSEHOLDS', 'GUEST_TRAVEL', 'GUEST_PASSES', 'GUEST_CHECKIN', 'SEATING_PLAN', 'CATERING_HEADCOUNT', 'GUEST_FEEDBACK', 'GUESTBOOK']},
  {id: 'planning', label: 'Planning tools', codes: ['CELEBRATION_PLANNING', 'EVENT_ESSENTIALS', 'EVENT_FUNCTIONS', 'PLANNING_CHECKLIST', 'RUN_SHEET', 'EVENT_BUDGET', 'BUDGET_SHARING', 'VENDOR_BOOKINGS', 'VENDOR_DIRECTORY', 'CO_HOSTS', 'EVENT_REUSE', 'YEARLY_REHOST', 'HOST_NOTIFICATIONS', 'OCCASION_REMINDERS']},
  {id: 'gifts', label: 'Gifts & contributions', codes: ['GROUP_GIFTING', 'GUEST_GIFTS', 'EXPERIENCE_GIFTS', 'SHAGUN_REGISTER', 'GIFT_LEDGER', 'RETURN_GIFTS', 'GROUP_COLLECTIONS']},
  {id: 'sharing', label: 'Invitations & sharing', codes: ['AI_INVITATION', 'WHATSAPP_SHARING', 'WHATSAPP_MESSAGING', 'SOCIAL_SHARING', 'STORY_CARDS', 'LIVE_UPDATES', 'LIVE_WALL']},
  {id: 'memories', label: 'Photos & memories', codes: ['MEMORY_ALBUM', 'SHARED_ALBUM', 'MEMORY_BOOK']},
  {id: 'acquisition', label: 'Growth & partners', codes: ['TRY_WITHOUT_ACCOUNT', 'PARTNER_ACCOUNTS', 'GUEST_FIRST_FREE', 'CELEBRATION_SHOWCASE', 'HOST_REFERRALS']}
];
const OTHER_CATEGORY = {id: 'other', label: 'Other features'};

export function moduleCategory(code) {
  return MODULE_CATEGORIES.find(c => c.codes.includes(code)) || OTHER_CATEGORY;
}

/**
 * Groups module rows by category after applying a text query (matches label or code) and an
 * on/off filter. `isOn` reads the state that the filter should use. Empty categories are dropped.
 */
export function groupModules(rows, labels, {query = '', state = 'ALL', isOn = r => !!r.enabled} = {}) {
  const q = query.trim().toLowerCase();
  const matches = rows.filter(r => {
    const label = (labels[r.module_code] || r.module_code).toLowerCase();
    if (q && !label.includes(q) && !r.module_code.toLowerCase().replaceAll('_', ' ').includes(q)) return false;
    if (state === 'ON' && !isOn(r)) return false;
    if (state === 'OFF' && isOn(r)) return false;
    return true;
  });
  const order = [...MODULE_CATEGORIES, OTHER_CATEGORY];
  return order.map(category => ({
    ...category,
    rows: matches.filter(r => moduleCategory(r.module_code).id === category.id)
      .sort((a, b) => (labels[a.module_code] || a.module_code).localeCompare(labels[b.module_code] || b.module_code))
  })).filter(group => group.rows.length > 0);
}

// "Find a setting": plain-language names and synonyms for controls, each pointing at its section.
export const SETTING_INDEX = [
  {label: 'UPI payments on/off', section: 'payments', keywords: 'upi qr vpa direct intent gpay phonepe paytm disable payments'},
  {label: 'Bank transfers on/off', section: 'payments', keywords: 'bank neft imps account transfer direct disable payments'},
  {label: 'Razorpay checkout on/off', section: 'payments', keywords: 'card netbanking gateway razorpay keys test live'},
  {label: 'Razorpay Route on/off', section: 'payments', keywords: 'route routing split settlement linked account payout vpa'},
  {label: 'Payment methods for one event', section: 'payments', keywords: 'event override upi bank razorpay channel'},
  {label: 'Payment methods for one host', section: 'hosts', keywords: 'host override upi bank razorpay route channel disable'},
  {label: 'Payment control history', section: 'payments', keywords: 'payment changes revision reason'},
  {label: 'Platform fees master switch', section: 'monetization', keywords: 'monetization fee pricing charge percentage commission'},
  {label: 'Pricing history', section: 'monetization', keywords: 'monetization revision fee changes'},
  {label: 'Affiliate partners & placements', section: 'monetization', keywords: 'affiliate partner attribution placement commission statement'},
  {label: 'Access limits & billing periods', section: 'monetization', keywords: 'premium host plan billing subscription'},
  {label: 'Collected revenue', section: 'monetization', keywords: 'revenue income earnings'},
  {label: 'Feature presets', section: 'features', keywords: 'bundle preset apply all hosts'},
  {label: 'Custom invitation uploads for a host', section: 'hosts', keywords: 'stickers backgrounds upload premium host'},
  {label: 'Host language', section: 'hosts', keywords: 'language scope host translation'},
  {label: 'Group member email verification for a host', section: 'hosts', keywords: 'group members email verify host'},
  {label: 'AI design access details', section: 'hosts', keywords: 'ai design credits provider host'},
  {label: 'All users and accounts', section: 'users', keywords: 'users accounts people hosts administrators directory disabled deleted'},
  {label: 'Verification inbox', section: 'inbox', keywords: 'verify pending awaiting waiting queue transfers reports accounts unverified'},
  {label: 'Group gift account switching', section: 'groups', keywords: 'group gift switching receiving account creator destination'},
  {label: 'Memory album for an event', section: 'events', keywords: 'album photos memories event'},
  {label: 'Design with AI for an event', section: 'events', keywords: 'ai invitation design event'},
  {label: 'Celebration feature controls for an event', section: 'events', keywords: 'event modules per event features'},
  {label: 'Event language', section: 'events', keywords: 'language scope event translation'},
  {label: 'Referral rewards', section: 'programs', keywords: 'invite a friend referral reward'},
  {label: 'Vendor directory listings', section: 'programs', keywords: 'vendors directory listing caterer photographer'},
  {label: 'Showcase submissions', section: 'programs', keywords: 'showcase approve celebration public'},
  {label: 'Growth results', section: 'analytics', keywords: 'acquisition results growth performance'},
  {label: 'Journey funnel (last 30 days)', section: 'analytics', keywords: 'funnel journey conversion'},
  {label: 'Clicks & outcomes', section: 'analytics', keywords: 'clicks interactions analytics outcomes downloads'},
  {label: 'Mobile OTP across the application', section: 'policies', keywords: 'msg91 sms otp mobile verification'},
  {label: 'Bypass email verification', section: 'policies', keywords: 'email verification bypass outage'},
  {label: 'Registration & recovery policies', section: 'policies', keywords: 'require email mobile group pin login policy recovery authentication'},
  {label: 'Group transfer reports', section: 'reports', keywords: 'transfers reported unverified verified rejected money'},
  {label: 'Guestbook moderation overview', section: 'reports', keywords: 'guestbook pending approved hidden moderation'},
  {label: 'Notification records', section: 'reports', keywords: 'notification outbox manual claim email sms'},
  {label: 'Administrative audit', section: 'activity', keywords: 'audit log history who changed'},
  {label: 'Account, gift & payment audit', section: 'activity', keywords: 'business audit gifts payments accounts'},
  {label: 'Production safeguards', section: 'system', keywords: 'production readiness configuration check backup'},
  {label: 'Branded email delivery', section: 'system', keywords: 'email delivery sent failed smtp'},
  {label: 'Platform name, logo & colours', section: 'appearance', keywords: 'branding logo colour color theme name'},
  {label: 'Languages & translations', section: 'languages', keywords: 'language translation default locale multi-language import export'},
  {label: 'Invitation backgrounds & stickers', section: 'gallery', keywords: 'gallery assets backgrounds decorations stickers publish'},
  {label: 'Profile picture & preferences', section: 'account', keywords: 'profile picture my account preferences'},
  {label: 'Verify my email or mobile', section: 'account', keywords: 'my email mobile verification account'}
];

/**
 * Searches the static index plus dynamic entries (feature switches, site experiences).
 * Returns at most `limit` results ranked by where the query matched.
 */
export function findSettings(query, extra = [], limit = 8) {
  const q = String(query || '').trim().toLowerCase();
  if (q.length < 2) return [];
  const words = q.split(/\s+/);
  const scored = [];
  for (const entry of [...SETTING_INDEX, ...extra]) {
    const label = entry.label.toLowerCase();
    const haystack = `${label} ${entry.keywords || ''} ${adminSection(entry.section).label.toLowerCase()}`;
    if (!words.every(w => haystack.includes(w))) continue;
    const score = label.startsWith(q) ? 0 : label.includes(q) ? 1 : 2;
    scored.push({...entry, score});
  }
  return scored.sort((a, b) => a.score - b.score || a.label.localeCompare(b.label)).slice(0, limit);
}
