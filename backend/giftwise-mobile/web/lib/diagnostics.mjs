// Opt-in, memory-only diagnostics. Never records identifiers, contact details or request bodies.
const samples=[];
const flows=new Map();
export function routeLabel(path){
  const clean=String(path).split(/[?#]/)[0];
  return clean.replace(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/gi,':id').replace(/\/\d+(?=\/|$)/g,'/:id');
}
export function recordMetric(name,value,route='/'){
  if(!Number.isFinite(value)||value<0)return;
  samples.push({name,value:Math.round(value),route:routeLabel(route)});
  if(samples.length>200)samples.shift();
}
const allowedStages=new Set(['opened','started','submitted_unverified','confirmed','confirmation_pending','failed','cancelled']);
const allowedFlows=new Set(['guest_contribution','group_guest_contribution','group_member_contribution']);
export function recordFlow(flow,stage){
  if(typeof process==='undefined'||process.env.NEXT_PUBLIC_DIAGNOSTICS_ENABLED!=='true')return;
  if(!allowedFlows.has(flow)||!allowedStages.has(stage))return;
  const key=flow+':'+stage;flows.set(key,(flows.get(key)||0)+1);
}
export function diagnosticsSnapshot(){return {samples:samples.map(x=>({...x})),flows:Object.fromEntries(flows)};}

export function startScreenTiming(route){
  if(typeof performance==='undefined'||process.env.NEXT_PUBLIC_DIAGNOSTICS_ENABLED!=='true')return ()=>{};
  const start=performance.now();
  return ()=>recordMetric('screen_data_ready_ms',performance.now()-start,route);
}
