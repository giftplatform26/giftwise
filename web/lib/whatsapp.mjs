// WhatsApp messaging helpers shared by the host page and tests.
export const STATUS_LABEL = {QUEUED: 'Sending', SENDING: 'Sending', SENT: 'Sent', DELIVERED: 'Delivered', READ: 'Read', FAILED: 'Not delivered'};

/** How many guests a send would reach, matching the server's audience filter (opted-out guests excluded). */
export function audienceCount(contacts, audience) {
  const reachable = (contacts ?? []).filter(c => !c.optedOut);
  if (audience === 'NO_REPLY') return reachable.filter(c => !c.replyAttendance).length;
  if (audience === 'ATTENDING') return reachable.filter(c => c.replyAttendance === 'YES').length;
  return reachable.length;
}
