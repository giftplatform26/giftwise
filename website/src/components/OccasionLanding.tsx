import { ArrowUpRight, Check, ChevronDown, Gift } from 'lucide-react';
import { createEventUrl, tryUrl } from '@/lib/site';
import { occasionPages, type OccasionPage } from '@/lib/occasions';

/** Server-rendered landing page for one occasion — fast, indexable, and consistent with the home page styles. */
export function OccasionLanding({ page }: { page: OccasionPage }) {
  const others = occasionPages.filter(o => o.slug !== page.slug);
  return <>
    <header className="site-header is-scrolled"><div className="container-shell nav-shell"><a className="brand" href="/" aria-label="GiftWise home"><span className="brand-mark"><Gift size={23} strokeWidth={1.8} /></span><span>gift<span className="brand-light">wise</span><span className="brand-period">.</span></span></a>
      <div className="header-actions"><a className="button button-primary nav-create" href={tryUrl}>Try it free<ArrowUpRight size={18} /></a></div></div></header>
    <main id="main" className="occ-page">
      <section className="occ-hero"><div className="container-shell"><span className="occ-kicker">{page.kicker}</span><h1>{page.heading}</h1><p>{page.intro}</p>
        <div className="hero-buttons"><a className="button button-primary" href={tryUrl}>Try it free — no sign-up<ArrowUpRight size={18} /></a><a className="button button-ghost" href={createEventUrl}>Create your event</a></div>
        <p className="occ-reassure"><Check size={13} /> Free to start · Guests need no app · Gifts go straight to you</p></div></section>
      <section className="section-space"><div className="container-shell"><h2 className="occ-h2">What you can do</h2>
        <div className="occ-features">{page.features.map(f => <article key={f.title}><span className="occ-tick"><Check size={14} /></span><h3>{f.title}</h3><p>{f.copy}</p></article>)}</div></div></section>
      <section className="occ-steps-section"><div className="container-shell"><h2 className="occ-h2">How it works</h2>
        <ol className="occ-steps">{page.steps.map((s, i) => <li key={s}><span>{i + 1}</span>{s}</li>)}</ol></div></section>
      <section className="section-space"><div className="container-shell faq-layout"><div className="faq-head"><h2>Questions</h2></div>
        <div className="faq-list">{page.faqs.map(([q, a]) => <details key={q}><summary>{q}<ChevronDown size={18} /></summary><p>{a}</p></details>)}</div></div></section>
      <section className="final-cta-section"><div className="container-shell"><div className="final-cta"><h2>Ready when you are.</h2><p>See your invitation come alive in under a minute — no sign-up needed.</p><a className="button button-cream" href={tryUrl}>Try it free<ArrowUpRight size={18} /></a></div></div></section>
      <nav className="occ-more container-shell" aria-label="Other celebrations"><h2>Made for every celebration</h2><ul>{others.map(o => <li key={o.slug}><a href={`/occasions/${o.slug}/`}>{o.label}<ArrowUpRight size={13} /></a></li>)}</ul></nav>
    </main>
    <footer className="site-footer"><div className="container-shell footer-bottom"><span>© {new Date().getFullYear()} GiftWise. A little more meaning.</span><a href="/">Home</a></div></footer>
  </>;
}
