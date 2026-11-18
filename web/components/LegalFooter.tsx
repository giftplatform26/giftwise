'use client';
/* localization:instrumented */
import Link from 'next/link';
import {usePathname} from 'next/navigation';
import {BrandLogo,useBranding} from './BrandingProvider';
import {localize} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';
import {isHostPath} from '../lib/host-routes.mjs';
export default function LegalFooter(){useLocalizationRender();const {brand}=useBranding();const host=isHostPath(usePathname());
 return <footer className="giftwiseFooter"><BrandLogo size={32}/><p>{brand.footerText}</p><nav aria-label={localize("ui.4787eaf7c938045f",{defaultValue:"Legal"})}><ul className="giftwiseFooterLinks"><li><Link href="/privacy">{localize("ui.506ff394621596dd",{defaultValue:"Privacy Policy"})}</Link></li><li><Link href="/terms">{localize("ui.173f5f9f26b45592",{defaultValue:"Terms & Conditions"})}</Link></li><li><Link href="/contact">{localize("ui.98b67063cf8e584d",{defaultValue:"Contact Us"})}</Link></li><li><button type="button" onClick={()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))}>{localize("ui.6cff67309716528e",{defaultValue:"Cookie preferences"})}</button></li></ul></nav>{brand.supportEmail&&<p><a href={'mailto:'+brand.supportEmail}>{localize("ui.f8d47b82e285ba95",{defaultValue:"Contact Support"})}</a></p>}<div>{brand.socialLinks.split(/\r?\n/).filter(link=>/^https:\/\//.test(link)).map(link=><a key={link} href={link} rel="noopener noreferrer" target="_blank" style={{margin:8}}>{new URL(link).hostname}</a>)}</div>{host&&<p>{localize("ui.46fdf9658d7f7ded",{defaultValue:"For your security you will be automatically logged out after inactivity."})}</p>}<p>{localize("ui.f35880dddd5abe17",{defaultValue:"{{value0}} acts as a platform facilitating gifting between hosts and guests. {{value0}} is not responsible for product quality, delivery delays, or third-party merchant services, subject to applicable law.",value0:brand.applicationName})}</p></footer>}
