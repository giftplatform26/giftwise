'use client';
/* localization:instrumented */
import {localize,localizeText} from '../../../lib/localization.mjs';
import {useLocalizationRender} from '../../../components/LocalizationProvider';


import {useCallback,useEffect,useState} from 'react';
import {useExperience} from '../../../components/ExperienceProvider';
import {adminGallery,assetImageUrl,categories,occasions} from '../../../lib/admin-gallery.mjs';
import {commercialApi} from '../../../components/CommercePurchase';
import {optimizeGalleryImage} from '../../../lib/gallery-image-optimizer.mjs';

type Asset={id:string;category:string;occasion:string;title:string;published:boolean;width:number;height:number;tier:string};

export default function AdminInvitationAssetsPage(){useLocalizationRender();
 const {mode}=useExperience();const focused=mode==='FOCUSED';
 const [assets,setAssets]=useState<Asset[]>([]);
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState('');
 const [notice,setNotice]=useState('');
 const [busy,setBusy]=useState(false);
 const [category,setCategory]=useState('BACKGROUND');
 const [tier,setTier]=useState('FREE');
 const [occasion,setOccasion]=useState('BIRTHDAY');
 const [title,setTitle]=useState('');
 const [file,setFile]=useState<File|null>(null);
 const [originalSize,setOriginalSize]=useState<number|null>(null);
 const [optimizing,setOptimizing]=useState(false);
 const [preview,setPreview]=useState('');
 const [filter,setFilter]=useState('ALL');
 const [status,setStatus]=useState('ALL');
 const refresh=useCallback(async()=>{
  try {setAssets(await adminGallery.list(fetch));setError('');}
  catch(e){setError(e instanceof Error?e.message:'Unable to load assets');}
  finally{setLoading(false);}
 },[]);
 useEffect(()=>{void refresh();},[refresh]);
 useEffect(()=>{
  if(!file){setPreview('');return;}
  const url=URL.createObjectURL(file);
  setPreview(url);
  return()=>URL.revokeObjectURL(url);
 },[file]);
 const selectImage=async(next:File|null)=>{
  setFile(null);setOriginalSize(null);setError('');
  if(!next)return;
  setOptimizing(true);
  try{const converted=await optimizeGalleryImage(next,category);setFile(converted);setOriginalSize(next.size);}
  catch(e){setError(e instanceof Error?e.message:'Image optimization failed');}
  finally{setOptimizing(false);}
 };
 const upload=async(e:React.FormEvent)=>{
  e.preventDefault();if(busy)return;
  setBusy(true);setError('');setNotice('');
  try{
   await adminGallery.upload(fetch,{category,occasion,title,file,tier});
   setFile(null);setOriginalSize(null);setTitle('');
   const picker=document.getElementById('asset-file') as HTMLInputElement|null;
   if(picker)picker.value='';
   setNotice('Image uploaded as a draft. Publish it when ready.');
   await refresh();
  }catch(e){setError(e instanceof Error?e.message:'Upload failed');}
  finally{setBusy(false);}
 };
 const toggle=async(asset:Asset)=>{
  if(busy)return;setBusy(true);setError('');setNotice('');
  try{
   await adminGallery.setPublished(fetch,asset.id,!asset.published);
   setNotice(`${asset.title} ${asset.published?'unpublished':'published'}.`);
   await refresh();
  }catch(e){setError(e instanceof Error?e.message:'Update failed');}
  finally{setBusy(false);}
 };
 const visible=assets.filter(a=>(filter==='ALL'||a.occasion===filter)&&(status==='ALL'||(status==='PUBLISHED'?a.published:!a.published)));
 return <main style={{maxWidth:1160,margin:'0 auto',padding:'32px 20px',fontFamily:'system-ui,sans-serif',color:'#202536'}}>
  <header style={{marginBottom:24}}>
   <a href="/admin" style={{color:'#6353b5',textDecoration:'none'}}>{localize("ui.12ee0846b8b35e07",{defaultValue:"← Admin dashboard"})}</a>
   <h1 style={{fontSize:30,margin:'14px 0 6px'}}>{localize("ui.f2b0278a0236c008",{defaultValue:"Invitation asset gallery"})}</h1>
   <p style={{color:'#636b7a'}}>{localize("ui.a028ddc2ef95a74b",{defaultValue:"Platform administrator workspace · Upload backgrounds and decorations as drafts, then publish them for hosts."})}</p>
  </header>
  {error&&<div role="alert" style={{background:'#fff0f0',border:'1px solid #e6a5a5',borderRadius:12,padding:14,marginBottom:16}}>{localizeText(error)}</div>}
  {notice&&<div role="status" style={{background:'#effbf4',border:'1px solid #b5dfc5',borderRadius:12,padding:14,marginBottom:16}}>{localizeText(notice)}</div>}
  <section style={{border:'1px solid #dedde8',borderRadius:18,padding:22,background:'#fff',marginBottom:28}}>
   <details open={focused?undefined:true}><summary>{localize("ui.0602addc1ce42a9d",{defaultValue:"Upload new asset"})}</summary>
   <form onSubmit={upload}>
    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(210px,1fr))',gap:14}}>
     <label>{localize("ui.292c06f0045a45d0",{defaultValue:"Category"})}<select value={category} onChange={e=>{setCategory(e.target.value);setFile(null);setOriginalSize(null);const picker=document.getElementById('asset-file') as HTMLInputElement|null;if(picker)picker.value='';}} style={field}>{categories.map(v=><option key={v} value={v}>{v==='BACKGROUND'?localize("ui.ea2b8a878841df59",{defaultValue:"Background"}):localize("ui.6cb4595dde03af43",{defaultValue:"Decoration / sticker"})}</option>)}</select></label>
     <label>{localizeText('Template access')}<select value={tier} onChange={e=>setTier(e.target.value)} style={field}><option value="FREE">{localizeText('Free for all hosts')}</option><option value="PREMIUM">{localizeText('Premium · paid access')}</option></select></label><label>{localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})}<select value={occasion} onChange={e=>setOccasion(e.target.value)} style={field}>{occasions.map(v=><option key={v}>{v}</option>)}</select></label>
     <label>{localize("ui.7e8cd2056da73a7f",{defaultValue:"Title"})}<input required maxLength={120} value={title} onChange={e=>setTitle(e.target.value)} placeholder={localize("ui.e44bac80f981cc2d",{defaultValue:"e.g. Golden confetti"})} style={field}/></label>
     <label>{localize("ui.799bde27d87f6cd8",{defaultValue:"Image (automatically optimized to 2 MB)"})}<input id="asset-file" required type="file" accept="image/png,image/jpeg,image/webp" onChange={e=>void selectImage(e.target.files?.[0]??null)} style={field}/></label>
    </div>
    {optimizing&&<p role="status">{localize("ui.9d9d9b8f3296cca0",{defaultValue:"Optimizing image…"})}</p>}
    {file&&originalSize!==null&&<p role="status">{localize("ui.65cae0ad8637bf00",{defaultValue:"Ready to upload: "})}{(originalSize/1048576).toFixed(2)} {localize("ui.801b70ed64ae7623",{defaultValue:" MB → "})}{(file.size/1048576).toFixed(2)} {localize("ui.51190257ff1142ac",{defaultValue:" MB ("})}{file.type==='image/png'?localize("ui.796120837694d3f3",{defaultValue:"PNG"}):localize("ui.f4dc1c96a30fbdeb",{defaultValue:"JPEG"})})</p>}
    {preview&&<div style={{marginTop:16}}><img src={preview} alt={localize("ui.5a7c924a7f82e4c2",{defaultValue:"Selected image preview"})} style={{maxWidth:230,maxHeight:180,objectFit:'contain',borderRadius:12,background:'#f1f0f5'}}/></div>}
    <p style={{fontSize:13,color:'#6b7280'}}>{localize("ui.36ecee8126719342",{defaultValue:"PNG and JPEG are supported; WebP requires server image-decoder support. Images are processed into PNG by the backend."})}</p>
    <button disabled={busy||optimizing||!file} type="submit" style={primary}>{busy?localize("ui.5474eef8d0f179c7",{defaultValue:"Working…"}):localize("ui.b79a6ae3e54c1ad7",{defaultValue:"Upload as draft"})}</button>
   </form></details>
  </section>
  <p style={{padding:'14px 18px',background:'#f6eff7',border:'1px solid #e6d7e9',borderRadius:12,color:'#79516e'}}>{localizeText('You control the collection: upload a background or decoration, choose Free or Premium, then publish it. Premium previews carry a GiftWise watermark; clean artwork requires access.')}</p><section>
   <div style={{display:'flex',flexWrap:'wrap',gap:12,alignItems:'end',justifyContent:'space-between',marginBottom:16}}>
    <h2 style={{margin:0,fontSize:22}}>{localize("ui.d13b051b078e405c",{defaultValue:"Assets ("})}{visible.length})</h2>
    <div style={{display:'flex',flexWrap:'wrap',gap:10}}>
     <label>{localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})}<select value={filter} onChange={e=>setFilter(e.target.value)} style={field}><option value="ALL">{localize("ui.f2ffa526bb1a2c3b",{defaultValue:"All occasions"})}</option>{occasions.map(v=><option key={v}>{v}</option>)}</select></label>
     <label>{localize("ui.920e413c7d411b61",{defaultValue:"Status"})}<select value={status} onChange={e=>setStatus(e.target.value)} style={field}><option value="ALL">{localize("ui.8ee57323a6f24cc7",{defaultValue:"All statuses"})}</option><option value="PUBLISHED">{localize("ui.2ef42ec21d9300dd",{defaultValue:"Published"})}</option><option value="DRAFT">{localize("ui.ebf12ef47cf575b3",{defaultValue:"Draft"})}</option></select></label>
    </div>
   </div>
   {loading?<p>{localize("ui.0ddf07e8eb349eb1",{defaultValue:"Loading gallery…"})}</p>:visible.length===0?<p>{localize("ui.0c1088af22312bde",{defaultValue:"No matching assets yet."})}</p>:
    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(220px,1fr))',gap:16}}>
     {visible.map(a=><article key={a.id} style={{border:'1px solid #dedde8',borderRadius:16,overflow:'hidden',background:'#fff'}}>
      <div style={{height:180,background:'#f2f0f8',display:'flex',alignItems:'center',justifyContent:'center'}}>
       <img src={assetImageUrl(a.id)} alt={a.title} style={{maxHeight:'100%',maxWidth:'100%',objectFit:'contain'}}/>
      </div>
      <div style={{padding:14}}>
       <strong style={{display:'block',overflowWrap:'anywhere'}}>{a.title}</strong>
       <div style={{fontSize:12,color:'#656d7b',margin:'6px 0'}}>{a.category} · {a.occasion} · {a.width} × {a.height}</div>
       <div style={{fontSize:13,marginBottom:12}}>{a.published?localize("ui.13137594e55313af",{defaultValue:"● Published"}):localize("ui.607aed04bb4bebc5",{defaultValue:"○ Draft / unpublished"})}</div>
       <label>{localize("ui.3f7328c9b281ce5a",{defaultValue:"Access tier"})}<select disabled={busy} value={a.tier||'FREE'} onChange={async e=>{setBusy(true);setError('');try{const saved=await commercialApi<Asset>(`/api/admin/invitation-assets/${a.id}/tier`,{tier:e.target.value},'PUT');setAssets(rows=>rows.map(x=>x.id===a.id?saved:x));setNotice('Asset access tier saved and audited.')}catch(e){setError(String(e))}finally{setBusy(false)}}}><option value="FREE">{localize("ui.f411a1fb62758b4c",{defaultValue:"Free"})}</option><option value="PREMIUM">{localize("ui.de88c121a82a0635",{defaultValue:"Premium"})}</option></select></label>
       <button disabled={busy} onClick={()=>void toggle(a)} style={{...primary,width:'100%',background:a.published?'#fff':'#5d4bb0',color:a.published?'#45368c':'white',border:'1px solid #5d4bb0'}}>{a.published?localize("ui.2db04a54fa743150",{defaultValue:"Unpublish"}):localize("ui.859390eb495b2ead",{defaultValue:"Publish"})}</button>
      </div>
     </article>)}
    </div>}
  </section>
 </main>;
}
const field:React.CSSProperties={display:'block',marginTop:7,width:'100%',minHeight:39,padding:'8px 10px',border:'1px solid #cfcddd',borderRadius:9,background:'#fff',fontSize:14};
const primary:React.CSSProperties={padding:'11px 18px',borderRadius:10,background:'#5d4bb0',color:'#fff',border:0,cursor:'pointer',fontWeight:600};
