'use client';
import {trackedCopy,trackedShare} from '../lib/interaction-analytics.mjs';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

import {useEffect,useState} from 'react';
import {Share2,Copy,Check,Mail,ArrowUpRight} from 'lucide-react';
import {socialLinks} from '../lib/invitation-sharing.mjs';
import './guest-actions.css';
export default function SocialSharing({token,title,social,whatsapp}:{token:string;title:string;social:boolean;whatsapp:boolean}){useLocalizationRender();
 const [url,setUrl]=useState(''),[notice,setNotice]=useState(''),[copied,setCopied]=useState(false);
 useEffect(()=>{setUrl(`${location.origin}/e/${encodeURIComponent(token)}`);setNotice('');setCopied(false)},[token]);
 if(!social&&!whatsapp)return null;
 const links=socialLinks(url,title);
 async function copy(){try{await trackedCopy(url);setCopied(true);setNotice('Invitation link copied. Share it with someone special.')}catch{setNotice('Select and copy the invitation link below.')}}
 async function share(){if(!navigator.share){await copy();return}try{await trackedShare({title,text:`You're invited to ${title}!`,url})}catch(e){if(!(e instanceof Error&&e.name==='AbortError'))setNotice('Use a sharing option below or copy the link.')}}
 return <section className="guest-social" aria-label={localize("ui.005391eade5edd43",{defaultValue:"Share this invitation"})}><div className="guest-social-heading"><span className="guest-social-icon"><Share2 size={22}/></span><div><p className="guest-social-kicker">{localize("ui.745eb160c5b1c279",{defaultValue:"GOOD MOMENTS ARE BETTER TOGETHER"})}</p><h3>{localize("ui.b479181313896290",{defaultValue:"Bring your people into the celebration."})}</h3><p>{localize("ui.5c951b8c5f824149",{defaultValue:"Send this invitation to friends and family."})}</p></div></div><div className="guest-social-links">{whatsapp&&<a className="guest-whatsapp" href={links.whatsapp} target="_blank" rel="noopener noreferrer">{localize("ui.4d7e477e95c6c245",{defaultValue:"WhatsApp "})}<ArrowUpRight size={15}/></a>}{social&&<><button type="button" disabled={!url} onClick={()=>void share()}><Share2 size={16}/> {localize("ui.517e0d761f33fe3b",{defaultValue:" Share invitation"})}</button><a href={links.facebook} target="_blank" rel="noopener noreferrer">{localize("ui.5d9c638e1123d3ef",{defaultValue:"Facebook "})}<ArrowUpRight size={15}/></a><a href={links.x} target="_blank" rel="noopener noreferrer">{localize("ui.368dd7f6a7bd943e",{defaultValue:"X "})}<ArrowUpRight size={15}/></a><a href={links.email}><Mail size={16}/> {localize("ui.6abc2376e054cf4c",{defaultValue:" Email"})}</a><button type="button" disabled={!url} onClick={()=>void copy()}>{copied?<Check size={16}/>:<Copy size={16}/>} {copied?localize("ui.8d525e5f158b9afe",{defaultValue:"Copied"}):localize("ui.dbf362d4f210c780",{defaultValue:"Copy link"})}</button></>}</div>{notice&&<p role="status" className="guest-action-notice">{localizeText(notice)}</p>}{notice.startsWith('Select')&&<input aria-label={localize("ui.88c3be35063553d5",{defaultValue:"Invitation link"})} readOnly value={url} onFocus={e=>e.target.select()}/>}</section>;
}
