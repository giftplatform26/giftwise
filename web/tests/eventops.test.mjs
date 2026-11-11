import test from 'node:test';
import assert from 'node:assert/strict';
import {groupTasks, percent, budgetHealth, clock, searchGuests} from '../lib/eventops.mjs';

test('tasks are grouped into overdue, this week, later, undated and done', () => {
  const today = new Date(2026, 10, 11);
  const g = groupTasks([{dueDate: '2026-11-01'}, {dueDate: '2026-11-14'}, {dueDate: '2026-12-30'}, {}, {done: true, dueDate: '2026-11-01'}], today);
  assert.deepEqual([g.overdue.length, g.week.length, g.later.length, g.undated.length, g.done.length], [1, 1, 1, 1, 1]);
});

test('percent is safe and capped', () => {
  assert.equal(percent(5, 0), 0);
  assert.equal(percent(150, 100), 100);
  assert.equal(percent(33, 100), 33);
});

test('budget health speaks plainly', () => {
  assert.equal(budgetHealth({}).tone, 'neutral');
  assert.equal(budgetHealth({totalBudgetPaise: 100, plannedPaise: 120}).label, 'Over budget');
  assert.equal(budgetHealth({totalBudgetPaise: 100, committedPaise: 95}).tone, 'warning');
  assert.equal(budgetHealth({totalBudgetPaise: 100, plannedPaise: 40}).tone, 'good');
});

test('clock formats times and ignores junk', () => {
  assert.ok(clock('18:30:00').length > 0);
  assert.equal(clock(''), '');
  assert.equal(clock('xx'), '');
});

test('check-in search finds word starts and puts arrived guests last', () => {
  const g = [{name: 'Ravi Kumar', arrived: true}, {name: 'Asha Rao', arrived: false}, {name: 'Kumar Swamy', arrived: false}];
  assert.deepEqual(searchGuests(g, 'kum').map(x => x.name), ['Kumar Swamy', 'Ravi Kumar']);
  assert.equal(searchGuests(g, '').length, 3);
});
