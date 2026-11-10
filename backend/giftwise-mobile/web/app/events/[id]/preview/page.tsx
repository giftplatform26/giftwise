'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../../components/LocalizationProvider';

import GiftDescription from '../../../../components/GiftDescription';
import {giftPhotoUrl} from '../../../../lib/gift-photos.mjs';
import dynamic from 'next/dynamic';
import {useExperience} from '../../../../components/ExperienceProvider';
const FocusedPreview=dynamic(()=>import('../../../../components/focused/Preview'),{loading:()=> <main className="focusPage" role="status">{localize("ui.c926c2c50e65d5a5",{defaultValue:"Opening…"})}</main>});
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { hostApi } from '../../../../lib/host-api.mjs';
import { giftApi } from '../../../../lib/gift-api.mjs';
import { eventDateLabel, moneyLabel, occasionStyle, validateEventId, visibleGifts } from '../../../../lib/event-preview.mjs';
import './preview.css';

type EventInfo = { id:string; title:string; description:string|null; occasionType:string; eventDate:string|null; status:string };
type GiftInfo = { imageKey?:string;id:string; name:string; description:string|null; targetAmount:string|number; status:string; visibility:string };
function ClassicEventPreview() {useLocalizationRender();
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
    <header className="previewNav"><Link href="/events" className="previewBrand">{localize("ui.5b8afb8fdcf1e242",{defaultValue:"← Back to Events"})}</Link><Link href="/events" className="previewBrand">{localize("ui.775be12c6f4bb051",{defaultValue:"✦ GiftWise"})}</Link><span className="previewMode">{localize("ui.af33d8fbd40851f9",{defaultValue:"HOST-ONLY PREVIEW · NOT SHAREABLE"})}</span></header>
    {loading ? <div className="previewState" role="status">{localize("ui.8fe30ad66c95faea",{defaultValue:"Preparing your celebration…"})}</div> : error ?
      <div className="previewState" role="alert"><h1>{localize("ui.5f043140cbb27b0b",{defaultValue:"Unable to open preview"})}</h1><p>{localizeText(error)}</p><Link href="/login">{localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"})}</Link> · <Link href="/events">{localize("ui.30e9e213741ef578",{defaultValue:"My events"})}</Link></div> : event ? <>
      <section className="previewHero"><div className="previewSparkle" aria-hidden="true">✦ · ✧ · ✦</div><div className="previewOccasion">{style.icon} {style.eyebrow}</div><h1>{event.title}</h1><p className="previewGreeting">{style.greeting}</p><div className="previewDate">{eventDateLabel(event.eventDate)}</div><div className="previewDivider" aria-hidden="true">✧</div><p className="previewDescription">{event.description || localize("ui.860e15173f480222",{defaultValue:"A special day, shared with special people."})}</p><a className="previewExplore" href="#wishlist">{localize("ui.4699fd0a61bd3592",{defaultValue:"Explore the wishlist ↓"})}</a></section>
      <section id="wishlist" className="previewWishlist"><span className="previewEyebrow">{localize("ui.a0b6c9e94fa1b722",{defaultValue:"THOUGHTFUL LITTLE WISHES"})}</span><h2>{localize("ui.8302ff742ba14883",{defaultValue:"Gifts that make memories"})}</h2><p className="previewIntro">{localize("ui.7de176bc71c782d7",{defaultValue:"A glimpse of the gifts selected for this celebration."})}</p>
        {gifts.length === 0 ? <div className="previewEmpty">{localize("ui.699bbd1d88ab2b0c",{defaultValue:"The wishlist is being prepared. Check back soon for some wonderful ideas."})}</div> :
          <div className="previewGiftGrid">{gifts.map(g => <article className="previewGift" key={g.id}><div className="previewGiftSymbol">{giftPhotoUrl(String(params.id),g.imageKey)?<img src={giftPhotoUrl(String(params.id),g.imageKey)} alt={g.name} style={{width:120,height:120,objectFit:'contain'}}/>:<span aria-hidden="true">🎁</span>}</div><div className="previewGiftBody"><h3>{g.name}</h3><GiftDescription name={g.name} description={g.description}/><div className="previewGiftPrice">{moneyLabel(g.targetAmount)}</div><span className="previewGiftHint">{localize("ui.3ffd21e95c5a8360",{defaultValue:"Contribution opens when the guest experience is published"})}</span></div></article>)}</div>}
      </section><section className="previewFooter"><span>{localize("ui.85320364933e965d",{defaultValue:"Made with love ✦ GiftWise"})}</span><Link href={`/gifts?eventId=${encodeURIComponent(event.id)}`}>{localize("ui.a2fe4416afe87269",{defaultValue:"Manage wishlist →"})}</Link></section>
    </> : <div className="previewState" role="alert">{localize("ui.126db8776b98b7aa",{defaultValue:"Event unavailable"})}</div>}
  </main>;
}

export default function EventPreview(){useLocalizationRender();const {mode}=useExperience();return mode==='FOCUSED'?<FocusedPreview/>:<ClassicEventPreview/>;}
