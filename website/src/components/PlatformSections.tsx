// Server components: static marketing sections (FeatureJourney, the only interactive one, lives in FeatureJourney.tsx).
import {
  ArrowUpRight, BadgeCheck, BookHeart, BookOpen, Briefcase, Building2, CalendarHeart, CalendarRange, Check, ChevronDown, Clock, DoorOpen, Gift,
  HandHeart, Heart, Images, Languages, ListChecks, Lock, Mail, Megaphone, MessageCircle, MonitorPlay, NotebookPen, QrCode, ReceiptText, ShieldCheck,
  Smartphone, Sparkles, Store, Tv, UsersRound, Utensils, Wallet, type LucideIcon,
} from 'lucide-react';
import { createEventUrl, registerUrl, showcaseUrl, tryUrl } from '@/lib/site';
import { Eyebrow, IconTile } from './ui';

/** Trust: direct payments, privacy and control. */
export function TrustSection() {
  const items = [
    { icon: Lock, title: 'Money goes straight to you', copy: 'Guests pay your own UPI or bank account. GiftWise never holds gift money.' },
    { icon: ReceiptText, title: 'Every gift has a receipt', copy: 'A payment code makes transfers easy to match; you confirm when the money arrives.' },
    { icon: ShieldCheck, title: 'Private by default', copy: 'Guests never see each other’s amounts, phone numbers or emails. You choose what is shared.' },
    { icon: BadgeCheck, title: 'You stay in control', copy: 'Co-hosts get only the tools you allow. Payments, accounts and your budget stay yours.' },
  ];
  return <section className="trust-section"><div className="container-shell"><div className="trust-panel" data-reveal>
    <div className="trust-head"><Eyebrow light>BUILT FOR TRUST</Eyebrow><h2>Love, gifts and money —<br /><span>handled with care.</span></h2></div>
    <div className="trust-grid">{items.map(({ icon: Icon, title, copy }) => <div key={title}><span className="trust-icon"><Icon size={20} /></span><h3>{title}</h3><p>{copy}</p></div>)}</div>
  </div></div></section>;
}

/** Planners, venues and offices: the business side. */
export function ForPlanners() {
  return <section id="for-planners" className="section-space planners-section"><div className="container-shell"><div className="planners-grid">
    <article className="planner-card planner-main" data-reveal><Eyebrow>FOR PLANNERS, VENUES &amp; VENDORS</Eyebrow><h2>Run every client’s celebration<br /><span>from one workspace.</span></h2>
      <p>Set up the invitation, schedule and wishlist for your client, then hand it over with one link. They add their own payment account; you stay on as their planner — and every guest sees your name.</p>
      <ul className="planner-list">{['Your business page with logo, contact and real celebrations', 'Hand over in one tap — clients own their gifts and money', 'Keep the checklist, run sheet, register and check-in', '“Planned by” your business on every invitation'].map(t => <li key={t}><span><Check size={13} /></span>{t}</li>)}</ul>
      <a className="button button-primary" href={registerUrl}>Create a partner account<ArrowUpRight size={18} /></a></article>
    <div className="planner-side">
      <article className="planner-card planner-mini" data-reveal><span className="planner-mini-icon"><Building2 size={22} /></span><h3>For banquet halls &amp; decorators</h3><p>Give every booking a beautiful invitation and live wall — and a reason for guests to remember your name.</p></article>
      <article className="planner-card planner-mini planner-office" data-reveal><span className="planner-mini-icon"><Briefcase size={22} /></span><h3>For offices &amp; teams</h3><p>Farewells, birthdays, new babies. Start a group gift in under a minute and share one link on the team chat.</p><a className="text-link" href={createEventUrl}>Start a group gift <ArrowUpRight size={14} /></a></article>
      <article className="planner-card planner-mini" data-reveal><span className="planner-mini-icon"><Store size={22} /></span><h3>A trusted vendor directory</h3><p>Hand-picked decorators, caterers and photographers by city, with a shortlist for every host.</p></article>
    </div>
  </div></div></section>;
}

/** "Try it free" band. */
export function TryBand() {
  return <section className="try-band"><div className="container-shell"><div className="try-band-inner" data-reveal>
    <div><span className="try-badge">NO SIGN-UP NEEDED</span><h2>See your invitation come alive<br /><span>in under a minute.</span></h2><p>Pick the occasion, type your names and watch the preview update. Save it with a free account only when you love it.</p></div>
    <div className="try-actions"><a className="button button-primary" href={tryUrl}>Try it free<ArrowUpRight size={18} /></a><a className="button button-ghost" href={showcaseUrl}>See real celebrations</a></div>
  </div></div></section>;
}

const faqs: [string, string][] = [
  ['Does GiftWise hold the money guests send?', 'No. For UPI and bank transfers, guests pay your own account directly using a payment code, and you confirm when it arrives. GiftWise records the gift and issues a receipt; it never holds gift money.'],
  ['Can I use it for something other than a wedding or birthday?', 'Yes — housewarmings, baby showers, anniversaries, farewells, reunions and more. You can even start an office or group gift without creating an event at all.'],
  ['Do my guests need to download an app or create an account?', 'No. Guests open your invitation link or scan the QR code in any browser. They can RSVP, send wishes and gift without signing up.'],
  ['Can my family help me manage the celebration?', 'Yes. Invite co-hosts and gift-desk helpers. Each role sees only the tools you allow — like the gift register or check-in — while payments, receiving accounts and your budget stay with you.'],
  ['Will guests see how much others gave?', 'No. Individual amounts stay private to you. For group gifts you can choose to show overall progress without names or amounts.'],
  ['Can guests reply on WhatsApp?', 'Yes. Invitations and reminders go out on WhatsApp, and guests can reply YES, NO or MAYBE (with the number of people). Replying STOP stops further messages.'],
  ['What happens on the day itself?', 'Put a live wall of wishes on the big screen, check guests in at the door, record envelopes in the digital shagun register (it works on weak Wi-Fi), follow your run sheet and post live updates.'],
  ['I’m a wedding planner or venue. Can I use GiftWise for clients?', 'Yes. Create a partner profile, set up each client’s celebration, and hand it over with one link. You stay on as their planner and your name appears on the invitation.'],
  ['Is my data private?', 'Your guest list, contacts and gift amounts are visible only to you and the helpers you choose. Public pages show only what you publish, and the showcase includes a celebration only with your consent and a review.'],
  ['How much does it cost?', 'You can start for free. Premium plans and any payment-provider charges are confirmed in the app before you pay — never as a surprise.'],
];

export function Faq() {
  return <section id="faq" className="section-space faq-section"><div className="container-shell faq-layout">
    <div className="faq-head" data-reveal><Eyebrow>GOOD QUESTIONS</Eyebrow><h2>Everything you<br /><span>wanted to ask.</span></h2><p>Can’t find your answer? Write to us from the contact link below — a real person will reply.</p></div>
    <div className="faq-list" data-reveal>{faqs.map(([q, a]) => <details key={q}><summary>{q}<ChevronDown size={18} /></summary><p>{a}</p></details>)}</div>
  </div></section>;
}
