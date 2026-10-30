import {csrfToken} from './gift-api.mjs';

const base='/backend/api/admin/invitation-assets';
export const categories=['BACKGROUND','DECORATION'];
export const occasions=['WEDDING','BIRTHDAY','BABY_SHOWER','HOUSEWARMING','ANNIVERSARY','OTHER'];
export const assetImageUrl=id=>`${base}/${encodeURIComponent(id)}/image`;

export function validateAsset({category,occasion,title,file}) {
 if(!categories.includes(category))throw Error('Select a valid category');
 if(!/^[A-Za-z0-9_-]{1,50}$/.test(occasion||''))throw Error('Select a valid occasion');
 if(!title?.trim()||title.trim().length>120)throw Error('Title must contain 1–120 characters');
 if(!file)throw Error('Choose an image');
 if(!['image/png','image/jpeg','image/webp'].includes(file.type))throw Error('Choose a PNG, JPEG or WebP image');
 if(file.size>2*1024*1024)throw Error('Image must be 2 MB or smaller');
 return {category,occasion,title:title.trim(),file};
}
export async function galleryRequest(fetcher,{method='GET',path='',form, cookie, bootstrap=true}={}) {
 if(!['GET','POST'].includes(method))throw Error('Unsupported request method');
 if(path&&!/^\/[0-9a-f-]{36}\/(publish|unpublish)$/i.test(path))throw Error('Invalid asset action');
 if(method==='POST'&&bootstrap){
  const init=await fetcher('/backend/api/auth/csrf',{method:'GET',credentials:'include',cache:'no-store'});
  if(!init.ok)throw Error(`Could not initialize secure session (HTTP ${init.status})`);
 }
 const token=csrfToken(cookie ?? (typeof document==='undefined'?'':document.cookie));
 if(method==='POST'&&!token)throw Error('CSRF cookie missing. Reload and sign in again.');
 const headers={Accept:'application/json'};
 if(token&&method==='POST')headers['X-XSRF-TOKEN']=token;
 const response=await fetcher(base+path,{method,credentials:'include',cache:'no-store',headers,...(form?{body:form}:{})});
 if(!response.ok){
  const message=({400:'Invalid image or metadata',401:'Please sign in',403:'Platform administrator access required (or CSRF expired)',404:'Asset not found',413:'Image is too large'})[response.status]||`Request failed (HTTP ${response.status})`;
  throw Error(message);
 }
 return response.json();
}
export const adminGallery={
 list:fetcher=>galleryRequest(fetcher),
 upload:(fetcher,data,options={})=>{
  const valid=validateAsset(data);
  const form=new FormData();
  for(const key of ['category','occasion','title'])form.append(key,valid[key]);
  form.append('file',valid.file);
  return galleryRequest(fetcher,{method:'POST',form,...options});
 },
 setPublished:(fetcher,id,published,options={})=>galleryRequest(fetcher,{
  method:'POST',path:`/${id}/${published?'publish':'unpublish'}`,...options
 })
};
