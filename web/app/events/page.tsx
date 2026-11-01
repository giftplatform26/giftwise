'use client';
import DeliveryAddressFields,{emptyDeliveryAddress} from '../../components/DeliveryAddressFields';
import {useExperience} from '../../components/ExperienceProvider';
import FocusedEvents from '../../components/focused/EventList';
import {useVerification} from '../../components/VerificationProvider';
import {useEffect,useState} from 'react';import Link from 'next/link';import {useRouter} from 'next/navigation';import {hostApi,OCCASIONS} from '../../lib/host-api.mjs';import '../host.css';import '../host-responsive.css';;import {saveFlash} from '../../lib/flash.mjs';import HostToast from '../components/HostToast';
type EventItem = {
  id: string;
  publicToken: string;
  title: string;
  description: string | null;
  occasionType: string;
  eventDate: string | null;
  status: string;
  canPublish: boolean; publishDisabledReasons?: string[];
};
const symbols:Record<string,string>={WEDDING:'💍',BIRTHDAY:'🎂',BABY_SHOWER:'🧸',HOUSEWARMING:'🏡',ANNIVERSARY:'🥂',OTHER:'✨'};
function ClassicEvents(){const {emailVerificationEnabled,mobileOtpEnabled}=useVerification();const router=useRouter();const [user,setUser]=useState('');const [profileName,setProfileName]=useState('');const [nameDraft,setNameDraft]=useState('');const [events,setEvents]=useState<EventItem[]>([]);const [loading,setLoading]=useState(true);const [busy,setBusy]=useState(false);const [error,setError]=useState('');const [title,setTitle]=useState('');const [description,setDescription]=useState('');const [occasionType,setOccasion]=useState('BIRTHDAY');const [eventDate,setDate]=useState('');const [addressLine1,setAddressLine1]=useState('');const [city,setCity]=useState('');const [state,setState]=useState('');const [postalCode,setPostalCode]=useState('');const [country,setCountry]=useState('India');const [hostName,setHostName]=useState('');const [contactName,setContactName]=useState('');const [contactPhone,setContactPhone]=useState('');const [contactEmail,setContactEmail]=useState('');const [deliverySameAsEvent,setDeliverySameAsEvent]=useState(true);const [deliveryAddress,setDeliveryAddress]=useState({...emptyDeliveryAddress});
async function reload(){setLoading(true);setError('');try{const [me,profile,data]=await Promise.all([hostApi.me(fetch),hostApi.profileName(fetch),hostApi.events(fetch)]);setUser(me.username||me.email||'');setProfileName(profile.fullName||'');setEvents(data);}catch(e){setError(e instanceof Error?e.message:'Unable to load events');}finally{setLoading(false);}}
useEffect(()=>{void reload();},[emailVerificationEnabled,mobileOtpEnabled]);
async function create(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');try{const created=await hostApi.createEvent(fetch,{title,description,occasionType,eventDate,addressLine1,city,state,postalCode,country,contactName,contactPhone,contactEmail,hostName,deliverySameAsEvent,deliveryAddress});saveFlash(window.sessionStorage,'success','Your event is created! Start adding gifts to its wishlist.');router.push(`/gifts?eventId=${encodeURIComponent(created.id)}`);}catch(e){setError(e instanceof Error?e.message:'Could not create event');}finally{setBusy(false);}}
async function logout(){setBusy(true);try{await hostApi.logout(fetch);saveFlash(window.sessionStorage,'info','You have signed out successfully.');router.replace('/login');router.refresh();}catch(e){setError(e instanceof Error?e.message:'Logout failed');setBusy(false);}}
async function publishEvent(id: string) {
    setBusy(true);
    setError('');
  
    try {
      await hostApi.publishEvent(fetch, id);
      saveFlash(
        window.sessionStorage,
        'success',
        'Your celebration is now published!'
      );
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not publish event');
    } finally {
      setBusy(false);
    }
  }
  async function unpublishEvent(id: string) {
    setBusy(true);
    setError('');
  
    try {
      await hostApi.unpublishEvent(fetch, id);
  
      saveFlash(
        window.sessionStorage,
        'success',
        'Your event is now private. Existing invitation links are temporarily unavailable.'
      );
  
      await reload();
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : 'Could not unpublish event'
      );
    } finally {
      setBusy(false);
    }
  }
async function saveName(e:React.FormEvent){e.preventDefault();try{const profile=await hostApi.setProfileName(fetch,nameDraft);setProfileName(profile.fullName);}catch(e){setError(e instanceof Error?e.message:'Unable to save name');}}
return <main className="hostShell"><HostToast/>{!loading&&<div style={{padding:'12px 24px'}}>{profileName?<h2>Welcome back, {profileName}!</h2>:<form onSubmit={saveName}><label>What should we call you? <input required minLength={2} maxLength={120} value={nameDraft} onChange={e=>setNameDraft(e.target.value)}/></label> <button type="submit">Save name</button></form>}</div>}<div className="hostWide"><section className="hostBanner"><span className="hostKicker">YOUR CELEBRATION STUDIO</span><h1>Every moment deserves magic.</h1><p>Plan an occasion, design its invitation, and curate gifts that mean something.</p></section>{error&&<div role="alert" className="hostError">{error} {error==='Please sign in'&&<Link href="/login">Go to login</Link>}</div>}<div className="hostColumns"><section className="hostPanel"><h2>Create a celebration</h2><form onSubmit={create} className="hostForm"><label>Event name<input required maxLength={200} value={title} onChange={e=>setTitle(e.target.value)} placeholder="A special birthday celebration"/></label><label>Occasion<select value={occasionType} onChange={e=>setOccasion(e.target.value)}>{OCCASIONS.map(o=><option key={o} value={o}>{o.replaceAll('_',' ')}</option>)}</select></label><label>Date (optional)<input type="date" value={eventDate} onChange={e=>setDate(e.target.value)}/></label><label>About this event<textarea maxLength={10000} rows={3} value={description} onChange={e=>setDescription(e.target.value)}/></label><h3>Event address</h3><label>Street address *<input required maxLength={200} autoComplete="street-address" value={addressLine1} onChange={e=>setAddressLine1(e.target.value)}/></label><label>City *<input required maxLength={120} autoComplete="address-level2" value={city} onChange={e=>setCity(e.target.value)}/></label><label>State *<input required maxLength={120} autoComplete="address-level1" value={state} onChange={e=>setState(e.target.value)}/></label><label>PIN / postal code *<input required maxLength={20} autoComplete="postal-code" value={postalCode} onChange={e=>setPostalCode(e.target.value)}/></label><label>Country *<input required maxLength={80} autoComplete="country-name" value={country} onChange={e=>setCountry(e.target.value)}/></label><DeliveryAddressFields same={deliverySameAsEvent} address={deliveryAddress} onSameChange={setDeliverySameAsEvent} onAddressChange={setDeliveryAddress}/><h3>Host details</h3><label>Host name (shown to guests) *<input required maxLength={200} value={hostName} onChange={e=>setHostName(e.target.value)} placeholder="e.g. Jiju & Family"/></label><h3>Private event contact</h3><label>Contact name *<input required maxLength={150} value={contactName} onChange={e=>setContactName(e.target.value)}/></label><label>Contact phone *<input required type="tel" maxLength={30} value={contactPhone} onChange={e=>setContactPhone(e.target.value)}/></label><label>Contact email (optional)<input type="email" maxLength={254} value={contactEmail} onChange={e=>setContactEmail(e.target.value)}/></label><p>The guest managing a selected gift can view the delivery address. Contact details stay private. Postal-code format validation does not confirm physical deliverability.</p><button disabled={busy||loading} className="hostPrimary">{busy?'Creating…':'Create event →'}</button></form></section><section className="hostPanel"><div className="hostPanelTop"><h2>My events</h2><button disabled={loading||busy} onClick={()=>void reload()}>Refresh</button></div>{loading?<p>Loading your events…</p>:events.length===0?<div className="hostEmpty">No events yet. Create your first celebration.</div>:<div className="hostCards">{events.map(e=><article key={e.id} className="hostEvent"><span className="hostEmoji">{symbols[e.occasionType]||'✨'}</span><div><div className="hostStatus">{e.status} · {e.occasionType.replaceAll('_',' ')}</div><h3>{e.title}</h3><p>{e.eventDate||'Date to be decided'}</p>
<div className="hostEventActions">
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/thank-you`}>
    💌 Thank-you card designer →
  </Link>
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/celebration`}>💌 RSVP & Guestbook →</Link>
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/contributions`}>
    📊 Contribution report →
  </Link>
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/notifications`}>
    🔔 Notification history →
  </Link>
  <Link
    className="hostEventLink"
    href={`/gifts?eventId=${encodeURIComponent(e.id)}`}
  >
    Manage gifts →
  </Link>

  <Link
    className="hostEventLink"
    href={`/events/${encodeURIComponent(e.id)}/preview`}
  >
    Preview celebration →
  </Link>
  {e.status === 'DRAFT' && !e.canPublish && <Link href={`/events/${e.id}/receiving-accounts`}>{e.publishDisabledReasons?.join("; ")||"Verify email and add a preferred receiving account to publish."}</Link>}
  {e.status === 'DRAFT' && (
  <span title={e.publishDisabledReasons?.join("; ")}><button
    type="button"
    className="hostEventLink"
    disabled={busy || loading || !e.canPublish}
    title={e.canPublish ? undefined : e.publishDisabledReasons?.join("; ")}
    onClick={() => {
      if (
        window.confirm(
          `Publish "${e.title}"? Guests will be able to view this celebration.`
        )
      ) {
        void publishEvent(e.id);
      }
    }}
  >
    {busy ? 'Publishing…' : '✨ Publish event'}
  </button></span>
)}
{e.status === 'PUBLISHED' && e.publicToken && (
  <Link
    className="hostEventLink"
    href={`/e/${encodeURIComponent(e.publicToken)}`}
    target="_blank"
    rel="noopener noreferrer"
  >
    🔗 View public invitation →
  </Link>
)}
{e.status === 'PUBLISHED' && (
  <button
    type="button"
    className="hostEventLink"
    disabled={busy || loading}
    onClick={() => {
      if (
        window.confirm(
          `Unpublish "${e.title}"?\n\nGuests will temporarily lose access to the invitation. You can publish it again later.`
        )
      ) {
        void unpublishEvent(e.id);
      }
    }}
    >
    🔒 Unpublish event
  </button>
)}
<Link
  className="hostEventLink"
  href={`/events/${encodeURIComponent(e.id)}/qr`}
>
  📱 QR Invitation →
</Link>
</div>

</div></article>)}</div>}</section></div></div></main>;
}

export default function Events(){const {mode}=useExperience();return mode==='FOCUSED'?<FocusedEvents/>:<ClassicEvents/>;}
