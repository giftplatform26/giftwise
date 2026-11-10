import test from 'node:test';import assert from 'node:assert/strict';import {validateRegistration,validateEvent,hostApi,bootstrapCsrf,OCCASIONS} from '../lib/host-api.mjs';
test('supported occasions match backend enum',()=>assert.deepEqual(OCCASIONS,['WEDDING','BIRTHDAY','BABY_SHOWER','HOUSEWARMING','ANNIVERSARY','FAREWELL','RETIREMENT','REUNION','OTHER']));
test('registration validates username',()=>assert.throws(()=>validateRegistration({username:'A',email:'a@b.co',password:'Abcdef12345!',termsAccepted:true})));
test('registration validates email',()=>assert.throws(()=>validateRegistration({username:'validuser',email:'wrong',password:'Abcdef12345!',termsAccepted:true})));
test('registration validates password',()=>assert.throws(()=>validateRegistration({username:'validuser',email:'a@b.co',password:'short'})));
test('registration accepts valid input',()=>assert.equal(validateRegistration({fullName:'Anjali Nair',username:'validuser',email:'a@b.co',mobile:'+919876543210',password:'Abcdef12345!',termsAccepted:true}).username,'validuser'));
test('event title required',()=>assert.throws(()=>validateEvent({title:' ',occasionType:'BIRTHDAY'})));
test('event occasion required',()=>assert.throws(()=>validateEvent({title:'Test',occasionType:'UNKNOWN'})));
test('event trims title and nulls empty date',()=>assert.deepEqual(validateEvent({title:' Test ',occasionType:'OTHER',addressLine1:'10 Main Street',city:'Chennai',state:'Tamil Nadu',postalCode:'600001',country:'India',contactName:'Host',contactPhone:'+919876543210'}),{deliverySameAsEvent:true,deliveryAddress:null,title:'Test',description:'',occasionType:'OTHER',eventDate:null,addressLine1:'10 Main Street',city:'Chennai',state:'Tamil Nadu',postalCode:'600001',country:'India',contactName:'Host',contactPhone:'+919876543210',contactEmail:null}));
test('GET me sends cookies',async()=>{let args;await hostApi.me(async(...a)=>{args=a;return {ok:true,status:200,json:async()=>({username:'u'})};});assert.equal(args[0],'/backend/api/auth/me');assert.equal(args[1].credentials,'include');});
test('bootstrap GET sends credentials',async()=>{let args;await bootstrapCsrf(async(...a)=>{args=a;return {ok:true,status:204};});assert.equal(args[0],'/backend/api/auth/csrf');assert.equal(args[1].credentials,'include');});
test('write refuses missing CSRF',async()=>{let calls=0;await assert.rejects(()=>hostApi.createEvent(async()=>{calls++;return {ok:true,status:204};},{title:'Party',occasionType:'OTHER',addressLine1:'10 Main Street',city:'Chennai',state:'Tamil Nadu',postalCode:'600001',country:'India',contactName:'Host',contactPhone:'+919876543210'}),/CSRF cookie missing/);assert.equal(calls,1);});
test('401 is not empty event list',async()=>{await assert.rejects(()=>hostApi.events(async()=>({ok:false,status:401})),/Please sign in/);});
test('login sends correct fields with token',async()=>{const original=globalThis.document;globalThis.document={cookie:'XSRF-TOKEN=abc'};let calls=[];try{await hostApi.login(async(path,opts)=>{calls.push([path,opts]);return {ok:true,status:path.endsWith('/csrf')?204:200,json:async()=>({username:'host'})};},'host','password');assert.equal(calls[1][0],'/backend/api/auth/login');assert.deepEqual(JSON.parse(calls[1][1].body),{identifier:'host',password:'password'});assert.equal(calls[1][1].headers['X-XSRF-TOKEN'],'abc');}finally{globalThis.document=original;}});

test('event requires address and contact',()=>assert.throws(()=>validateEvent({title:'Party',occasionType:'OTHER'}),/required/));

test('registration requires a full name',()=>assert.throws(()=>validateRegistration({username:'validuser',email:'a@b.co',password:'Abcdef12345!',termsAccepted:true}),/Enter your full name/));
test('registration trims full name',()=>assert.equal(validateRegistration({fullName:'  Anjali Nair  ',username:'validuser',email:'a@b.co',mobile:'+919876543210',password:'Abcdef12345!',termsAccepted:true}).fullName,'Anjali Nair'));

test('registration requires mobile number',()=>assert.throws(()=>validateRegistration({fullName:'Anjali Nair',username:'validuser',email:'a@b.co',password:'Abcdef12345!',termsAccepted:true}),/mobile number/i));

test('registration requires explicit consent',()=>assert.throws(()=>validateRegistration({fullName:'Anjali Nair',username:'validuser',email:'a@b.co',mobile:'+919876543210',password:'Abcdef12345!'}),/agree/));

test('event rejects impossible calendar dates',()=>{const base={title:'Party',occasionType:'BIRTHDAY',addressLine1:'Road',city:'City',state:'State',postalCode:'560001',country:'India',contactName:'Host',contactPhone:'+919876543210'};assert.throws(()=>validateEvent({...base,eventDate:'2026-02-30'}),/valid event date/);assert.equal(validateEvent({...base,eventDate:'2028-02-29'}).eventDate,'2028-02-29')});
