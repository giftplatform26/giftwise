'use client';
import { ArrowUpRight, Gift, Heart, Check, type LucideIcon } from 'lucide-react';
import { createEventUrl } from '@/lib/site';
export function Brand({ inverse = false }: { inverse?: boolean }) {
  return <a className={`brand${inverse ? ' brand-inverse' : ''}`} href="#top" aria-label="GiftWise home"><span className="brand-mark"><Gift size={23} strokeWidth={1.8} /></span><span>gift<span className="brand-light">wise</span><span className="brand-period">.</span></span></a>;
}
export function CreateButton({ children = 'Create Your Event', className = '' }: { children?: React.ReactNode; className?: string }) {
  return <a className={`button button-primary ${className}`} href={createEventUrl}>{children}<ArrowUpRight size={18} /></a>;
}
export function Eyebrow({ children, light = false }: { children: React.ReactNode; light?: boolean }) {
  return <span className={`eyebrow${light ? ' eyebrow-light' : ''}`}><span />{children}</span>;
}
export function Avatars({ count, small = false }: { count?: number; small?: boolean }) {
  return <div className={`avatar-stack${small ? ' avatar-stack-small' : ''}`} aria-label={count ? `${count} sample contributors` : 'Friends contributing together'}>{['A', 'R', 'S'].map((name, i) => <span key={name} className={`avatar avatar-${i}`}>{name}</span>)}{count && <span className="avatar avatar-more">+{Math.max(0, count - 3)}</span>}</div>;
}
export function ProductArt({ index, className = '', label }: { index: number; className?: string; label: string }) {
  return <div role="img" aria-label={label} className={`product-art product-art-${index} ${className}`} />;
}
export function IconTile({ icon: Icon, color = 'violet', size = 23 }: { icon: LucideIcon; color?: string; size?: number }) {
  return <span className={`icon-tile icon-${color}`}><Icon size={size} strokeWidth={1.7} /></span>;
}
export function CheckLine({ children, light = false }: { children: React.ReactNode; light?: boolean }) {
  return <span className={`check-line${light ? ' check-line-light' : ''}`}><span><Check size={13} strokeWidth={2.7} /></span>{children}</span>;
}
export function LittleHeart({ className = '' }: { className?: string }) { return <Heart className={className} size={18} fill="currentColor" strokeWidth={1.5} />; }
