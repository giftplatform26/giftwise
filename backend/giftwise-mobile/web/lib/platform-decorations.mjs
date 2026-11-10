import {publicAssetImageUrl} from './public-gallery.mjs';

const UUID =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function platformDecorationId(assetId) {
  if(typeof assetId!=='string' || !UUID.test(assetId)) {
    throw Error('Invalid platform decoration ID');
  }
  return `platform:${assetId.toLowerCase()}`;
}

export function validPlatformDecorationElement(element) {
  if(element?.type!=='sticker' ||
     typeof element.stickerId!=='string' ||
     !element.stickerId.startsWith('platform:')) {
    return false;
  }

  const id=element.stickerId.slice('platform:'.length);

  if(!UUID.test(id))return false;

  return element.src===publicAssetImageUrl(id)||element.src===`/backend/api/v1/commerce/templates/${id}/image`;
}

export function eligiblePlatformDecorations(assets,occasion) {
  if(!Array.isArray(assets))return [];

  return assets.filter(asset =>
    asset?.published===true &&
    asset.category==='DECORATION' &&
    typeof asset.id==='string' &&
    UUID.test(asset.id) &&
    typeof asset.title==='string' &&
    (asset.occasion===occasion || asset.occasion==='OTHER')
  );
}
