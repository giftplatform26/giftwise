'use client';
import Link from 'next/link';
import {localizeText} from '../../lib/localization.mjs';
import {useLocalizationRender} from '../../components/LocalizationProvider';
import {ShowcaseStrip} from '../../components/Showcase';
import '../../components/acquisition.css';

export default function ShowcasePage(){useLocalizationRender();
 return <main className="ppPage"><header className="ppHero"><p className="focusEyebrow">{localizeText('Shared with permission')}</p><h1>{localizeText('Real celebrations')}</h1>
  <p className="ppBio">{localizeText('Weddings, birthdays and get-togethers our hosts were happy to share, with a few of the wishes their guests sent.')}</p>
  <Link className="focusPrimary" href="/try">{localizeText('Create yours — free')}</Link></header>
  <ShowcaseStrip limit={24} title={false}/>
  <p className="gbFine ppFoot">{localizeText('Nothing appears here without the host’s consent and a review by the GiftWise team.')}</p></main>;
}
