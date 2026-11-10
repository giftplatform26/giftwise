import {stickerById} from './invitation-stickers.mjs';
import {filterBackgrounds} from './background-gallery.mjs';
export const initialDesign = {
  version: 1, width: 600, height: 800, background: '#fff8ef', accent: '#9e674c', backgroundImage: '', backgroundOverlay: .53, occasionType:'OTHER', backgroundTransform: {x:0,y:0,zoom:1},
  elements: [
    {id:'title',type:'text',text:'Celebrate With Us',x:50,y:115,w:500,h:90,fontSize:43,color:'#653e36',fontWeight:700,align:'center'},
    {id:'subtitle',type:'text',text:'Your presence is our joy',x:70,y:208,w:460,h:55,fontSize:22,color:'#8b6857',fontWeight:400,align:'center'},
    {id:'gift',type:'text',text:'SCAN TO EXPLORE GIFTS',x:80,y:345,w:440,h:45,fontSize:22,color:'#653e36',fontWeight:700,align:'center'},
    {id:'qr',type:'qr',text:'',x:198,y:415,w:204,h:204,fontSize:20,color:'#222222',fontWeight:400,align:'center'},
    {id:'cta',type:'text',text:'Choose a gift or contribute with love',x:65,y:660,w:470,h:70,fontSize:19,color:'#8b6857',fontWeight:500,align:'center'}
  ]
};
export function updateElement(design,id,patch){return {...design,elements:design.elements.map(e=>e.id===id?{...e,...patch}:e)}}
export function addText(design){return {...design,elements:[...design.elements,{id:globalThis.crypto?.randomUUID?.()??String(Date.now()),type:'text',text:'Your message',x:110,y:290,w:380,h:55,fontSize:24,color:'#653e36',fontWeight:500,align:'center'}]}}
export function deleteElement(design,id){if(id==='qr')return design;return {...design,elements:design.elements.filter(e=>e.id!==id)}}
export function validateDesign(design){return design&&design.version===1&&Array.isArray(design.elements)&&design.elements.length<=100&&design.elements.some(e=>e.id==='qr'&&e.type==='qr')&&design.elements.every(e=>typeof e.id==='string'&&typeof e.x==='number'&&Number.isFinite(e.x)&&typeof e.y==='number'&&Number.isFinite(e.y)&&e.w>0&&e.h>0)}

