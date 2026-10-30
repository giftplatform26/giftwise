'use client';

import {useCallback,useEffect,useState} from 'react';
import {useExperience} from '../../../components/ExperienceProvider';
import {adminGallery,assetImageUrl,categories,occasions} from '../../../lib/admin-gallery.mjs';
import {optimizeGalleryImage} from '../../../lib/gallery-image-optimizer.mjs';

type Asset={id:string;category:string;occasion:string;title:string;published:boolean;width:number;height:number};

export default function AdminInvitationAssetsPage(){
 const {mode}=useExperience();const focused=mode==='FOCUSED';
 const [assets,setAssets]=useState<Asset[]>([]);
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState('');
 const [notice,setNotice]=useState('');
 const [busy,setBusy]=useState(false);
 const [category,setCategory]=useState('BACKGROUND');
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
   await adminGallery.upload(fetch,{category,occasion,title,file});
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
   <a href="/admin" style={{color:'#6353b5',textDecoration:'none'}}>← Admin dashboard</a>
   <h1 style={{fontSize:30,margin:'14px 0 6px'}}>Invitation asset gallery</h1>
   <p style={{color:'#636b7a'}}>Platform administrator workspace · Upload backgrounds and decorations as drafts, then publish them for hosts.</p>
  </header>
  {error&&<div role="alert" style={{background:'#fff0f0',border:'1px solid #e6a5a5',borderRadius:12,padding:14,marginBottom:16}}>{error}</div>}
  {notice&&<div role="status" style={{background:'#effbf4',border:'1px solid #b5dfc5',borderRadius:12,padding:14,marginBottom:16}}>{notice}</div>}
  <section style={{border:'1px solid #dedde8',borderRadius:18,padding:22,background:'#fff',marginBottom:28}}>
   <details open={focused?undefined:true}><summary>Upload new asset</summary>
   <form onSubmit={upload}>
    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(210px,1fr))',gap:14}}>
     <label>Category<select value={category} onChange={e=>{setCategory(e.target.value);setFile(null);setOriginalSize(null);const picker=document.getElementById('asset-file') as HTMLInputElement|null;if(picker)picker.value='';}} style={field}>{categories.map(v=><option key={v} value={v}>{v==='BACKGROUND'?'Background':'Decoration / sticker'}</option>)}</select></label>
     <label>Occasion<select value={occasion} onChange={e=>setOccasion(e.target.value)} style={field}>{occasions.map(v=><option key={v}>{v}</option>)}</select></label>
     <label>Title<input required maxLength={120} value={title} onChange={e=>setTitle(e.target.value)} placeholder="e.g. Golden confetti" style={field}/></label>
     <label>Image (automatically optimized to 2 MB)<input id="asset-file" required type="file" accept="image/png,image/jpeg,image/webp" onChange={e=>void selectImage(e.target.files?.[0]??null)} style={field}/></label>
    </div>
    {optimizing&&<p role="status">Optimizing image…</p>}
    {file&&originalSize!==null&&<p role="status">Ready to upload: {(originalSize/1048576).toFixed(2)} MB → {(file.size/1048576).toFixed(2)} MB ({file.type==='image/png'?'PNG':'JPEG'})</p>}
    {preview&&<div style={{marginTop:16}}><img src={preview} alt="Selected image preview" style={{maxWidth:230,maxHeight:180,objectFit:'contain',borderRadius:12,background:'#f1f0f5'}}/></div>}
    <p style={{fontSize:13,color:'#6b7280'}}>PNG and JPEG are supported; WebP requires server image-decoder support. Images are processed into PNG by the backend.</p>
    <button disabled={busy||optimizing||!file} type="submit" style={primary}>{busy?'Working…':'Upload as draft'}</button>
   </form></details>
  </section>
  <section>
   <div style={{display:'flex',flexWrap:'wrap',gap:12,alignItems:'end',justifyContent:'space-between',marginBottom:16}}>
    <h2 style={{margin:0,fontSize:22}}>Assets ({visible.length})</h2>
    <div style={{display:'flex',flexWrap:'wrap',gap:10}}>
     <label>Occasion<select value={filter} onChange={e=>setFilter(e.target.value)} style={field}><option value="ALL">All occasions</option>{occasions.map(v=><option key={v}>{v}</option>)}</select></label>
     <label>Status<select value={status} onChange={e=>setStatus(e.target.value)} style={field}><option value="ALL">All statuses</option><option value="PUBLISHED">Published</option><option value="DRAFT">Draft</option></select></label>
    </div>
   </div>
   {loading?<p>Loading gallery…</p>:visible.length===0?<p>No matching assets yet.</p>:
    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(220px,1fr))',gap:16}}>
     {visible.map(a=><article key={a.id} style={{border:'1px solid #dedde8',borderRadius:16,overflow:'hidden',background:'#fff'}}>
      <div style={{height:180,background:'#f2f0f8',display:'flex',alignItems:'center',justifyContent:'center'}}>
       <img src={assetImageUrl(a.id)} alt={a.title} style={{maxHeight:'100%',maxWidth:'100%',objectFit:'contain'}}/>
      </div>
      <div style={{padding:14}}>
       <strong style={{display:'block',overflowWrap:'anywhere'}}>{a.title}</strong>
       <div style={{fontSize:12,color:'#656d7b',margin:'6px 0'}}>{a.category} · {a.occasion} · {a.width} × {a.height}</div>
       <div style={{fontSize:13,marginBottom:12}}>{a.published?'● Published':'○ Draft / unpublished'}</div>
       <button disabled={busy} onClick={()=>void toggle(a)} style={{...primary,width:'100%',background:a.published?'#fff':'#5d4bb0',color:a.published?'#45368c':'white',border:'1px solid #5d4bb0'}}>{a.published?'Unpublish':'Publish'}</button>
      </div>
     </article>)}
    </div>}
  </section>
 </main>;
}
const field:React.CSSProperties={display:'block',marginTop:7,width:'100%',minHeight:39,padding:'8px 10px',border:'1px solid #cfcddd',borderRadius:9,background:'#fff',fontSize:14};
const primary:React.CSSProperties={padding:'11px 18px',borderRadius:10,background:'#5d4bb0',color:'#fff',border:0,cursor:'pointer',fontWeight:600};
