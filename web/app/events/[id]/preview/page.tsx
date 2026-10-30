'use client';
import GiftDescription from '../../../../components/GiftDescription';
import {giftPhotoUrl} from '../../../../lib/gift-photos.mjs';
import dynamic from 'next/dynamic';
import {useExperience} from '../../../../components/ExperienceProvider';
const FocusedPreview=dynamic(()=>import('../../../../components/focused/Preview'),{loading:()=> <main className="focusPage" role="status">Opening…</main>});
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { hostApi } from '../../../../lib/host-api.mjs';
import { giftApi } from '../../../../lib/gift-api.mjs';
import { eventDateLabel, moneyLabel, occasionStyle, validateEventId, visibleGifts } from '../../../../lib/event-preview.mjs';
import './preview.css';

type EventInfo = { id:string; title:string; description:string|null; occasionType:string; eventDate:string|null; status:string };
type GiftInfo = { imageKey?:string;id:string; name:string; description:string|null; targetAmount:string|number; status:string; visibility:string };
function ClassicEventPreview() {
  const params = useParams();
  const [event, setEvent] = useState<EventInfo|null>(null);
  const [gifts, setGifts] = useState<GiftInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const id = validateEventId(params.id);
        const eventData = await hostApi.getEvent(fetch, id);
        const giftData = await giftApi.list(fetch, id);
        if (active) { setEvent(eventData); setGifts(visibleGifts(giftData)); }
      } catch (err) { if (active) setError(err instanceof Error ? err.message : 'Could not load event preview'); }
      finally { if (active) setLoading(false); }
    }
    void load();
    return () => { active = false; };
  }, [params.id]);
  const style = occasionStyle(event?.occasionType);
  return <main className={`eventPreview eventPreview--${style.theme}`}>
    <header className="previewNav"><Link href="/events" className="previewBrand">← Back to Events</Link><Link href="/events" className="previewBrand">✦ Gift Platform</Link><span className="previewMode">HOST-ONLY PREVIEW · NOT SHAREABLE</span></header>
    {loading ? <div className="previewState" role="status">Preparing your celebration…</div> : error ?
      <div className="previewState" role="alert"><h1>Unable to open preview</h1><p>{error}</p><Link href="/login">Sign in</Link> · <Link href="/events">My events</Link></div> : event ? <>
      <section className="previewHero"><div className="previewSparkle" aria-hidden="true">✦ · ✧ · ✦</div><div className="previewOccasion">{style.icon} {style.eyebrow}</div><h1>{event.title}</h1><p className="previewGreeting">{style.greeting}</p><div className="previewDate">{eventDateLabel(event.eventDate)}</div><div className="previewDivider" aria-hidden="true">✧</div><p className="previewDescription">{event.description || 'A special day, shared with special people.'}</p><a className="previewExplore" href="#wishlist">Explore the wishlist ↓</a></section>
      <section id="wishlist" className="previewWishlist"><span className="previewEyebrow">THOUGHTFUL LITTLE WISHES</span><h2>Gifts that make memories</h2><p className="previewIntro">A glimpse of the gifts selected for this celebration.</p>
        {gifts.length === 0 ? <div className="previewEmpty">The wishlist is being prepared. Check back soon for some wonderful ideas.</div> :
          <div className="previewGiftGrid">{gifts.map(g => <article className="previewGift" key={g.id}><div className="previewGiftSymbol">{giftPhotoUrl(String(params.id),g.imageKey)?<img src={giftPhotoUrl(String(params.id),g.imageKey)} alt={g.name} style={{width:120,height:120,objectFit:'contain'}}/>:<span aria-hidden="true">🎁</span>}</div><div className="previewGiftBody"><h3>{g.name}</h3><GiftDescription name={g.name} description={g.description}/><div className="previewGiftPrice">{moneyLabel(g.targetAmount)}</div><span className="previewGiftHint">Contribution opens when the guest experience is published</span></div></article>)}</div>}
      </section><section className="previewFooter"><span>Made with love ✦ Gift Platform</span><Link href={`/gifts?eventId=${encodeURIComponent(event.id)}`}>Manage wishlist →</Link></section>
    </> : <div className="previewState" role="alert">Event unavailable</div>}
  </main>;
}

export default function EventPreview(){const {mode}=useExperience();return mode==='FOCUSED'?<FocusedPreview/>:<ClassicEventPreview/>;}
