import QRCode from 'qrcode';
import MarketingWebsite from '@/components/MarketingWebsite';
import { site } from '@/lib/site';
export default async function HomePage() {
  const qrSvg = await QRCode.toString(`${site.url}/#wishlist`, { type: 'svg', margin: 1, width: 160, color: { dark: '#29223F', light: '#FFFFFF' }, errorCorrectionLevel: 'M' });
  return <MarketingWebsite qrSvg={qrSvg} />;
}
