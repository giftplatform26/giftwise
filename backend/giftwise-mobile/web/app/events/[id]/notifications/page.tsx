'use client';
import {activeLocale} from '../../../../lib/localization.mjs';

/* localization:instrumented */
import {localize,localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';


import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import GroupCollectionReport from '../../../../components/GroupCollectionReport';
import Link from 'next/link';
import {useExperience} from '../../../../components/ExperienceProvider';
import {ActivityRow} from '../../../../components/focused/ResponsiveTable';
type Activity={id:string;guestName:string;giftName:string|null;method:string;amountPaise:number;verificationStatus:string;createdAt:string;giftPaise:number;walletPaise:number};

type History = {
  id: string;
  notification_kind: string;
  recipient_kind: string;
  channel: string;
  payment_evidence: string;
  status: string;
  created_at: string;
};

export default function NotificationsPage() {useLocalizationRender();
  const {mode}=useExperience();const focused=mode==='FOCUSED';
  const params = useParams();
  const id = String(params.id ?? '');
  const [history, setHistory] = useState<History[]>([]);
  const [activity, setActivity] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const endpoint =
          `/backend/api/v1/events/${encodeURIComponent(id)}/thank-you/history`;

        const response = await fetch(endpoint, {
          credentials: 'include',
          cache: 'no-store',
        });

        if (!response.ok) {
          throw new Error(
            `Could not load notification history (${response.status})`
          );
        }

        const data: unknown = await response.json();

        if (!Array.isArray(data)) {
          throw new Error('Unexpected notification history response');
        }

        if (active) setHistory(data as History[]);
        const activityResponse = await fetch(
          `/backend/api/v1/events/${encodeURIComponent(id)}/contributions?limit=100`,
          {credentials:'include',cache:'no-store'}
        );
        if (!activityResponse.ok) throw new Error(`Could not load contribution activity (${activityResponse.status})`);
        const report:unknown=await activityResponse.json();
        if (!report || typeof report!=='object' || !('entries' in report) || !Array.isArray(report.entries))
          throw new Error('Unexpected contribution activity response');
        if (active) setActivity(report.entries as Activity[]);
      } catch (err) {
        if (active) {
          setError(err instanceof Error ? err.message : 'Unable to load history');
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    if (id) void load();
    else {
      setError('Missing event ID');
      setLoading(false);
    }

    return () => { active = false; };
  }, [id]);

  return (
    <main style={{
      maxWidth: 1100,
      margin: '0 auto',
      padding: '28px 20px'
    }}>
      <Link href="/events">{localize("ui.34bd233410c2ceb9",{defaultValue:"← Back to My Events"})}</Link>

      <GroupCollectionReport eventId={id} notifications/><h1>{focused?localize("ui.783222ed531ca517",{defaultValue:"Activity & notifications"}):localize("ui.61d4e77e9eb4e3c0",{defaultValue:"Notification History"})}</h1>
      <p>
        {localize("ui.747151be868ef32b",{defaultValue:"Review acknowledgments and notification records for this celebration. Queued notifications are not proof of external delivery. Guest-reported UPI and bank transfers are not independently verified payments."})}</p>

      <p>
        <Link href={`/events/${encodeURIComponent(id)}/thank-you`}>
          {localize("ui.fc8d555ac7c4f591",{defaultValue:"Open Thank-you Card Studio →"})}</Link>
      </p>

      <h2>{localize("ui.8d0b5abe7179e452",{defaultValue:"Contribution activity"})}</h2>
      <p>{localize("ui.db671cb7e8a1d542",{defaultValue:"These are in-app activity previews, not proof of email/SMS delivery or payment verification."})}</p>
      <p><Link href={`/events/${encodeURIComponent(id)}/contributions`}>{localize("ui.0b55bbbd0968fe00",{defaultValue:"View full contribution report →"})}</Link></p>
      {!loading && !error && activity.length===0 && <p>{localize("ui.8495cde7ded081d2",{defaultValue:"No contribution activity yet."})}</p>}
      {focused&&!loading&&<div className="focusMobileRows">{activity.map(a=><ActivityRow key={a.id} title={a.guestName} amount={'₹'+(a.amountPaise/100).toLocaleString(activeLocale("en-IN"))} status={a.verificationStatus==='VERIFIED'?'Payment verified':a.verificationStatus==='VERIFICATION_PENDING'?'Verification pending':a.verificationStatus.toLowerCase().replaceAll('_',' ')}><p>{a.giftName||localize("ui.0ebda8b7a27b9d41",{defaultValue:"Celebration wallet"})} · {a.method}</p><p>{new Date(a.createdAt).toLocaleString()}</p></ActivityRow>)}</div>}{!loading && !error && activity.length>0 && <div className={focused?'focusDesktopTable':undefined} style={{overflowX:'auto'}}><table style={{width:'100%'}}>
        <thead><tr>{['Date','Guest','Gift / wallet','Method','Amount','Status'].map(t=><th key={t} style={{textAlign:'left'}}>{t}</th>)}</tr></thead>
        <tbody>{activity.map(a=><tr key={a.id}><td>{new Date(a.createdAt).toLocaleString()}</td><td>{a.guestName}</td><td>{a.giftName||localize("ui.0ebda8b7a27b9d41",{defaultValue:"Celebration wallet"})}{a.giftPaise>0&&a.walletPaise>0?localize("ui.bd5861888693e40d",{defaultValue:" + wallet excess"}):''}</td><td>{a.method}</td><td>{'₹'+(a.amountPaise/100).toLocaleString(activeLocale("en-IN"))}</td><td>{a.verificationStatus.replaceAll('_',' ')}</td></tr>)}</tbody>
      </table></div>}
      <details open={focused?undefined:true}><summary>{localize("ui.360fac9e19c804d3",{defaultValue:"Notification delivery records"})}</summary>
      {loading && <p role="status">{localize("ui.eae12e0d3bab022d",{defaultValue:"Loading notifications…"})}</p>}

      {error && <p role="alert">{localizeText(error)}</p>}

      {!loading && !error && history.length === 0 && (
        <p>{localize("ui.eb09818fa808861c",{defaultValue:"No notifications recorded for this event yet."})}</p>
      )}

      {!loading && !error && history.length > 0 && (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                {['Date', 'Recipient', 'Channel', 'Evidence', 'Status'].map(x => (
                  <th key={x} scope="col" style={{
                    textAlign: 'left',
                    padding: 10,
                    borderBottom: '1px solid #ddd'
                  }}>
                    {x}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {history.map(h => (
                <tr key={h.id}>
                  <td style={{ padding: 10 }}>
                    {h.created_at
                      ? new Date(h.created_at).toLocaleString()
                      : '—'}
                  </td>
                  <td>{h.recipient_kind}</td>
                  <td>{h.channel}</td>
                  <td>{h.payment_evidence}</td>
                  <td>{localizeText(h.status)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </details></main>
  );
}
