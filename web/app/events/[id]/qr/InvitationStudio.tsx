 'use client';

import { useEffect, useState } from 'react';
import { createInvitation, fetchInvitationPng, INVITATION_STYLES, listInvitations } from '../../../../lib/invitation-api.mjs';

type Design = { id: string; name: string; style: string; greeting: string | null; hasImage: boolean };

export default function InvitationStudio({ eventId }: { eventId: string }) {
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
    setBusy(true); setError('');
    try {
      const blob = await fetchInvitationPng(fetch, eventId, selected);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = `gift-invitation-${selected}.png`;
      document.body.appendChild(a); a.click(); a.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (err) { setError(err instanceof Error ? err.message : 'Download failed'); }
    finally { setBusy(false); }
  }
  return <section className="hostPanel" aria-labelledby="studio-title" style={{ marginTop: 24 }}>
    <h2 id="studio-title">Custom Invitation Studio</h2>
    <p>Create an invitation with a scannable QR. All designs for this event use its existing permanent guest link.</p>
    <form onSubmit={e => void save(e)} style={{ display: 'grid', gap: 12, maxWidth: 620 }}>
      <label>Design name <input required maxLength={100} value={name} onChange={e => setName(e.target.value)} /></label>
      <label>Invitation style <select value={style} onChange={e => setStyle(e.target.value)}>
        {INVITATION_STYLES.map(s => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}
      </select></label>
      <label>Greeting <textarea maxLength={180} rows={3} value={greeting} onChange={e => setGreeting(e.target.value)} /></label>
      <label>Optional event photo <input type="file" accept="image/png,image/jpeg,image/webp" onChange={e => setImage(e.target.files?.[0] || null)} /></label>
      <button disabled={busy} type="submit">{busy ? 'Saving…' : 'Save invitation design'}</button>
    </form>
    <h3>Saved invitations</h3>
    <label>Choose design to preview <select value={selected} onChange={e => { setSelected(e.target.value); setError(''); }}>
      <option value="">Select an invitation</option>
      {designs.map(d => <option key={d.id} value={d.id}>{d.name} — {d.style.replaceAll('_', ' ')}</option>)}
    </select></label>
    {preview && <div style={{ marginTop: 16 }}><img src={preview} alt="Invitation with event QR code" style={{ width: '100%', maxWidth: 450, height: 'auto' }} /></div>}
    <p><button type="button" disabled={!selected || busy} onClick={() => void download()}>Download selected invitation PNG</button></p>
    {notice && <p role="status">{notice}</p>}
    {error && <p role="alert" className="hostError">{error}</p>}
    <p style={{ fontSize: 13 }}>Security: Only trust the official Gift Platform domain. A QR image or design alone does not verify the host's identity. Live GiftVerify verification will be introduced separately.</p>
  </section>;
}
