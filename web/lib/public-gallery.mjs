const base='/backend/api/public/invitation-assets';

export function publicAssetImageUrl(id){
 if(!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id))
  throw Error('Invalid asset ID');
 return `${base}/${encodeURIComponent(id)}/image`;
}

export async function listPublishedAssets(fetcher){
 const response=await fetcher(base,{
  credentials:'include',
  cache:'no-store'
 });
 if(!response.ok)throw Error(`Gallery HTTP ${response.status}`);
 const assets=await response.json();
 if(!Array.isArray(assets))throw Error('Invalid gallery response');
 return assets.filter(a=>
  a?.published===true &&
  ['BACKGROUND','DECORATION'].includes(a.category) &&
  typeof a.id==='string' &&
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(a.id) &&
  typeof a.title==='string' &&
  typeof a.occasion==='string'
 );
}
