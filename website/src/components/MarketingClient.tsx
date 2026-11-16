'use client';
import { useEffect, useMemo, useState } from 'react';
import { ArrowUpRight, Check, Gift, Heart, Menu, Play, Users, Wallet, X } from 'lucide-react';
import { loginUrl } from '@/lib/site';
import { type DemoGift } from '@/lib/demo-data';
import { Avatars, Brand, CreateButton, Eyebrow } from './ui';
import { GiftPreviewDialog, InfoDialog, WalkthroughDialog, type InfoKind } from './Dialogs';
import { MarketingContext, useMarketing } from './marketing-context';

/** Client islands for the home page; everything else in MarketingWebsite renders on the server. */
export function MarketingProvider({ children }: { children: React.ReactNode }) {
  const [gift, setGift] = useState<DemoGift | null>(null), [walkthrough, setWalkthrough] = useState(false), [info, setInfo] = useState<InfoKind | null>(null);
  const actions = useMemo(() => ({ openGift: setGift, openWalkthrough: () => setWalkthrough(true), openInfo: setInfo }), []);
  useEffect(() => { if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) return; const observer = new IntersectionObserver(entries => { entries.forEach(entry => { if (entry.isIntersecting) { entry.target.classList.add('is-visible'); observer.unobserve(entry.target); } }); }, { threshold: 0.08 }); document.querySelectorAll('[data-reveal]').forEach(el => observer.observe(el)); document.documentElement.classList.add('motion-ready'); return () => { observer.disconnect(); document.documentElement.classList.remove('motion-ready'); }; }, []);
  return <MarketingContext.Provider value={actions}>{children}{gift && <GiftPreviewDialog gift={gift} onClose={() => setGift(null)} />}{walkthrough && <WalkthroughDialog onClose={() => setWalkthrough(false)} />}{info && <InfoDialog kind={info} onClose={() => setInfo(null)} />}</MarketingContext.Provider>;
}

export function WatchButton() {
  const { openWalkthrough } = useMarketing();
  return <button className="button button-ghost" onClick={openWalkthrough}><span className="play-circle"><Play size={11} fill="currentColor" /></span>Watch how it works</button>;
}

export function InfoButton({ kind, label, children }: { kind: InfoKind; label?: string; children?: React.ReactNode }) {
  const { openInfo } = useMarketing();
  return <button aria-label={label} onClick={() => openInfo(kind)}>{children}</button>;
}

export function Header() {
  const [open, setOpen] = useState(false), [scrolled, setScrolled] = useState(false);
  useEffect(() => { const scroll = () => setScrolled(window.scrollY > 12); scroll(); window.addEventListener('scroll', scroll, { passive: true }); const key = (e: KeyboardEvent) => { if (e.key === 'Escape') setOpen(false); }; window.addEventListener('keydown', key); return () => { window.removeEventListener('scroll', scroll); window.removeEventListener('keydown', key); }; }, []);
  const links = [{ href: '#how-it-works', label: 'How it works' }, { href: '#features', label: 'Features' }, { href: '#wishlist', label: 'The wishlist' }, { href: '#for-planners', label: 'For planners' }, { href: '#faq', label: 'FAQ' }];
  return <header className={`site-header${scrolled ? ' is-scrolled' : ''}`}><div className="container-shell nav-shell"><Brand /><nav className="desktop-nav" aria-label="Main navigation">{links.map(l => <a href={l.href} key={l.href}>{l.label}</a>)}</nav><div className="header-actions"><a className="login-link" href={loginUrl}>Log in</a><CreateButton className="nav-create">Create an event</CreateButton><button className="mobile-menu-toggle" aria-label={open ? 'Close navigation' : 'Open navigation'} aria-expanded={open} aria-controls="mobile-nav" onClick={() => setOpen(!open)}>{open ? <X size={22} /> : <Menu size={22} />}</button></div></div>{open && <nav id="mobile-nav" className="mobile-nav" aria-label="Mobile navigation">{links.map(l => <a href={l.href} key={l.href} onClick={() => setOpen(false)}>{l.label}<ArrowUpRight size={17} /></a>)}<a href={loginUrl}>Log in<ArrowUpRight size={17} /></a><CreateButton /></nav>}</header>;
}

export function ContributionWays() {
  const [active, setActive] = useState(0);
  const ways = [{ title: 'The whole happy thing.', label: 'BUY THE ENTIRE GIFT', icon: Gift, color: 'violet', copy: 'Found the perfect thing on their list? Make the whole wish come true.', art: 'whole', detail: 'One thoughtful guest. One complete gift. Zero guessing.' }, { title: 'A little goes a long way.', label: 'CONTRIBUTE PARTIALLY', icon: Heart, color: 'coral', copy: 'Big dreams don’t need big budgets. Add an amount that feels right.', art: 'partial', detail: 'Give what you’re comfortable with. Every contribution brings a wish closer.' }, { title: 'For whatever comes next.', label: 'WALLET CONTRIBUTION', icon: Wallet, color: 'mint', copy: 'Let the hosts put your contribution towards what matters most.', art: 'wallet', detail: 'A little flexibility for their next chapter, and all the things still to come.' }, { title: 'More friends. More magic.', label: 'GROUP CONTRIBUTION', icon: Users, color: 'peach', copy: 'Bring your favourite people together for an extraordinary gift.', art: 'group', detail: 'Split a gift, follow the progress, and share one wonderful surprise.' }];
  return <section id="contribute" className="section-space contribution-section"><div className="container-shell"><div className="section-heading" data-reveal><Eyebrow>THERE&apos;S NO ONE WAY TO BE THOUGHTFUL.</Eyebrow><h2>Your heart.<br /><span>Your way to contribute.</span></h2><p>Whatever your budget. However you want to give.<br />There&apos;s a meaningful way to be part of their moment.</p></div><div className="ways-grid" data-reveal>{ways.map(({ title, label, icon: Icon, color, copy, art }, i) => <button key={title} className={`way-card way-${color}${active === i ? ' is-active' : ''}`} onClick={() => setActive(i)} aria-pressed={active === i}><div className={`way-art way-art-${art}`}>{art === 'whole' ? <><div className="little-gift-box"><Gift size={48} strokeWidth={1.4} /></div><span className="way-floating-check"><Check size={18} /></span></> : art === 'partial' ? <><span className="contribution-coin coin-one">₹</span><span className="contribution-coin coin-two">₹</span><Heart size={41} fill="currentColor" strokeWidth={1} /></> : art === 'wallet' ? <><div className="wallet-illustration"><Wallet size={48} strokeWidth={1.2} /><span><Heart size={15} fill="currentColor" /></span></div><span className="wallet-spark">✦</span></> : <><Avatars /><span className="group-mini-gift"><Gift size={28} /></span><span className="way-group-heart">♡</span></>}</div><span className="way-label">{label}</span><h3>{title}</h3><p>{copy}</p><span className="way-select"><Icon size={15} />{active === i ? 'This feels like me' : 'Explore this way'}<ArrowUpRight size={14} /></span></button>)}</div><div className="way-detail" aria-live="polite"><Heart size={15} />{ways[active].detail}</div></div></section>;
}
