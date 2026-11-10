 'use client';
import Link from 'next/link';
export default function TermsAcceptance({checked,onChange}:{checked:boolean,onChange:(value:boolean)=>void}){return <label style={{display:'flex',gap:10,alignItems:'baseline',gridColumn:'1 / -1'}}><input type="checkbox" required checked={checked} onChange={e=>onChange(e.target.checked)} style={{width:'auto'}}/><span>I agree to the <Link href="/terms" target="_blank">Terms &amp; Conditions</Link> and <Link href="/privacy" target="_blank">Privacy Policy</Link>.</span></label>}
