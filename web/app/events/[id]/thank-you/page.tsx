'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';

import KeepsakeSharing from '../../../../components/KeepsakeSharing';
import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import {useExperience} from '../../../../components/ExperienceProvider';

type Settings = {
  hostName: string; emailSubject: string; emailBody: string; smsBody: string;
  cardTemplate: string; autoSend: boolean; emailEnabled: boolean;
  smsEnabled: boolean; whatsappEnabled: boolean;
};
const defaults: Settings = {
  hostName: '', emailSubject: 'Thank you for celebrating with us',
  emailBody: 'Dear {{guestName}},\nThank you for celebrating {{eventName}} with us.\nWith love, {{hostName}}',
  smsBody: 'Thank you for celebrating with us!', cardTemplate: 'CLASSIC',
  autoSend: true, emailEnabled: true, smsEnabled: false, whatsappEnabled: false,
};
function xsrfToken(): string {
  if (typeof document === 'undefined') return '';
  const found = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
  return found ? decodeURIComponent(found.slice('XSRF-TOKEN='.length)) : '';
}
export default function ThankYouPage() {useLocalizationRender();
 const {mode}=useExperience();const focused=mode==='FOCUSED';
  const params = useParams();
  const id = String(params.id ?? '');
  const [settings, setSettings] = useState<Settings>(defaults);
  const [notice, setNotice] = useState('');
  const [saving, setSaving] = useState(false);
  const endpoint = `/backend/api/v1/events/${encodeURIComponent(id)}/thank-you`;
  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const response = await fetch(endpoint, { credentials: 'include', cache: 'no-store' });
        if (!response.ok) throw new Error(`Settings request failed (${response.status})`);
        const body = await response.json() as Settings;
        if (active) setSettings(body);
      } catch (error) { if (active) setNotice(String(error)); }
    })();
    return () => { active = false; };
  }, [endpoint]);
  const set = <K extends keyof Settings>(key: K, value: Settings[K]) =>
    setSettings(prev => ({ ...prev, [key]: value }));
  async function save() {
    setSaving(true); setNotice('');
    try {
      const token = xsrfToken();
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      if (token) headers['X-XSRF-TOKEN'] = token;
      const response = await fetch(endpoint, {
        method: 'PUT', credentials: 'include', headers, body: JSON.stringify(settings),
      });
      if (!response.ok) throw new Error(`Save failed (${response.status}). Check login and CSRF token.`);
      setSettings(await response.json() as Settings);
      setNotice('Saved successfully. External messages are not sent by this editor.');
    } catch (error) { setNotice(String(error)); }
    finally { setSaving(false); }
  }
  const preview = settings.emailBody
    .replaceAll('{{guestName}}', 'Sample Guest')
    .replaceAll('{{eventName}}', 'Sample Celebration')
    .replaceAll('{{hostName}}', settings.hostName)
    .replaceAll('{{giftName}}', 'A thoughtful gift')
    .replaceAll('{{amount}}', '₹2,000');
  const field: React.CSSProperties = { width: '100%', padding: 10, marginTop: 5, marginBottom: 14, border: '1px solid #bbb', borderRadius: 7 };
  const label: React.CSSProperties = { display: 'block', fontWeight: 600 };
  return <main style={{ maxWidth: 1180, margin: '0 auto', padding: '28px 20px', color: '#282828' }}><KeepsakeSharing eventId={id}/>
    <Link href="/events">{localize("ui.5442b1b0dcc591c5",{defaultValue:"← Events"})}</Link>
    <h1 style={{ fontSize: 30, marginTop: 18 }}>{localize("ui.ae2fc37d79fa3fa3",{defaultValue:"Thank-you card studio"})}</h1>
    <p>{localize("ui.1af0d87ed3cee82e",{defaultValue:"Personalize the greeting for your event. Reported UPI and bank transfers are not verified payments."})}</p>
    {notice && <p role="status" style={{ padding: 10, background: '#fff1dc' }}>{localizeText(notice)}</p>}
    <div className={focused?'focusThankyouGrid':undefined} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 360px), 1fr))', gap: 30 }}>
      <details open={focused?undefined:true}><summary>{localize("ui.e2f43b76eece0e47",{defaultValue:"Personalise your thank-you card"})}</summary>
        <label style={label}>{localize("ui.c34b360f7db1ee14",{defaultValue:"Card signature / host display name"})}<input style={field} maxLength={200} value={settings.hostName} onChange={e => set('hostName', e.target.value)} />
        </label>
        <label style={label}>{localize("ui.848f0ca2c125e4dc",{defaultValue:"Email subject"})}<input style={field} maxLength={200} value={settings.emailSubject} onChange={e => set('emailSubject', e.target.value)} />
        </label>
        <label style={label}>{localize("ui.eb653a6eee22afc1",{defaultValue:"Email message"})}<textarea style={field} rows={7} maxLength={10000} value={settings.emailBody} onChange={e => set('emailBody', e.target.value)} />
        </label>
        <label style={label}>{localize("ui.d5f85d6b6ed53a00",{defaultValue:"SMS message"})}<textarea style={field} rows={3} maxLength={1000} value={settings.smsBody} onChange={e => set('smsBody', e.target.value)} />
        </label>
        <label style={label}>{localize("ui.ba3776ceea05bb99",{defaultValue:"Card theme"})}<select style={field} value={settings.cardTemplate} onChange={e => set('cardTemplate', e.target.value)}>
            <option value="CLASSIC">{localize("ui.ae25d6481fc1741f",{defaultValue:"Classic"})}</option><option value="FLORAL">{localize("ui.b579d00d3939d52a",{defaultValue:"Floral"})}</option><option value="MODERN">{localize("ui.eef6c7f6ddd4067e",{defaultValue:"Modern"})}</option>
          </select>
        </label>
        <label style={{ display: 'block', marginBottom: 10 }}>
          <input type="checkbox" checked={settings.autoSend} onChange={e => set('autoSend', e.target.checked)} /> {localize("ui.f0b369d7e0055fd0",{defaultValue:" Queue acknowledgments automatically"})}</label>
        <label style={{ display: 'block', marginBottom: 10 }}>
          <input type="checkbox" checked={settings.emailEnabled} onChange={e => set('emailEnabled', e.target.checked)} /> {localize("ui.700c8ae481cdde15",{defaultValue:" Queue guest email acknowledgments"})}</label>
        <p style={{ fontSize: 13 }}>{localize("ui.f5b45b4cfb797c89",{defaultValue:"Live email, SMS and WhatsApp delivery are not configured. Queued records are not proof of delivery."})}</p>
        <button disabled={saving} onClick={() => void save()} style={{ padding: '12px 20px', borderRadius: 8, background: '#303c66', color: '#fff', cursor: 'pointer' }}>
          {saving ? localize("ui.23e39291d6135814",{defaultValue:"Saving…"}) : localize("ui.039de9dce407cb01",{defaultValue:"Save design"})}
        </button>
      </details>
      <section aria-label={localize("ui.6436d3154ddefb4a",{defaultValue:"Card preview"})}>
        <div style={{ padding: 34, borderRadius: 16, textAlign: 'center', whiteSpace: 'pre-wrap', overflowWrap: 'anywhere', background: settings.cardTemplate === 'FLORAL' ? '#faeaf1' : settings.cardTemplate === 'MODERN' ? '#e8f0fc' : '#faf1df' }}>
          <div style={{ fontSize: 36 }}>♡</div><h2>{localize("ui.cdd2fb2d19c934d9",{defaultValue:"Thank You!"})}</h2><p>{preview}</p><strong>{settings.hostName}</strong>
        </div>
        <p style={{ fontSize: 13 }}>{localize("ui.c1f5e6837e4b0956",{defaultValue:"Preview placeholders: "})}{localize("ui.9651972cc101663f",{defaultValue:"{{guestName}}"})}, {localize("ui.108542121e046ff3",{defaultValue:"{{eventName}}"})}, {localize("ui.4130848b6383adb5",{defaultValue:"{{hostName}}"})}, {localize("ui.0d801df673aefba5",{defaultValue:"{{giftName}}"})}, {localize("ui.e683b2f370060446",{defaultValue:"{{amount}}"})}.</p>
      </section>
    </div>
  </main>;
}
