'use client';
import {useEffect,useState} from 'react';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {hostRequest} from '../../lib/host-api.mjs';
import '../../components/gift-books.css';
import '../../components/growth-pages.css';

type Mine={code:string;link:string;reward:string;friends:{name:string;status:string;joinedAt:string}[];qualified:number;rewarded:number};
const STATUS:Record<string,string>={ATTRIBUTED:'Joined — waiting for their first invitation',QUALIFIED:'Published! Reward on its way',REWARDED:'Reward given',CANCELLED:'Not eligible'};

export default function Referrals(){useLocalizationRender();
 const [m,setM]=useState<Mine|null>(null),[error,setError]=useState(''),[notice,setNotice]=useState('');
 useEffect(()=>{hostRequest(fetch,'/v1/me/referrals').then(v=>setM(v as Mine)).catch(e=>setError(e instanceof Error?e.message:'Could not load'))},[]);
 async function copy(){if(!m)return;try{await navigator.clipboard.writeText(m.link);setNotice('Link copied.')}catch{setNotice('Select the link and copy it.')}}
 async function share(){if(!m)return;const text=localizeText('I made our invitation on GiftWise and loved it. Make yours here:');
  if(navigator.share){try{await navigator.share({title:'GiftWise',text,url:m.link});return}catch{return}}window.open(`https://wa.me/?text=${encodeURIComponent(`${text} ${m.link}`)}`,'_blank','noopener')}
 return <main className="focusPage gbPage">
  <header className="focusPageHeading"><div><p className="focusEyebrow">{localizeText('Share the joy')}</p><h1>{localizeText('Invite friends')}</h1><p>{m?m.reward:''}</p></div></header>
  {error&&<p className="focusError" role="alert">{localizeText(error)}</p>}
  {notice&&<p className="gbNotice" role="status">{localizeText(notice)}</p>}
  {m&&<><section className="rfHero"><p>{localizeText('Your link')}</p><div className="lwCopy"><input readOnly value={m.link} onFocus={e=>e.currentTarget.select()} aria-label={localizeText('Your referral link')}/>
    <button type="button" className="focusSecondary" onClick={()=>void copy()}>{localizeText('Copy')}</button><button type="button" className="focusPrimary" onClick={()=>void share()}>{localizeText('Share')}</button></div>
    <ol className="rfSteps"><li>{localizeText('A friend signs up with your link')}</li><li>{localizeText('They publish their first invitation')}</li><li>{localizeText('You get your reward')}</li></ol></section>
   <section className="gbStats gbStats--three"><div><span>{localizeText('Friends joined')}</span><strong>{m.friends.length}</strong></div><div><span>{localizeText('Published')}</span><strong>{m.qualified}</strong></div><div><span>{localizeText('Rewards')}</span><strong>{m.rewarded}</strong></div></section>
   {m.friends.length>0&&<ul className="gbRows">{m.friends.map((f,i)=><li key={i}><div><span className="gbWho"><strong>{f.name}</strong><small>{new Intl.DateTimeFormat(undefined,{dateStyle:'medium'}).format(new Date(f.joinedAt))}</small></span><span className={`rfStatus rfStatus--${f.status.toLowerCase()}`}>{localizeText(STATUS[f.status]??f.status)}</span></div></li>)}</ul>}</>}
 </main>;
}
