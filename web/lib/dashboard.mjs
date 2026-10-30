export const EVENT_STATES = ['DRAFT', 'PUBLISHED', 'COMPLETED', 'CANCELLED'];
export function summarizeEvents(events) {
  if (!Array.isArray(events)) throw new TypeError('Expected event list');
  const summary = {total: events.length, draft: 0, published: 0, completed: 0, cancelled: 0};
  for (const event of events) {
    if (event && typeof event.status === 'string') {
      const state = event.status.toUpperCase();
      if (EVENT_STATES.includes(state)) summary[state.toLowerCase()]++;
    }
  }
  return summary;
}
export function invitationPath(event) {
  if (event?.status !== 'PUBLISHED' || !/^[0-9a-f]{8}-[0-9a-f-]{27,}$/i.test(event?.publicToken || '')) return null;
  return `/e/${encodeURIComponent(event.publicToken)}`;
}
export function eventActions(event) {
  if (!event || typeof event.id !== 'string' || !/^[0-9a-f-]{36}$/i.test(event.id)) return [];
  const id = encodeURIComponent(event.id);
  return [
    {label:'Manage gifts',href:`/gifts?eventId=${id}`},
    {label:'Design invitation',href:`/events/${id}/qr`},
    {label:'Preview',href:`/events/${id}/preview`},
    ...(invitationPath(event) ? [{label:'Guest invitation',href:invitationPath(event)}] : []),
  ];
}
export function safeEventDate(date) {
  if (typeof date !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(date)) return 'Date to be decided';
  const parsed = new Date(`${date}T12:00:00Z`);
  return Number.isNaN(parsed.getTime()) || parsed.toISOString().slice(0,10) !== date ? 'Date to be decided' : new Intl.DateTimeFormat('en-IN',{day:'numeric',month:'short',year:'numeric',timeZone:'UTC'}).format(parsed);
}