// Occasion templates are deterministic; resetting never modifies event identity or gifts.
export const occasionTemplates = Object.freeze({
  WEDDING:{label:'Wedding',background:'#fff4ee',accent:'#9b5369',title:'Celebrate Our Love',subtitle:'Your blessings mean everything'},
  BIRTHDAY:{label:'Birthday',background:'#fff5d9',accent:'#b55b92',title:'Make a Wish!',subtitle:'Join our birthday celebration'},
  BABY_SHOWER:{label:'Baby shower',background:'#e9f5f1',accent:'#3e837e',title:'A Little One Is Coming',subtitle:'Celebrate this beautiful new beginning'},
  HOUSEWARMING:{label:'Housewarming',background:'#f0eee7',accent:'#826547',title:'Welcome to Our New Home',subtitle:'New keys, new memories'},
  ANNIVERSARY:{label:'Anniversary',background:'#f5eafa',accent:'#80538e',title:'Another Year of Love',subtitle:'Celebrate our journey together'},
  ENGAGEMENT:{label:'Engagement',background:'#fdf0f4',accent:'#a45172',title:'We Are Engaged!',subtitle:'Celebrate our new beginning'},
  GRADUATION:{label:'Graduation',background:'#eef3fa',accent:'#294e86',title:'Graduation Day',subtitle:'Celebrate this achievement'},
  FESTIVAL:{label:'Festival',background:'#fff0df',accent:'#b45d22',title:'Celebrate Together',subtitle:'Wishing joy and togetherness'},
  RETIREMENT:{label:'Retirement',background:'#edf5ed',accent:'#446f50',title:'A New Chapter',subtitle:'Celebrate a wonderful journey'},
  FIRST_BIRTHDAY:{label:'First Birthday',background:'#fef1e8',accent:'#aa6b8a',title:'Turning One!',subtitle:'One year of smiles and love'},
  MILESTONE_BIRTHDAY:{label:'Milestone Birthday',background:'#f5edfa',accent:'#7150a0',title:'A Milestone to Remember',subtitle:'Celebrate this special birthday'},
  REUNION:{label:'Family Reunion',background:'#eaf5f3',accent:'#31776d',title:'Together Again',subtitle:'Making memories with family'},
  THANK_YOU:{label:'Thank You',background:'#f7f3e8',accent:'#8c7544',title:'With Gratitude',subtitle:'Thank you for being part of our journey'},
  OTHER:{label:'Celebration',background:'#fff8ef',accent:'#9e674c',title:'Celebrate With Us',subtitle:'Your presence is our joy'}
});
export function createOccasionDesign(occasion='OTHER',details={}){
 const t=occasionTemplates[occasion]??occasionTemplates.OTHER;
 const title=typeof details.title==='string'&&details.title.trim()?details.title.trim():t.title;
 const d=structuredClone(initialDesign);
 d.occasionType=occasionTemplates[occasion]?occasion:'OTHER';d.background=t.background;d.accent=t.accent;d.backgroundOverlay=0;
 d.backgroundImage=details.backgroundImage||filterBackgrounds(occasion)[0]?.src||'';
 d.elements=d.elements.map(e=>{
   if(e.id==='title')return {...e,text:title,color:t.accent,fontFamily:['BIRTHDAY','FIRST_BIRTHDAY','BABY_SHOWER'].includes(occasion)?'"Comic Sans MS", cursive':'Georgia, serif',fontSize:Math.max(24,Math.min(43,Math.round(1700/Math.max(title.length,1))))};
   if(e.id==='subtitle')return {...e,text:t.subtitle,color:t.accent};
   if(e.id==='gift')return {...e,text:'SCAN TO EXPLORE GIFTS & CONTRIBUTE',color:t.accent};
   if(e.id==='cta')return {...e,text:'Choose a gift or contribute with love',color:t.accent};
   return e;
 });
 const address=details.eventAddress;
 const venue=address?[address.addressLine1,address.city,address.state].filter(Boolean).join(', '):'';
 const lines=[details.eventDate,details.hostName,venue].filter(v=>typeof v==='string'&&v.trim());
 if(lines.length)d.elements.push({id:'event-details',type:'text',text:lines.join(' · '),x:65,y:276,w:470,h:64,fontSize:17,color:t.accent,fontWeight:500,align:'center'});
 const layout=details.layout||(['BIRTHDAY','FIRST_BIRTHDAY','MILESTONE_BIRTHDAY'].includes(occasion)?'celebration':['BABY_SHOWER'].includes(occasion)?'storybook':'classic');
 d.layout=layout;
 const arrangements={
  editorial:{title:{x:60,y:137,w:480,h:125,align:'left',fontFamily:'Garamond, Georgia, serif'},subtitle:{x:60,y:280,w:480,h:65,align:'left'},'event-details':{x:60,y:365,w:480,h:95,align:'left'},gift:{x:60,y:495,w:260,h:50,align:'left',fontSize:18},qr:{x:360,y:540,w:180,h:180},cta:{x:60,y:580,w:250,h:80,align:'left',fontSize:19}},
  storybook:{title:{x:70,y:130,w:460,h:90,fontFamily:'Palatino, "Palatino Linotype", serif'},subtitle:{x:70,y:235,w:460,h:55},'event-details':{x:65,y:310,w:470,h:80},gift:{x:70,y:415,w:460,h:40,fontSize:18},qr:{x:210,y:470,w:180,h:180},cta:{x:70,y:690,w:460,h:55}},
  celebration:{title:{x:60,y:150,w:480,h:100,fontFamily:'Trebuchet MS, sans-serif'},subtitle:{x:70,y:260,w:460,h:50},'event-details':{x:65,y:320,w:470,h:65},gift:{x:65,y:610,w:470,h:45,fontSize:18},qr:{x:210,y:410,w:180,h:180},cta:{x:70,y:680,w:460,h:70}}
 };
 const positions=arrangements[layout]||{};d.elements=d.elements.map(element=>({...element,...positions[element.id]}));
 d.elements.push({id:'eyebrow',type:'text',text:"YOU’RE INVITED",x:layout==='editorial'?60:140,y:layout==='celebration'?105:65,w:layout==='editorial'?380:320,h:28,fontSize:12,color:t.accent,fontWeight:500,align:layout==='editorial'?'left':'center',fontFamily:'Trebuchet MS, sans-serif'});
 const motif={WEDDING:'rose-spray',ENGAGEMENT:'love-doves',BIRTHDAY:'party-hat',FIRST_BIRTHDAY:'cupcake',BABY_SHOWER:'baby-carriage',HOUSEWARMING:'golden-leaf',FESTIVAL:'festive-lantern',ANNIVERSARY:'floral-corner',GRADUATION:'golden-sparkle',OTHER:'butterfly'}[occasion]||'mandala-star';
 const sticker=stickerById(motif);if(sticker){const places=layout==='editorial'?[{x:460,y:24,w:110,h:110},{x:22,y:703,w:88,h:76}]:layout==='storybook'?[{x:27,y:24,w:88,h:88},{x:465,y:690,w:110,h:94}]:[{x:22,y:18,w:96,h:96},{x:470,y:724,w:100,h:62}];places.forEach((position,index)=>d.elements.push({id:`ornament-${index}`,type:'sticker',stickerId:sticker.id,src:sticker.src,text:'',...position,fontSize:20,color:t.accent,fontWeight:400,align:'center'}));}
 return d;
}
export function resetInvitationToDefault(occasion,details){return createOccasionDesign(occasion,details)}
