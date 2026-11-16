'use client';
import { useState } from 'react';
import {
  ArrowUpRight, BadgeCheck, BookHeart, BookOpen, Briefcase, Building2, CalendarHeart, CalendarRange, Check, ChevronDown, Clock, DoorOpen, Gift,
  HandHeart, Heart, Images, Languages, ListChecks, Lock, Mail, Megaphone, MessageCircle, MonitorPlay, NotebookPen, QrCode, ReceiptText, ShieldCheck,
  Smartphone, Sparkles, Store, Tv, UsersRound, Utensils, Wallet, type LucideIcon,
} from 'lucide-react';
import { createEventUrl, registerUrl, showcaseUrl, tryUrl } from '@/lib/site';
import { Eyebrow, IconTile } from './ui';

type Feature = { icon: LucideIcon; title: string; copy: string; tag?: string };
type Phase = { id: string; label: string; kicker: string; title: string; copy: string; color: string; features: Feature[]; screen: 'plan' | 'invite' | 'gift' | 'celebrate' | 'remember' };

const phases: Phase[] = [
  { id: 'plan', label: 'Plan', kicker: 'WEEKS BEFORE', title: 'Plan it calmly, together.', copy: 'From the first idea to the final checklist, every detail in one place — shared with the people helping you.', color: 'violet', screen: 'plan', features: [
    { icon: Sparkles, title: 'Try before you sign up', copy: 'Design your invitation with a live preview. Create a free account only when you want to share it.', tag: 'New' },
    { icon: CalendarRange, title: 'Every function, one schedule', copy: 'Haldi, mehendi, sangeet, reception — each with its own time, venue, dress code and RSVP.' },
    { icon: ListChecks, title: 'A planning checklist', copy: 'Ready-made tasks counted back from your date, with an owner for each and gentle overdue nudges.', tag: 'New' },
    { icon: Wallet, title: 'A budget that adds up', copy: 'Planned, booked and paid for every vendor, plus the payments due in the next two weeks. Private to you.', tag: 'New' },
    { icon: UsersRound, title: 'Co-hosts & a gift desk', copy: 'Invite family to help. Choose exactly what each person can see and do; money stays with you.' },
  ] },
  { id: 'invite', label: 'Invite', kicker: 'SPREAD THE WORD', title: 'Invitations people actually open.', copy: 'Beautiful, personal and easy to answer — wherever your guests already are.', color: 'coral', screen: 'invite', features: [
    { icon: QrCode, title: 'A QR invitation', copy: 'One link and one scan for the whole celebration: details, wishlist, RSVP and wishes.' },
    { icon: MessageCircle, title: 'WhatsApp invitations & replies', copy: 'Send invitations, reminders and thank-yous on WhatsApp. Guests reply “YES 3” and you see it instantly.', tag: 'New' },
    { icon: Smartphone, title: 'Status & Story cards', copy: 'Phone-sized invitation, countdown and thank-you cards for WhatsApp Status and Instagram Stories.', tag: 'New' },
    { icon: Utensils, title: 'Meal choices for the caterer', copy: 'Guests choose veg, Jain, non-veg or vegan per function. You get the totals.', tag: 'New' },
    { icon: Languages, title: 'In your guests’ language', copy: 'Invitations and pages that speak to every generation of the family.' },
  ] },
  { id: 'gift', label: 'Gift', kicker: 'MEANINGFUL GIFTING', title: 'Gifts you’ll love. Money that reaches you.', copy: 'A wishlist from any store, gifts big and small, and direct transfers straight to your account.', color: 'mint', screen: 'gift', features: [
    { icon: Gift, title: 'A wishlist from any store', copy: 'Amazon, Flipkart, Myntra, IKEA or any link. Guests see progress, so no duplicates.' },
    { icon: HandHeart, title: 'Group gifting', copy: 'Friends pool together for something extraordinary, then surprise you with it.' },
    { icon: ReceiptText, title: 'Direct UPI & bank with receipts', copy: 'Guests pay you directly with a payment code; both of you get a receipt — even a custom-designed one.' },
    { icon: Heart, title: 'Experiences & charity wishes', copy: 'Wish for a honeymoon dinner, or ask guests to donate to a cause you love.', tag: 'New' },
    { icon: Briefcase, title: 'Office & group gifts', copy: 'A colleague’s farewell or a teacher’s day gift — collect in a minute, no event needed.', tag: 'New' },
  ] },
  { id: 'celebrate', label: 'Celebrate', kicker: 'ON THE DAY', title: 'The big day, beautifully handled.', copy: 'Tools for the entrance, the gift desk, the stage and the screen — built for busy hands and patchy Wi-Fi.', color: 'peach', screen: 'celebrate', features: [
    { icon: MonitorPlay, title: 'A live wall of wishes', copy: 'Guests scan, write and share photos. Approved wishes glide onto the big screen.', tag: 'New' },
    { icon: DoorOpen, title: 'Entry check-in', copy: 'Search a name, tap once per family, add walk-ins — and watch the headcount live.', tag: 'New' },
    { icon: NotebookPen, title: 'A digital shagun register', copy: 'Record envelopes and gifts in seconds, even offline. Totals by family, export to Excel.', tag: 'New' },
    { icon: Clock, title: 'A day-of run sheet', copy: 'Minute by minute, with who is responsible. Share a view-only link with your vendors.', tag: 'New' },
    { icon: Megaphone, title: 'Live updates & a livestream', copy: '“Dinner is served” on every phone, plus a Watch live button for family far away.', tag: 'New' },
  ] },
  { id: 'remember', label: 'Remember', kicker: 'LONG AFTER', title: 'Memories that last. Relationships too.', copy: 'Keep the love you received, thank people properly, and never miss the next celebration.', color: 'violet', screen: 'remember', features: [
    { icon: Images, title: 'A private memory album', copy: 'Every photo and wish in one place, shared only with the people you choose.' },
    { icon: BookHeart, title: 'A printable memory book', copy: 'Your schedule, wishes and the people who blessed you — ready to print or save as PDF.', tag: 'New' },
    { icon: Mail, title: 'Thank-you cards', copy: 'Personal thank-you notes for every guest, in your words.' },
    { icon: BookOpen, title: 'Your gift ledger', copy: 'What you gave and what you received, person by person — no more guessing at the next wedding.', tag: 'New' },
    { icon: CalendarHeart, title: 'Dates to remember', copy: 'Birthdays and anniversaries of your people, with a nudge to celebrate again next year.', tag: 'New' },
  ] },
];

