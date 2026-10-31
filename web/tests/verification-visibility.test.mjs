import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync,existsSync} from 'node:fs';
import {resolve,dirname} from 'node:path';
import {createRequire} from 'node:module';
import React from 'react';
import {renderToStaticMarkup} from 'react-dom/server';
import ts from 'typescript';
import {signupEmailState} from '../lib/signup-email.mjs';
const require=createRequire(import.meta.url);
const components=resolve(dirname(new URL(import.meta.url).pathname),'../components');
function loader(settings){
 const cache=new Map();
 function load(path){
  if(cache.has(path))return cache.get(path).exports;
  const module={exports:{}};cache.set(path,module);
  const source=readFileSync(path,'utf8');
  const compiled=ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.CommonJS,jsx:ts.JsxEmit.ReactJSX,target:ts.ScriptTarget.ES2022}}).outputText;
  function localRequire(name){
   if(name.endsWith('.css'))return {};
   if(name.endsWith('/signup-email.mjs'))return {signupEmailState};
   if(name.endsWith('/VerificationProvider')&&settings)return {useVerification:()=>settings};
   if(name.startsWith('.')){const base=resolve(dirname(path),name);return load(existsSync(base)?base:base+'.tsx')}
   return require(name);
  }
  new Function('require','module','exports',compiled)(localRequire,module,module.exports);
  return module.exports;
 }
 return load;
}
const widgets=[['AccountEmailVerification',{}],['GroupEmailVerification',{token:'event'}],['SignupEmailVerification',{email:'person@example.test',kind:'HOST',onVerified:()=>{}}],['MobileVerification',{}]];
for(const [name,props]of widgets){
 test(`${name} renders no verification details with platform defaults`,()=>{const component=loader()(resolve(components,name+'.tsx')).default;assert.equal(renderToStaticMarkup(React.createElement(component,props)),'')});
 test(`${name} displays when the administrator enables its channel`,()=>{const component=loader({mobileOtpEnabled:true,emailVerificationEnabled:true})(resolve(components,name+'.tsx')).default;assert.match(renderToStaticMarkup(React.createElement(component,props)),/verification|Verify your email/i)});
}
test('email collection does not prompt verification while bypassed',()=>{const component=loader()(resolve(components,'SignupEmailField.tsx')).default;const html=renderToStaticMarkup(React.createElement(component,{value:'person@example.test',onChange:()=>{}}));assert.match(html,/Email format is valid/);assert.doesNotMatch(html,/Please verify|Email verified|after signup/)});
