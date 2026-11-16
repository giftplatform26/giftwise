import type {Metadata} from 'next';
import PublicEventClient from './PublicEventClient';
import type {InvitationEvent} from '../../../components/focused/Invitation';
export const dynamic='force-dynamic';
const TOKEN=/^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i;
function backendOrigin(){try{const backend=new URL(process.env.GIFTWISE_BACKEND_ORIGIN||'http://127.0.0.1:8080');return ['http:','https:'].includes(backend.protocol)?backend:null}catch{return null}}
export async function generateMetadata({params}:{params:Promise<{token:string}>}):Promise<Metadata>{
 const {token}=await params;const generic:Metadata={title:'You are invited',description:'Open your celebration invitation.',robots:{index:false,follow:false,noarchive:true},referrer:'no-referrer'};
 if(!TOKEN.test(token))return generic;
 try{
  const origin=new URL(process.env.GIFTWISE_PUBLIC_ORIGIN||'');if(origin.protocol!=='https:'||origin.pathname!=='/'||origin.search||origin.hash||origin.username||origin.password)return generic;
  const backend=backendOrigin();if(!backend)return generic;
  const response=await fetch(new URL(`/api/public/events/${token}/share-preview`,backend),{cache:'no-store',signal:AbortSignal.timeout(2000)});if(!response.ok)return generic;
  const value=await response.json();const description=[value.occasion?.replaceAll('_',' '),value.date].filter(Boolean).join(' · ');
  const image=new URL(`/backend/api/public/events/${token}/share-preview.png`,origin).href;
  return {...generic,title:value.title,description,openGraph:{type:'website',title:value.title,description,images:[{url:image,width:1200,height:630,alt:'Celebration invitation'}]},twitter:{card:'summary_large_image',title:value.title,description,images:[image]}};
 }catch{return generic}
}
/** Server-side first paint: the invitation arrives in the HTML. Any failure or slow backend falls back to the client fetch. */
async function initialEvent(token:string):Promise<InvitationEvent|null>{
 if(!TOKEN.test(token))return null;const backend=backendOrigin();if(!backend)return null;
 try{const response=await fetch(new URL(`/api/public/events/${token}`,backend),{cache:'no-store',signal:AbortSignal.timeout(2500)});if(!response.ok)return null;
  const value=await response.json();return value&&typeof value==='object'&&typeof value.title==='string'&&typeof value.occasionType==='string'&&Array.isArray(value.gifts)?value as InvitationEvent:null}
 catch{return null}
}
export default async function Page({params}:{params:Promise<{token:string}>}){const {token}=await params;return <PublicEventClient initialEvent={await initialEvent(token)}/>}