function PhaseScreen({ kind }: { kind: Phase['screen'] }) {
  if (kind === 'plan') return <div className="ps-screen" aria-hidden="true"><div className="ps-screen-head"><b>Meera &amp; Arjun · Wedding</b><span>38 days to go</span></div>
    <div className="ps-ring"><span>68%</span><small>tasks done</small></div>
    {[['Book the mehendi artist', 'Priya', 'This week', false], ['Confirm menu with caterer', 'You', 'Overdue', false], ['Send invitations', 'Ravi', 'Done', true]].map(([t, w, d, done]) => <div key={String(t)} className={`ps-task${done ? ' is-done' : ''}`}><span className="ps-tick">{done ? <Check size={12} /> : null}</span><div><b>{t}</b><small>{w}</small></div><em className={d === 'Overdue' ? 'is-late' : ''}>{d}</em></div>)}
    <div className="ps-budget"><span>Budget</span><div className="ps-bar"><i style={{ width: '46%' }} /><i className="booked" style={{ width: '28%' }} /></div><small>₹4.6L paid · ₹2.8L booked · ₹2.6L left</small></div></div>;
  if (kind === 'invite') return <div className="ps-screen ps-chat" aria-hidden="true"><div className="ps-chat-head"><MessageCircle size={15} /> WhatsApp</div>
    <div className="ps-bubble ps-in">Hi Asha! Meera &amp; Arjun invite you to their wedding on 12 December. See the invitation: giftwise.app/e/…<small>Reply YES, NO or MAYBE</small></div>
    <div className="ps-bubble ps-out">YES 3 🎉</div><div className="ps-replies"><span><b>124</b> coming</span><span><b>18</b> maybe</span><span><b>212</b> people</span></div>
    <div className="ps-meals"><span>🥗 96 veg</span><span>🌿 22 Jain</span><span>🍗 94 non-veg</span></div></div>;
  if (kind === 'gift') return <div className="ps-screen" aria-hidden="true"><div className="ps-receipt"><span className="ps-receipt-top">GIFT RECEIPT</span><b>Meera &amp; Arjun</b><strong>₹5,001</strong>
    <dl><div><dt>Receipt no.</dt><dd>MA2026-000017</dd></div><div><dt>From</dt><dd>Ravi Kumar</dd></div><div><dt>Payment code</dt><dd>GW-7K3P9Q</dd></div></dl>
    <span className="ps-confirmed"><BadgeCheck size={14} /> Confirmed received by the host</span></div><div className="ps-direct"><Lock size={13} /> Paid directly to the host’s UPI — GiftWise never holds the money</div></div>;
  if (kind === 'celebrate') return <div className="ps-screen ps-tv" aria-hidden="true"><div className="ps-tv-frame"><span className="ps-tv-live"><Tv size={12} /> LIVE WALL</span><p>“May your home always be full of laughter and good food!”</p><small>— Lata</small><span className="ps-tv-qr"><QrCode size={30} /><em>Scan to add your wish</em></span></div>
    <div className="ps-door"><DoorOpen size={16} /><div><b>186 people here</b><div className="ps-bar"><i style={{ width: '74%' }} /></div><small>of about 250 expected · 12 walk-ins</small></div></div></div>;
  return <div className="ps-screen ps-book" aria-hidden="true"><div className="ps-book-page"><span>OUR CELEBRATION</span><b>Meera &amp; Arjun</b><small>12 December 2026 · Kochi</small><em>212 guests celebrated with us</em></div>
    <div className="ps-ledger"><b>Ravi Kumar</b><div><span>They gave you</span><strong>₹5,100</strong></div><div><span>You gave them</span><strong>₹3,100</strong></div><small>Birthday in 9 days · last year you gave ₹1,100</small></div></div>;
}

