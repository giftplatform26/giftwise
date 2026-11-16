'use client';
import {useEffect} from 'react';
import {formatRequirement} from '../lib/field-messages.mjs';
import {localizeText} from '../lib/localization.mjs';

/**
 * App-wide: when an input fails its `pattern`, the browser bubble states the actual requirement
 * (from the field's title or lib/field-messages.mjs) instead of "Please match the requested format".
 * The custom message is cleared as soon as the person edits the field, so normal validation resumes.
 */
export default function FieldFormatMessages(){
 useEffect(()=>{
  const isField=(t:EventTarget|null):t is HTMLInputElement=>t instanceof HTMLInputElement&&t.hasAttribute('pattern');
  const invalid=(e:Event)=>{const field=e.target;if(!isField(field))return;field.setCustomValidity('');
   if(!field.validity.patternMismatch)return;const message=formatRequirement(field);if(message)field.setCustomValidity(localizeText(message));};
  const edited=(e:Event)=>{const field=e.target;if(isField(field))field.setCustomValidity('');};
  // `invalid` does not bubble, so listen in the capture phase.
  document.addEventListener('invalid',invalid,true);document.addEventListener('input',edited,true);
  return()=>{document.removeEventListener('invalid',invalid,true);document.removeEventListener('input',edited,true);};
 },[]);
 return null;
}
