 'use client';
import {beginAction,completeAction} from '../../../../lib/interaction-analytics.mjs';
/* localization:instrumented */
import {localize,localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';


import { useEffect, useState } from 'react';
import { createInvitation, fetchInvitationPng, INVITATION_STYLES, listInvitations } from '../../../../lib/invitation-api.mjs';

type Design = { id: string; name: string; style: string; greeting: string | null; hasImage: boolean };

export default function InvitationStudio({ eventId }: { eventId: string }) {useLocalizationRender();
  const [designs, setDesigns] = useState<Design[]>([]);
  const [name, setName] = useState('My invitation');
  const [style, setStyle] = useState('FLORAL');
  const [greeting, setGreeting] = useState('Join us for a special celebration!');
  const [image, setImage] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [preview, setPreview] = useState('');
  const [selected, setSelected] = useState('');
  useEffect(() => {
    let active = true;
    setError('');
    listInvitations(fetch, eventId).then((rows: Design[]) => { if (active) setDesigns(rows); })
      .catch((e: Error) => { if (active) setError(e.message); });
    return () => { active = false; };
  }, [eventId]);
  useEffect(() => {
    let active = true;
    let url = '';
    setPreview('');
    if (selected) {
      fetchInvitationPng(fetch, eventId, selected).then(blob => {
        if (!active) return;
        url = URL.createObjectURL(blob);
        setPreview(url);
      }).catch((e: Error) => { if (active) setError(e.message); });
    }
    return () => { active = false; if (url) URL.revokeObjectURL(url); };
  }, [eventId, selected]);
  async function save(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      const created = await createInvitation(fetch, eventId, { name, style, greeting, image });
      const rows = await listInvitations(fetch, eventId);
      setDesigns(rows); setSelected(created.id); setNotice('Invitation saved. Preview it below.');
    } catch (err) { setError(err instanceof Error ? err.message : 'Could not save invitation'); }
    finally { setBusy(false); }
  }
  async function download() {
    if (!selected) return;
    const interaction=beginAction('INVITATION_DOWNLOAD',{eventId});
    setBusy(true); setError('');
    try {
      const blob = await fetchInvitationPng(fetch, eventId, selected);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = `gift-invitation-${selected}.png`;
      document.body.appendChild(a); a.click(); a.remove();completeAction(interaction,'BROWSER_READY','PNG_HANDED_TO_BROWSER');
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (err) {completeAction(interaction,'FAILURE','INVITATION_DOWNLOAD_FAILED');setError(err instanceof Error ? err.message : 'Download failed'); }
    finally { setBusy(false); }
  }
  return <section className="hostPanel" aria-labelledby="studio-title" style={{ marginTop: 24 }}>
    <h2 id="studio-title">{localize("ui.1dd3cfdd1798fd13",{defaultValue:"Custom Invitation Studio"})}</h2>
    <p>{localize("ui.49a6ca202351dcd9",{defaultValue:"Create an invitation with a scannable QR. All designs for this event use its existing permanent guest link."})}</p>
    <form onSubmit={e => void save(e)} style={{ display: 'grid', gap: 12, maxWidth: 620 }}>
      <label>{localize("ui.9c645525c72ab92b",{defaultValue:"Design name "})}<input required maxLength={100} value={name} onChange={e => setName(e.target.value)} /></label>
      <label>{localize("ui.dc769cb893f2b247",{defaultValue:"Invitation style "})}<select value={style} onChange={e => setStyle(e.target.value)}>
        {INVITATION_STYLES.map(s => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}
      </select></label>
      <label>{localize("ui.44792314c575f6ec",{defaultValue:"Greeting "})}<textarea maxLength={180} rows={3} value={greeting} onChange={e => setGreeting(e.target.value)} /></label>
      <label>{localize("ui.3bc90813d1a2e771",{defaultValue:"Optional event photo "})}<input type="file" accept="image/png,image/jpeg,image/webp" onChange={e => setImage(e.target.files?.[0] || null)} /></label>
      <button disabled={busy} type="submit">{busy ? localize("ui.23e39291d6135814",{defaultValue:"Saving…"}) : localize("ui.fa83c9de0b442cfb",{defaultValue:"Save invitation design"})}</button>
    </form>
    <h3>{localize("ui.3aab91ef40480c94",{defaultValue:"Saved invitations"})}</h3>
    <label>{localize("ui.b4ab037d956dfde9",{defaultValue:"Choose design to preview "})}<select value={selected} onChange={e => { setSelected(e.target.value); setError(''); }}>
      <option value="">{localize("ui.6e2f2e3f6208d140",{defaultValue:"Select an invitation"})}</option>
      {designs.map(d => <option key={d.id} value={d.id}>{d.name} — {d.style.replaceAll('_', ' ')}</option>)}
    </select></label>
    {preview && <div style={{ marginTop: 16 }}><img src={preview} alt={localize("ui.a04f1e5f5ed14e6e",{defaultValue:"Invitation with event QR code"})} style={{ width: '100%', maxWidth: 450, height: 'auto' }} /></div>}
    <p><button type="button" disabled={!selected || busy} onClick={() => void download()}>{localize("ui.cfe198648de28bd6",{defaultValue:"Download selected invitation PNG"})}</button></p>
    {notice && <p role="status">{localizeText(notice)}</p>}
    {error && <p role="alert" className="hostError">{localizeText(error)}</p>}
    <p style={{ fontSize: 13 }}>{localize("ui.538d6ca0afe64ed0",{defaultValue:"Security: Only trust the official GiftWise domain. A QR image or design alone does not verify the host's identity. Live GiftVerify verification will be introduced separately."})}</p>
  </section>;
}
