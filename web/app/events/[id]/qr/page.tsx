// batch-3d4d2d-compact
'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import {useExperience} from '../../../../components/ExperienceProvider';
import SharedVisualInvitationEditor from '../../../../components/InvitationEditor';
import { hostApi, fetchEventQrPng } from '../../../../lib/host-api.mjs';

type QrDetails = {
  eventId: string;
  publicToken: string;
  theme: string;
  url: string;
};

const THEMES = [
  'MODERN_MINIMAL',
  'ELEGANT_GOLD',
  'BIRTHDAY_CELEBRATION',
  'BABY_SHOWER',
  'ANNIVERSARY',
  'HOUSEWARMING'
];

export default function EventQrPage() {
  const {mode}=useExperience();const focused=mode==='FOCUSED';
  const params = useParams();
  const eventId = String(params.id);

  const [details, setDetails] = useState<QrDetails | null>(null);
  const [imageUrl, setImageUrl] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let active = true;
    let objectUrl = '';

    async function load() {
      try {
        const data = await hostApi.qrDetails(fetch, eventId);
        const blob = await fetchEventQrPng(fetch, eventId);

        if (!active) return;

        objectUrl = URL.createObjectURL(blob);
        setDetails(data);
        setImageUrl(objectUrl);
      } catch (e) {
        if (active) {
          setError(e instanceof Error ? e.message : 'Unable to load QR');
        }
      }
    }

    void load();

    return () => {
      active = false;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [eventId]);

  async function updateTheme(theme: string) {
    setBusy(true);
    setError('');

    try {
      const updated = await hostApi.changeQrTheme(fetch, eventId, theme);
      setDetails(updated);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to change theme');
    } finally {
      setBusy(false);
    }
  }

  async function downloadQr() {
    setBusy(true);
    setError('');

    try {
      const blob = await fetchEventQrPng(fetch, eventId);
      const url = URL.createObjectURL(blob);

      try {
        const link = document.createElement('a');
        link.href = url;
        link.download = `gift-platform-qr-${eventId}.png`;
        document.body.appendChild(link);
        link.click();
        link.remove();
      } finally {
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Download failed');
    } finally {
      setBusy(false);
    }
  }

  async function copyLink() {
    if (!details) return;

    try {
      await navigator.clipboard.writeText(details.url);
      alert('Invitation link copied!');
    } catch {
      setError('Could not copy invitation link');
    }
  }

  return (
    <main className="hostShell invitationStudioPage">
      <div className="hostWide">
        {!focused&&<nav aria-label="Event navigation" style={{marginBottom:22}}>
          <Link
            href="/events"
            style={{
              display:'inline-flex',
              alignItems:'center',
              gap:10,
              padding:'12px 18px',
              borderRadius:12,
              background:'#fff',
              border:'1px solid #e7d8d1',
              boxShadow:'0 4px 16px rgba(65,40,35,0.07)',
              color:'#795047',
              fontSize:14,
              fontWeight:700,
              textDecoration:'none'
            }}
          >
            <span aria-hidden="true" style={{fontSize:20}}>←</span>
            Back to My Events
          </Link>
        </nav>}

        <section className="studioCardSection" aria-label="Gift Invitation Studio">
          <SharedVisualInvitationEditor eventId={eventId}>
<section className="hostPanel compactQrPanel" aria-label="QR Invitation">
          

          <h1>QR Invitation</h1>
          <p>Manage and share your celebration invitation.</p>

          {error && <p role="alert" className="hostError">{error}</p>}

          {imageUrl && (
            <img
              src={imageUrl}
              alt="Scannable event invitation QR code"
              width={280}
              height={280}
            />
          )}

          {details && (
            <>
              <p>
                <strong>Invitation URL</strong>
              </p>

              <p style={{ overflowWrap: 'anywhere' }}>
                {details.url}
              </p>

              <button type="button" onClick={copyLink}>
                Copy invitation link
              </button>

              <button
                type="button"
                disabled={busy}
                onClick={() => void downloadQr()}
              >
                Download QR PNG
              </button>

              <p>
                <strong>QR Theme</strong>
              </p>

              <select
                aria-label="QR theme"
                value={details.theme}
                disabled={busy}
                onChange={e => void updateTheme(e.target.value)}
              >
                {THEMES.map(theme => (
                  <option key={theme} value={theme}>
                    {theme.replaceAll('_', ' ')}
                  </option>
                ))}
              </select>

              <p>
                Theme selection is saved. Customized invitation artwork
                will be available in the next batch.
              </p>
            </>
          )}
        </section>
          </SharedVisualInvitationEditor>
        </section>

      </div>
    </main>
  );
}