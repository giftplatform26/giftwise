import type {Metadata} from 'next';
import PublicEventClient from './PublicEventClient';
export const dynamic='force-dynamic';
export async function generateMetadata({params}:{params:Promise<{token:string}>}):Promise<Metadata>{
 const {token}=await params;const generic:Metadata={title:'You are invited',description:'Open your celebration invitation.',robots:{index:false,follow:false,noarchive:true},referrer:'no-referrer'};
 if(!/^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i.test(token))return generic;
 try{
  const origin=new URL(process.env.GIFTWISE_PUBLIC_ORIGIN||'');if(origin.protocol!=='https:'||origin.pathname!=='/'||origin.search||origin.hash||origin.username||origin.password)return generic;
  const backend=new URL(process.env.GIFTWISE_BACKEND_ORIGIN||'http://127.0.0.1:8080');if(!['http:','https:'].includes(backend.protocol))return generic;
  const response=await fetch(new URL(`/api/public/events/${token}/share-preview`,backend),{cache:'no-store',signal:AbortSignal.timeout(2000)});if(!response.ok)return generic;
  const value=await response.json();const description=[value.occasion?.replaceAll('_',' '),value.date].filter(Boolean).join(' · ');
  const image=new URL(`/backend/api/public/events/${token}/share-preview.png`,origin).href;
  return {...generic,title:value.title,description,openGraph:{type:'website',title:value.title,description,images:[{url:image,width:1200,height:630,alt:'Celebration invitation'}]},twitter:{card:'summary_large_image',title:value.title,description,images:[image]}};
 }catch{return generic}
}
export default function Page(){return <PublicEventClient/>}
