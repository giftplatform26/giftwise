'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';

import {useGrowth} from '../../components/GrowthProvider';
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
const symbols:Record<string,string>={WEDDING:'💍',BIRTHDAY:'🎂',BABY_SHOWER:'🧸',HOUSEWARMING:'🏡',ANNIVERSARY:'🥂',FAREWELL:'💐',RETIREMENT:'🌅',REUNION:'🤝',OTHER:'✨'};
function ClassicEvents(){useLocalizationRender();const {features}=useGrowth();const {emailVerificationEnabled,mobileOtpEnabled}=useVerification();const router=useRouter();const [user,setUser]=useState('');const [profileName,setProfileName]=useState('');const [nameDraft,setNameDraft]=useState('');const [events,setEvents]=useState<EventItem[]>([]);const [loading,setLoading]=useState(true);const [busy,setBusy]=useState(false);const [error,setError]=useState('');const [title,setTitle]=useState('');const [description,setDescription]=useState('');const [occasionType,setOccasion]=useState('BIRTHDAY');const [eventDate,setDate]=useState('');const [addressLine1,setAddressLine1]=useState('');const [city,setCity]=useState('');const [state,setState]=useState('');const [postalCode,setPostalCode]=useState('');const [country,setCountry]=useState('India');const [hostName,setHostName]=useState('');const [contactName,setContactName]=useState('');const [contactPhone,setContactPhone]=useState('');const [contactEmail,setContactEmail]=useState('');const [deliverySameAsEvent,setDeliverySameAsEvent]=useState(true);const [deliveryAddress,setDeliveryAddress]=useState({...emptyDeliveryAddress});
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
return <main className="hostShell"><HostToast/>{features.GUIDED_CREATION&&<p><Link className="focusPrimary" href="/events/new">{localizeText("Create with the step-by-step guide")} →</Link></p>}{!loading&&<div style={{padding:'12px 24px'}}>{profileName?<h2>{localize("ui.0c7229ae1986eb6d",{defaultValue:"Welcome back, "})}{profileName}!</h2>:<form onSubmit={saveName}><label>{localize("ui.eb821576f19fa523",{defaultValue:"What should we call you? "})}<input required minLength={2} maxLength={120} value={nameDraft} onChange={e=>setNameDraft(e.target.value)}/></label> <button type="submit">{localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})}</button></form>}</div>}<div className="hostWide"><section className="hostBanner"><span className="hostKicker">{localize("ui.4331b0fb55ffeda7",{defaultValue:"YOUR CELEBRATION STUDIO"})}</span><h1>{localize("ui.f84ffe003b345378",{defaultValue:"Every moment deserves magic."})}</h1><p>{localize("ui.326ddea76cfe8734",{defaultValue:"Plan an occasion, design its invitation, and curate gifts that mean something."})}</p></section>{error&&<div role="alert" className="hostError">{localizeText(error)} {error==='Please sign in'&&<Link href="/login">{localize("ui.b84cc3347b94fc0c",{defaultValue:"Go to login"})}</Link>}</div>}<div className="hostColumns"><section className="hostPanel"><h2>{localize("ui.c9b85fa91f9c467e",{defaultValue:"Create a celebration"})}</h2><form onSubmit={create} className="hostForm"><label>{localize("ui.25bc9efedfb71cbf",{defaultValue:"Event name"})}<input required maxLength={200} value={title} onChange={e=>setTitle(e.target.value)} placeholder={localize("ui.e427739c808d67ad",{defaultValue:"A special birthday celebration"})}/></label><label>{localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})}<select value={occasionType} onChange={e=>setOccasion(e.target.value)}>{OCCASIONS.map(o=><option key={o} value={o}>{o.replaceAll('_',' ')}</option>)}</select></label><label>{localize("ui.6807b347bcddc04f",{defaultValue:"Date (optional)"})}<input type="date" value={eventDate} onChange={e=>setDate(e.target.value)}/></label><label>{localize("ui.3cae814cc8c2d447",{defaultValue:"About this event"})}<textarea maxLength={10000} rows={3} value={description} onChange={e=>setDescription(e.target.value)}/></label><h3>{localize("ui.5c4829231a0afe1f",{defaultValue:"Event address"})}</h3><label>{localize("ui.2ec422832b2a8042",{defaultValue:"Street address *"})}<input required maxLength={200} autoComplete="street-address" value={addressLine1} onChange={e=>setAddressLine1(e.target.value)}/></label><label>{localize("ui.4b592363361d535f",{defaultValue:"City *"})}<input required maxLength={120} autoComplete="address-level2" value={city} onChange={e=>setCity(e.target.value)}/></label><label>{localize("ui.0553992d31b72bce",{defaultValue:"State *"})}<input required maxLength={120} autoComplete="address-level1" value={state} onChange={e=>setState(e.target.value)}/></label><label>{localize("ui.3ce9d7ae8a85a19b",{defaultValue:"PIN / postal code *"})}<input required maxLength={20} autoComplete="postal-code" value={postalCode} onChange={e=>setPostalCode(e.target.value)}/></label><label>{localize("ui.776f755f2fe0ac5b",{defaultValue:"Country *"})}<input required maxLength={80} autoComplete="country-name" value={country} onChange={e=>setCountry(e.target.value)}/></label><DeliveryAddressFields same={deliverySameAsEvent} address={deliveryAddress} onSameChange={setDeliverySameAsEvent} onAddressChange={setDeliveryAddress}/><h3>{localize("ui.49a9db7034abbf54",{defaultValue:"Host details"})}</h3><label>{localize("ui.7a7d38be87e59584",{defaultValue:"Host name (shown to guests) *"})}<input required maxLength={200} value={hostName} onChange={e=>setHostName(e.target.value)} placeholder={localize("ui.d991458c9b576d55",{defaultValue:"e.g. Jiju & Family"})}/></label><h3>{localize("ui.93ab0002f529851b",{defaultValue:"Private event contact"})}</h3><label>{localize("ui.fcf0d0752236b977",{defaultValue:"Contact name *"})}<input required maxLength={150} value={contactName} onChange={e=>setContactName(e.target.value)}/></label><label>{localize("ui.f4a1cd67585beb93",{defaultValue:"Contact phone *"})}<input required type="tel" maxLength={30} value={contactPhone} onChange={e=>setContactPhone(e.target.value)}/></label><label>{localize("ui.fb685cc8ddb2ec70",{defaultValue:"Contact email (optional)"})}<input type="email" maxLength={254} value={contactEmail} onChange={e=>setContactEmail(e.target.value)}/></label><p>{localize("ui.e67f2143a4beba8d",{defaultValue:"The guest managing a selected gift can view the delivery address. Contact details stay private. Postal-code format validation does not confirm physical deliverability."})}</p><button disabled={busy||loading} className="hostPrimary">{busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):localize("ui.d12514e733617cf7",{defaultValue:"Create event →"})}</button></form></section><section className="hostPanel"><div className="hostPanelTop"><h2>{localize("ui.30e9e213741ef578",{defaultValue:"My events"})}</h2><button disabled={loading||busy} onClick={()=>void reload()}>{localize("ui.0e91610117029a62",{defaultValue:"Refresh"})}</button></div>{loading?<p>{localize("ui.1d1e620809b2725f",{defaultValue:"Loading your events…"})}</p>:events.length===0?<div className="hostEmpty">{localize("ui.9642ae82e820ee2f",{defaultValue:"No events yet. Create your first celebration."})}</div>:<div className="hostCards">{events.map(e=><article key={e.id} className="hostEvent"><span className="hostEmoji">{symbols[e.occasionType]||'✨'}</span><div><div className="hostStatus">{localizeText(e.status)} · {localizeText(e.occasionType.replaceAll('_',' '))}</div><h3>{e.title}</h3><p>{e.eventDate||localize("ui.56e3ebcc6e9d7bf6",{defaultValue:"Date to be decided"})}</p>
<div className="hostEventActions">
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/thank-you`}>
    {localize("ui.c71cfd57c7228a33",{defaultValue:"💌 Thank-you card designer →"})}</Link>
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/celebration`}>{localize("ui.821ed0a399d1f9c9",{defaultValue:"💌 RSVP & Guestbook →"})}</Link>
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/contributions`}>
    {localize("ui.cf00ac7c25063577",{defaultValue:"📊 Contribution report →"})}</Link>
  <Link className="hostEventLink" href={`/events/${encodeURIComponent(e.id)}/notifications`}>
    {localize("ui.68685a19b9a47165",{defaultValue:"🔔 Notification history →"})}</Link>
  <Link
    className="hostEventLink"
    href={`/gifts?eventId=${encodeURIComponent(e.id)}`}
  >
    {localize("ui.73d04d819e4646de",{defaultValue:"Manage gifts →"})}</Link>

  <Link
    className="hostEventLink"
    href={`/events/${encodeURIComponent(e.id)}/preview`}
  >
    {localize("ui.ca8171fffe282db0",{defaultValue:"Preview celebration →"})}</Link>
  {e.status === 'DRAFT' && !e.canPublish && <Link href={`/events/${e.id}/receiving-accounts`}>{e.publishDisabledReasons?.join("; ")||localize("ui.657be741c06de5d5",{defaultValue:"Verify email and add a preferred receiving account to publish."})}</Link>}
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
    {busy ? localize("ui.582e0f1abad43e9e",{defaultValue:"Publishing…"}) : localize("ui.56b8d9be82d69e8f",{defaultValue:"✨ Publish event"})}
  </button></span>
)}
{e.status === 'PUBLISHED' && e.publicToken && (
  <Link
    className="hostEventLink"
    href={`/e/${encodeURIComponent(e.publicToken)}`}
    target="_blank"
    rel="noopener noreferrer"
  >
    {localize("ui.4fbee6b31a4d9a27",{defaultValue:"🔗 View public invitation →"})}</Link>
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
    {localize("ui.50ca41c9e596c471",{defaultValue:"🔒 Unpublish event"})}</button>
)}
<Link
  className="hostEventLink"
  href={`/events/${encodeURIComponent(e.id)}/qr`}
>
  {localize("ui.fd16dc1450a89a39",{defaultValue:"📱 QR Invitation →"})}</Link>
</div>

</div></article>)}</div>}</section></div></div></main>;
}

export default function Events(){useLocalizationRender();const {mode}=useExperience();const {features}=useGrowth();return mode==='FOCUSED'&&features.GUIDED_CREATION?<FocusedEvents/>:<ClassicEvents/>;}
