 'use client';
import {passwordRules,usernameRules} from '../lib/credential-policy.mjs';
import {localizeText} from '../lib/localization.mjs';
export default function CredentialRequirements({kind,value,id}:{kind:'username'|'password',value:string,id:string}) {
 const rules=(kind==='username'?usernameRules(value):passwordRules(value)) as [string,boolean][];
 const remaining=rules.filter(([,valid])=>!valid).length;
 return <div id={id} style={{fontSize:'.875rem',margin:'8px 0'}}><strong>{localizeText(kind==='username'?'Username must:':'Password must contain:')}</strong><ul style={{listStyle:'none',padding:0,margin:'6px 0'}}>{rules.map(([label,valid]:[string,boolean])=><li key={label} style={{color:valid?'#166534':'#b91c1c',display:'flex',gap:6,alignItems:'baseline',margin:'4px 0'}}><span aria-hidden="true">{valid?'✓':'✕'} </span><span style={{position:"absolute",width:1,height:1,padding:0,overflow:"hidden",clip:"rect(0,0,0,0)",whiteSpace:"nowrap"}}>{localizeText(valid?'Requirement met: ':'Still needed: ')}</span>{localizeText(label)}</li>)}</ul><p role="status" aria-live="polite" style={{margin:"4px 0",color:remaining?"#b91c1c":"#166534"}}>{value?(remaining?localizeText(`${remaining} requirements still needed.`):localizeText("All requirements met.")):localizeText("Enter your "+kind+" to check the requirements.")}</p></div>;
}
