// Routes that render inside the signed-in host workspace (sidebar, session timeout).
// Guest pages such as /e/<token>, /a/<token> and /t/<token> are not host routes.
const EXACT=new Set(['/studio','/dashboard','/events','/gifts','/ledger','/collections','/partner','/reminders','/referrals','/vendors','/helping']);
export function isHostPath(path){
  if(!path)return false;
  return EXACT.has(path)||path.startsWith('/events/')||path.startsWith('/helping/')||path.startsWith('/host/');
}
