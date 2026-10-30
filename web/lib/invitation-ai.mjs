import {hostRequest} from './host-api.mjs';
import {validateSafeLayout} from './invitation-geometry.mjs';
import {validateDesign} from './design.mjs';
const uuid=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
function route(id){if(!uuid.test(String(id)))throw Error('Choose an event before designing with AI.');return `/v1/events/${id}/qr/ai-design`;}
export const aiEventContext=(f,id)=>hostRequest(f,route(id)+'/context');
export function preferencesFromEvent(context){return {visualStyle:context.visualStyle||'ELEGANT',tone:'WARM',language:'English',displayNames:String(context.hostName||'').slice(0,200),publicVenue:String(context.venue||'').slice(0,160),startTime:'',specialMessage:String(context.description||'').slice(0,400)};}
export const aiDesignSettings=(f,id)=>hostRequest(f,route(id));
export async function generateAiInvitation(f,id,input,cookie=''){
 try{return await hostRequest(f,route(id),{method:'POST',payload:input,cookie});}
 catch(e){const message=String(e.message||'');if(message.includes('503'))throw Error('AI design is not configured. Ask the administrator to set OPENAI_API_KEY in the backend.');if(message.includes('429'))throw Error('The hourly AI design limit has been reached. Please try again later.');if(message.includes('422'))throw Error('AI could not design this invitation. Try revising your personal message.');if(message.includes('502'))throw Error('AI is temporarily unavailable. Your invitation has not changed. Please try again.');throw e;}
}
const PALETTES={
 BLUSH:{background:'#fff2f4',accent:'#9b4961',color:'#522c3a',art:'rose-gold-lines'},
 SAGE:{background:'#edf5ef',accent:'#416950',color:'#203e30',art:'sage-leaves'},
 GOLD:{background:'#fff8e9',accent:'#966627',color:'#51391d',art:'golden-arch'},
 LAVENDER:{background:'#f4effa',accent:'#77559a',color:'#46335c',art:'lavender-garden'},
 OCEAN:{background:'#eff6fa',accent:'#37657b',color:'#243d51',art:'silver-geometry'},
 MIDNIGHT:{background:'#142032',accent:'#e4c183',color:'#fff4db',art:'midnight-gold'}
};
const FONTS={SERIF:'Georgia, serif',SANS:'Arial, sans-serif',SCRIPT:'"Brush Script MT", cursive'};
function fit(text,w,h,max){let size=max;while(size>11&&Math.ceil(Math.max(1,text.length)*size*.58/w)*size*1.25>h)size--;return size;}
export function createAiDesign(suggestion){
 const b=suggestion?.blueprint,p=PALETTES[b?.palette],font=FONTS[b?.font];
 if(!p||!font||!['CLASSIC','AIRY','BOLD'].includes(b.layout)||!uuid.test(String(suggestion.eventId)))throw Error('AI returned an unsupported design.');
 for(const [key,max] of [['designName',70],['headline',60],['message',140],['closing',100]])if(typeof b[key]!=='string'||!b[key].trim()||b[key].length>max)throw Error('AI returned invalid invitation text.');
 if(typeof suggestion.eventTitle!=='string'||suggestion.eventTitle.length>200||typeof suggestion.hostName!=='string'||suggestion.hostName.length>200)throw Error('Invalid event details.');
 const align=b.layout==='BOLD'?'left':'center';
 const text=(id,value,y,h,max,weight=400)=>({id,type:'text',text:value,x:50,y,w:500,h,fontSize:fit(value,500,h,max),color:p.color,fontWeight:weight,align,fontFamily:font});
 const date=[suggestion.eventDate,suggestion.startTime].filter(Boolean).join(' · ');
 const design={version:1,width:600,height:800,background:p.background,accent:p.accent,backgroundImage:`/invitation-backgrounds/${p.art}.svg`,backgroundTransform:{x:0,y:0,zoom:1},elements:[
  text('headline',b.headline,50,80,b.layout==='AIRY'?36:44,700),
  text('title',suggestion.eventTitle,145,76,32,700),
  text('host',suggestion.hostName,228,44,21,500),
  text('event-date',date,278,30,18),
  text('venue',suggestion.publicVenue||'',311,43,17),
  text('subtitle',b.message,360,36,17),
  {id:'qr',type:'qr',text:'',x:198,y:415,w:204,h:204,fontSize:20,color:'#222222',fontWeight:400,align:'center'},
  text('cta',b.closing,652,76,21,500),
  {...text('gift','SCAN TO EXPLORE GIFTS',748,25,14,700),fontFamily:'Arial, sans-serif',align:'center'}
 ]};
 if(!validateDesign(design)||!validateSafeLayout(design))throw Error('AI design could not be safely applied.');return design;
}
