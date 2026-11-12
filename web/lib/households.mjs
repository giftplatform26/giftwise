// Guest list & households: labels, party sizes, filtering and the paste-import template.

export const SIDES = ['BRIDE', 'GROOM', 'HOST', 'FAMILY', 'FRIENDS', 'WORK', 'OTHER'];
const SIDE_LABELS = {BRIDE: "Bride's side", GROOM: "Groom's side", HOST: 'Our side', FAMILY: 'Family', FRIENDS: 'Friends', WORK: 'Work', OTHER: 'Other', UNASSIGNED: 'No side yet'};
export const MEALS = {VEG: 'Veg', JAIN: 'Jain', NON_VEG: 'Non-veg', VEGAN: 'Vegan'};
export const AGES = {ADULT: 'Adult', CHILD: 'Child', INFANT: 'Infant'};
export const RSVP_LABELS = {PENDING: 'Awaiting reply', YES: 'Coming', NO: 'Not coming', MAYBE: 'Maybe'};

export function sideLabel(code) { return SIDE_LABELS[code] ?? (code || ''); }

/** People counted for a household: attending members when any are listed, otherwise the expected size. */
export function partyOf(h) {
  const members = (h?.members ?? []).filter((m) => m && String(m.name ?? '').trim());
  if (members.length) return members.filter((m) => m.attending !== false).length;
  const n = Number(h?.expectedSize);
  return Number.isFinite(n) && n >= 1 ? Math.min(30, Math.floor(n)) : 1;
}

const words = (s) => String(s ?? '').toLowerCase().normalize('NFKD').replace(/[̀-ͯ]/g, '').split(/[^\p{L}\p{N}+]+/u).filter(Boolean);

/** Search by the start of any word in the household name, member names, phone or city; plus side and RSVP filters. */
export function filterHouseholds(list, {q = '', side = '', rsvp = ''} = {}) {
  const terms = words(q);
  return (list ?? []).filter((h) => {
    if (side && (h.side ?? 'UNASSIGNED') !== side) return false;
    if (rsvp && h.rsvp !== rsvp) return false;
    if (!terms.length) return true;
    const hay = [...words(h.name), ...words(h.city), ...words(h.group), ...(h.members ?? []).flatMap((m) => words(m.name)), String(h.phone ?? '').replace(/\D/g, '')];
    return terms.every((t) => hay.some((w) => w.startsWith(t.replace(/^\+/, '')) || (/^\d{3,}$/.test(t) && w.includes(t))));
  });
}

export const IMPORT_TEMPLATE = 'Name, People, Phone, Side, Group, City, Email, Outstation\nSharma family, 4, +919876543210, Bride, College friends, Pune, , Yes\nRavi Kumar, 2, , Groom, , Chennai, ravi@example.com, No';

/** A blank member row for the editor. */
export function blankMember() { return {name: '', ageGroup: 'ADULT', meal: '', attending: true}; }