/** Everything the platform does, organised by when a host needs it. */
export function FeatureJourney() {
  const [active, setActive] = useState(0);
  return <section id="features" className="section-space journey-section"><div className="container-shell">
    <div className="section-heading" data-reveal><Eyebrow>ONE PLACE FOR THE WHOLE CELEBRATION</Eyebrow><h2>From the first idea<br /><span>to the last thank-you.</span></h2><p>GiftWise grew from a better wishlist into everything a family needs to plan, invite, gift, celebrate and remember — switched on only when you want it.</p></div>
    <div className="journey-tabs" role="tablist" aria-label="Celebration stages" data-reveal>{phases.map((ph, i) => <button key={ph.id} role="tab" id={`tab-${ph.id}`} aria-selected={active === i} aria-controls={`panel-${ph.id}`} className={active === i ? 'is-active' : ''} onClick={() => setActive(i)}><span className="journey-step">{i + 1}</span>{ph.label}</button>)}</div>
    {phases.map((ph, i) => <div key={ph.id} className={`journey-panel journey-${ph.color}`} role="tabpanel" id={`panel-${ph.id}`} aria-labelledby={`tab-${ph.id}`} hidden={active !== i}>
      <div className="journey-copy"><span className="journey-kicker">{ph.kicker}</span><h3>{ph.title}</h3><p>{ph.copy}</p>
        <ul className="journey-features">{ph.features.map(({ icon, title, copy, tag }) => <li key={title}><IconTile icon={icon} color={ph.color} size={20} /><div><b>{title}{tag && <em className="journey-tag">{tag}</em>}</b><span>{copy}</span></div></li>)}</ul></div>
      <div className="journey-visual"><PhaseScreen kind={ph.screen} /></div>
    </div>)}
  </div></section>;
}
