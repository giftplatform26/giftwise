import test from 'node:test';
import assert from 'node:assert/strict';
import {createAiDesign,generateAiInvitation,preferencesFromEvent} from '../lib/invitation-ai.mjs';
import {validateSafeLayout} from '../lib/invitation-geometry.mjs';
import {validateDesign} from '../lib/design.mjs';
const id='12345678-1234-1234-1234-123456789012';
function suggestion(){return {eventId:id,eventTitle:'Our Garden Celebration',occasionType:'WEDDING',hostName:'Anu & Rahul',eventDate:'2027-02-14',publicVenue:'Rosewood Gardens',startTime:'6 PM IST',blueprint:{designName:'Garden joy',headline:'Together in bloom',message:'Join us for an evening of love and laughter.',closing:'Your presence is our favourite gift.',palette:'SAGE',layout:'AIRY',font:'SERIF'}};}
test('every AI palette layout and font keeps one protected real-QR slot',()=>{for(const palette of ['BLUSH','SAGE','GOLD','LAVENDER','OCEAN','MIDNIGHT'])for(const layout of ['CLASSIC','AIRY','BOLD'])for(const font of ['SERIF','SANS','SCRIPT']){const data=suggestion();Object.assign(data.blueprint,{palette,layout,font});const design=createAiDesign(data);assert.ok(validateDesign(design));assert.ok(validateSafeLayout(design));assert.equal(design.elements.filter(e=>e.type==='qr').length,1);assert.deepEqual(design.elements.find(e=>e.id==='qr'),{id:'qr',type:'qr',text:'',x:198,y:415,w:204,h:204,fontSize:20,color:'#222222',fontWeight:400,align:'center'});}});
test('saved event facts stay separate from generated wording and input remains unchanged',()=>{const data=suggestion(),before=structuredClone(data);const design=createAiDesign(data);assert.equal(design.elements.find(e=>e.id==='title').text,data.eventTitle);assert.equal(design.elements.find(e=>e.id==='host').text,data.hostName);assert.equal(design.elements.find(e=>e.id==='venue').text,data.publicVenue);assert.deepEqual(data,before);});
test('unsupported palettes fonts and excessive text cannot become editable designs',()=>{for(const field of ['palette','layout','font']){const data=suggestion();data.blueprint[field]='url(javascript:bad)';assert.throws(()=>createAiDesign(data));}const data=suggestion();data.blueprint.headline='x'.repeat(61);assert.throws(()=>createAiDesign(data));});
test('AI cannot supply image URLs QR targets or arbitrary canvas coordinates',()=>{const data=suggestion();Object.assign(data.blueprint,{backgroundImage:'https://evil.example/track',qrUrl:'https://evil.example',x:99999});const design=createAiDesign(data);assert.ok(design.backgroundImage.startsWith('/invitation-backgrounds/'));assert.ok(!JSON.stringify(design).includes('evil.example'));assert.ok(validateSafeLayout(design));});
test('long supported event names and messages stay inside QR-safe layout',()=>{const data=suggestion();data.eventTitle='x'.repeat(200);data.hostName='x'.repeat(200);data.publicVenue='x'.repeat(160);data.blueprint.message='x'.repeat(140);assert.ok(validateSafeLayout(createAiDesign(data)));});
test('generation requires a selected event before network access',async()=>{let calls=0;await assert.rejects(generateAiInvitation(async()=>{calls++},'',{}),/Choose an event/);assert.equal(calls,0);});
test('generation sends a credentialed CSRF-protected request and no API key',async()=>{const requests=[];const fetcher=async(url,init)=>{requests.push({url,init});return {ok:true,status:200,json:async()=>suggestion()}};await generateAiInvitation(fetcher,id,{visualStyle:'FLORAL',tone:'WARM',language:'English'},'XSRF-TOKEN=test');assert.equal(requests.length,2);const {url,init}=requests[1];assert.equal(url,`/backend/api/v1/events/${id}/qr/ai-design`);assert.equal(init.credentials,'include');assert.equal(init.headers['X-XSRF-TOKEN'],'test');assert.ok(!('Authorization' in init.headers));});

test('event context prefills available fields and leaves unknown time empty',()=>{const form=preferencesFromEvent({hostName:'Jiju & Family',venue:'Rose Garden, Kochi',description:'Join us for a special birthday',visualStyle:'PLAYFUL'});assert.equal(form.displayNames,'Jiju & Family');assert.equal(form.publicVenue,'Rose Garden, Kochi');assert.equal(form.specialMessage,'Join us for a special birthday');assert.equal(form.visualStyle,'PLAYFUL');assert.equal(form.startTime,'');});
test('event prefill respects form limits without mutating the source',()=>{const context={hostName:'x'.repeat(200),venue:'y'.repeat(160),description:'z'.repeat(900)};const form=preferencesFromEvent(context);assert.equal(form.displayNames.length,200);assert.equal(form.specialMessage.length,400);assert.equal(context.description.length,900);assert.equal(form.language,'English');});

test('exhausted AI credits tell the host what to fix instead of suggesting retries',async()=>{
 const fetcher=async(url)=>url.endsWith('/csrf')?{ok:true,status:200}:{ok:false,status:402,json:async()=>({code:'AI_CREDITS_EXHAUSTED',detail:'private provider error'})};
 await assert.rejects(generateAiInvitation(fetcher,id,{},'XSRF-TOKEN=test'),/credits have run out/);
});
test('provider throttling is distinct from the application hourly limit',async()=>{
 const fetcher=async(url)=>url.endsWith('/csrf')?{ok:true,status:200}:{ok:false,status:503,json:async()=>({code:'AI_PROVIDER_RATE_LIMITED'})};
 await assert.rejects(generateAiInvitation(fetcher,id,{},'XSRF-TOKEN=test'),/provider is busy/);
});
test('AI provider errors never display arbitrary upstream detail',async()=>{
 const fetcher=async(url)=>url.endsWith('/csrf')?{ok:true,status:200}:{ok:false,status:502,json:async()=>({code:'AI_PROVIDER_UNAVAILABLE',detail:'PRIVATE_PROVIDER_MESSAGE'})};
 try{await generateAiInvitation(fetcher,id,{},'XSRF-TOKEN=test');assert.fail('Expected failure')}catch(error){assert.doesNotMatch(error.message,/PRIVATE_PROVIDER_MESSAGE/);assert.match(error.message,/invitation has not changed/)}
});
