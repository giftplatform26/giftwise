// Event-management tools: checklist grouping, budget maths, run-sheet times, check-in search.
import {personKey} from './register.mjs';

/** Groups tasks the way hosts think about them: what's late, what's this week, what's later, what's done. */
export function groupTasks(tasks, today = new Date()) {
  const day = (d) => new Date(`${d}T12:00:00`);
  const t0 = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 12);
  const week = new Date(t0); week.setDate(week.getDate() + 7);
  const groups = {overdue: [], week: [], later: [], undated: [], done: []};
  for (const t of tasks ?? []) {
    if (t.done) groups.done.push(t);
    else if (!t.dueDate) groups.undated.push(t);
    else if (day(t.dueDate) < t0) groups.overdue.push(t);
    else if (day(t.dueDate) <= week) groups.week.push(t);
    else groups.later.push(t);
  }
  return groups;
}

/** Whole-number percentage, capped 0–100, safe for zero totals. */
export function percent(part, whole) {
  if (!whole || whole <= 0) return 0;
  return Math.max(0, Math.min(100, Math.round((part / whole) * 100)));
}

/** Budget health in plain words for the summary card. */
export function budgetHealth(summary) {
  if (!summary?.totalBudgetPaise) return {tone: 'neutral', label: 'Set a total budget to see how you are doing'};
  const used = Math.max(summary.plannedPaise ?? 0, summary.committedPaise ?? 0);
  const p = percent(used, summary.totalBudgetPaise);
  if (used > summary.totalBudgetPaise) return {tone: 'danger', label: 'Over budget'};
  if (p >= 90) return {tone: 'warning', label: `${p}% of budget allocated`};
  return {tone: 'good', label: `${p}% of budget allocated`};
}

/** "18:30:00" → "6:30 pm" in the viewer's locale. */
export function clock(t) {
  if (!t) return '';
  const [h, m] = String(t).split(':').map(Number);
  if (!Number.isFinite(h) || !Number.isFinite(m)) return '';
  return new Intl.DateTimeFormat(undefined, {hour: 'numeric', minute: '2-digit'}).format(new Date(2000, 0, 1, h, m));
}

/** Check-in search: matches any word start, ignores case/accents; arrived guests sink to the bottom. */
export function searchGuests(guests, query) {
  const q = personKey(query);
  const list = (guests ?? []).filter(g => !q || personKey(g.name).split(' ').some(w => w.startsWith(q)) || personKey(g.name).includes(q));
  return [...list].sort((a, b) => Number(a.arrived) - Number(b.arrived) || a.name.localeCompare(b.name));
}

/** @type {[string, string][]} */
export const MEAL_CHOICES = [['VEG', 'Vegetarian'], ['JAIN', 'Jain'], ['NON_VEG', 'Non-vegetarian'], ['VEGAN', 'Vegan'], ['NONE', 'No meal']];
export const MEAL_LABEL = Object.fromEntries([...MEAL_CHOICES, ['NOT_SAID', 'Not specified']]);
