'use client';
import {useEffect,useState} from 'react';
import {Share2,Copy,Check,Mail,ArrowUpRight} from 'lucide-react';
import {socialLinks} from '../lib/invitation-sharing.mjs';
import './guest-actions.css';
export default function SocialSharing({token,title,social,whatsapp}:{token:string;title:string;social:boolean;whatsapp:boolean}){
 const [url,setUrl]=useState(''),[notice,setNotice]=useState(''),[copied,setCopied]=useState(false);
 useEffect(()=>{setUrl(`${location.origin}/e/${encodeURIComponent(token)}`);setNotice('');setCopied(false)},[token]);
 if(!social&&!whatsapp)return null;
 const links=socialLinks(url,title);
 async function copy(){try{await navigator.clipboard.writeText(url);setCopied(true);setNotice('Invitation link copied. Share it with someone special.')}catch{setNotice('Select and copy the invitation link below.')}}
 async function share(){if(!navigator.share){await copy();return}try{await navigator.share({title,text:`You're invited to ${title}!`,url})}catch(e){if(!(e instanceof Error&&e.name==='AbortError'))setNotice('Use a sharing option below or copy the link.')}}
 return <section className="guest-social" aria-label="Share this invitation"><div className="guest-social-heading"><span className="guest-social-icon"><Share2 size={22}/></span><div><p className="guest-social-kicker">GOOD MOMENTS ARE BETTER TOGETHER</p><h3>Bring your people into the celebration.</h3><p>Send this invitation to friends and family.</p></div></div><div className="guest-social-links">{whatsapp&&<a className="guest-whatsapp" href={links.whatsapp} target="_blank" rel="noopener noreferrer">WhatsApp <ArrowUpRight size={15}/></a>}{social&&<><button type="button" disabled={!url} onClick={()=>void share()}><Share2 size={16}/> Share invitation</button><a href={links.facebook} target="_blank" rel="noopener noreferrer">Facebook <ArrowUpRight size={15}/></a><a href={links.x} target="_blank" rel="noopener noreferrer">X <ArrowUpRight size={15}/></a><a href={links.email}><Mail size={16}/> Email</a><button type="button" disabled={!url} onClick={()=>void copy()}>{copied?<Check size={16}/>:<Copy size={16}/>} {copied?'Copied':'Copy link'}</button></>}</div>{notice&&<p role="status" className="guest-action-notice">{notice}</p>}{notice.startsWith('Select')&&<input aria-label="Invitation link" readOnly value={url} onFocus={e=>e.target.select()}/>}</section>;
}
