import test from 'node:test';
import assert from 'node:assert/strict';
import {detailProblems,securityProblems,firstProblemField,availabilityProblems,registrationErrorField,displayMobile} from '../lib/signup-form.mjs';

const good={fullName:'Asha Rao',username:'asha_r',email:'asha@example.com',mobile:'98765 43210'};

test('details step explains each problem next to its own field',()=>{
  assert.deepEqual(detailProblems(good),{});
  const p=detailProblems({fullName:' ',username:'_x',email:'asha@',mobile:'123'});
  assert.deepEqual(Object.keys(p).sort(),['email','fullName','mobile','username']);
  assert.match(p.email,/name@example\.com/);
  assert.match(p.mobile,/10-digit/);
  assert.equal(firstProblemField(p),'fullName');
  assert.equal(firstProblemField({mobile:'x',email:'y'}),'email');
});

test('mobile numbers people commonly type are accepted and shown back clearly',()=>{
  for(const m of ['9876543210','098765 43210','+91 98765-43210','(98765) 43210'])assert.equal(detailProblems({...good,mobile:m}).mobile,undefined,m);
  assert.equal(displayMobile('098765 43210'),'+91 98765 43210');
  assert.equal(displayMobile('+447700900123'),'+447700900123');
  assert.equal(displayMobile('12'),'');
});

test('security step needs a policy-compliant password and the terms box',()=>{
  assert.deepEqual(securityProblems({password:'Str0ng!pass',confirmPassword:'Str0ng!pass',termsAccepted:true}),{});
  const p=securityProblems({password:'short',termsAccepted:false});
  assert.match(p.password,/requirements/);
  assert.match(p.confirmPassword,/again/);
  assert.match(securityProblems({password:'Str0ng!pass',confirmPassword:'Str0ng!pas',termsAccepted:true}).confirmPassword,/don’t match/);
  assert.match(p.terms,/Terms/);
});

test('taken username or email from the availability check become field problems',()=>{
  assert.deepEqual(availabilityProblems({username:{available:true,message:'ok'},email:{available:false,message:'An account already uses this email.'}}),{email:'An account already uses this email.'});
  assert.deepEqual(availabilityProblems({}),{});
  assert.deepEqual(availabilityProblems(null),{});
  assert.match(availabilityProblems({username:{available:false}}).username,/taken/);
});

test('server registration errors are routed back to the right field',()=>{
  assert.equal(registrationErrorField('Email is already registered'),'email');
  assert.equal(registrationErrorField('Username is already registered. Please choose another username.'),'username');
  assert.equal(registrationErrorField('Mobile number is required in international format'),'mobile');
  assert.equal(registrationErrorField('Password must contain at least 8 characters'),'password');
  assert.equal(registrationErrorField('Please agree to the Terms & Conditions and Privacy Policy.'),'terms');
  assert.equal(registrationErrorField('Request failed (HTTP 500)'),null);
});
