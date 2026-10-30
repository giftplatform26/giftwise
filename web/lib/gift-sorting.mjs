const ranks={HIGH:0,MEDIUM:1,LOW:2};
export function sortWishlist(gifts, mode='priority', progress={}) {
  if (!['priority','amount-asc','amount-desc','percentage-desc','percentage-asc'].includes(mode)) throw Error('Invalid sort');
  return [...gifts].sort((a,b)=>{
    const p=(ranks[a.priority]??1)-(ranks[b.priority]??1);
    const amount=Number(a.targetAmount)-Number(b.targetAmount);
    const tie=String(a.id).localeCompare(String(b.id));
    // Progress is a display-only total (verified allocations + guest-reported
    // transfers); it is NOT a confirmed financial balance.
    const percentage=(g)=>{
      const target=Number(g.targetAmount)*100;
      const given=Number(progress?.[g.id]?.contributedPaise ?? 0);
      if(!Number.isFinite(target)||target<=0)return 0;
      if(!Number.isFinite(given)||given<=0)return 0;
      return Math.min(100,Math.max(0,(given/target)*100));
    };
    if(mode==='percentage-desc')return percentage(b)-percentage(a)||p||tie;
    if(mode==='percentage-asc')return percentage(a)-percentage(b)||p||tie;
    if(mode==='amount-asc')return amount||p||tie;
    if(mode==='amount-desc')return -amount||p||tie;
    return p||tie;
  });
}
