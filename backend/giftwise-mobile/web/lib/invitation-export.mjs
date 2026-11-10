import {beginAction,completeAction} from './interaction-analytics.mjs';

import {validateSafeLayout,QR_MARGIN} from './invitation-geometry.mjs';
import {backgroundImageRect} from './background-position.mjs';
import {validStickerElement} from './invitation-stickers.mjs';

export const FONT_CHOICES = Object.freeze([
  {label:'Classic Serif',value:'Georgia, serif'},
  {label:'Modern Sans',value:'Arial, sans-serif'},
  {label:'Elegant Times',value:'"Times New Roman", serif'},
  {label:'Bookish Palatino',value:'Palatino, "Palatino Linotype", serif'},
  {label:'Refined Baskerville',value:'Baskerville, Georgia, serif'},
  {label:'Editorial Didot',value:'Didot, "Times New Roman", serif'},
  {label:'Stylish Garamond',value:'Garamond, Georgia, serif'},
  {label:'Handwritten Casual',value:'"Comic Sans MS", cursive'},
  {label:'Script Brush',value:'"Brush Script MT", cursive'},
  {label:'Script Lucida',value:'"Lucida Handwriting", cursive'},
  {label:'Monospace',value:'"Courier New", monospace'},
  {label:'Trebuchet',value:'"Trebuchet MS", sans-serif'}
]);

export function safeFont(value){
  return FONT_CHOICES.some(f=>f.value===value)
    ? value
    : FONT_CHOICES[0].value;
}

export function wrapLines(ctx,text,maxWidth){
  const out=[];
  for(const paragraph of String(text??'').split('\n')){
    if(!paragraph.trim()){
      out.push('');
      continue;
    }
    let line='';
    for(const word of paragraph.split(/\s+/)){
      const candidate=line?`${line} ${word}`:word;
      if(line&&ctx.measureText(candidate).width>maxWidth){
        out.push(line);
        line=word;
      }else{
        line=candidate;
      }
    }
    out.push(line);
  }
  return out;
}

export function validateExport(design,qrUrl){
  if(
    !design||
    design.version!==1||
    design.width!==600||
    design.height!==800||
    !Array.isArray(design.elements)||
    design.elements.length>100
  ){
    throw Error('Invalid invitation layout');
  }

  if(
    design.elements.filter(e=>e.id==='qr'&&e.type==='qr').length!==1
  ){
    throw Error('Invitation requires exactly one QR');
  }

  if(
    typeof qrUrl!=='string'||
    !qrUrl.startsWith('blob:')
  ){
    throw Error('Authenticated QR required before download');
  }

  for(const el of design.elements){
    if(
      !['text','qr','sticker'].includes(el.type)||
      (el.type==='sticker'&&!validStickerElement(el))||
      !['x','y','w','h'].every(k=>Number.isFinite(el[k]))||
      el.w<=0||
      el.h<=0
    ){
      throw Error('Invalid invitation element');
    }
  }
  if(!validateSafeLayout(design))throw Error('Invitation overlaps the protected QR area or has unsafe dimensions');
  return true;
}

function loadImage(src){
  return new Promise((resolve,reject)=>{
    const img=new Image();
    img.onload=()=>resolve(img);
    img.onerror=()=>reject(
      Error('Could not load invitation image')
    );
    img.src=src;
  });
}

export async function renderInvitationPng(design,qrUrl){
  validateExport(design,qrUrl);

  if(typeof document==='undefined'){
    throw Error('PNG export requires a browser');
  }

  const canvas=document.createElement('canvas');
  canvas.width=600;
  canvas.height=800;

  const ctx=canvas.getContext('2d');
  if(!ctx)throw Error('Canvas unavailable');

  ctx.fillStyle=design.background||'#fff8ef';
  ctx.fillRect(0,0,600,800);

  if(design.backgroundImage){
    const bg=await loadImage(design.backgroundImage);
    const rect=backgroundImageRect(
      bg.width,bg.height,600,800,design.backgroundTransform
    );
    ctx.drawImage(bg,rect.x,rect.y,rect.width,rect.height);

    ctx.fillStyle=`rgba(255,255,255,${Number.isFinite(design.backgroundOverlay)?Math.max(0,Math.min(1,design.backgroundOverlay)):.53})`;
    ctx.fillRect(0,0,600,800);
  }

  const qr=await loadImage(qrUrl);

  for(const el of design.elements){
    if(el.type==='sticker'){
      const sticker=await loadImage(el.src);
      // Match the editor's object-fit: contain behavior.
      const imageRatio = sticker.width / sticker.height;
      const boxRatio = el.w / el.h;
      const drawW = imageRatio > boxRatio ? el.w : el.h * imageRatio;
      const drawH = imageRatio > boxRatio ? el.w / imageRatio : el.h;
      const drawX = el.x + (el.w - drawW) / 2;
      const drawY = el.y + (el.h - drawH) / 2;
      ctx.drawImage(sticker,drawX,drawY,drawW,drawH);
      continue;
    }
    if(el.type==='qr'){
      ctx.fillStyle='#ffffff';
      ctx.fillRect(
        el.x-QR_MARGIN,
        el.y-QR_MARGIN,
        el.w+2*QR_MARGIN,
        el.h+2*QR_MARGIN
      );

      // Preview QR has an 8px white border inside the element box.
      // Keep that inner border, while retaining the outer quiet-zone fill.
      const inner=8;
      ctx.imageSmoothingEnabled=false;
      ctx.drawImage(qr,el.x+inner,el.y+inner,el.w-2*inner,el.h-2*inner);
      ctx.imageSmoothingEnabled=true;
      continue;
    }

    const size=Math.min(
      100,
      Math.max(8,Number(el.fontSize)||20)
    );

    const weight=Number(el.fontWeight)>=600
      ? 'bold'
      : 'normal';

    ctx.font=`${weight} ${size}px ${safeFont(el.fontFamily)}`;

    ctx.fillStyle=/^#[0-9a-f]{6}$/i.test(el.color)
      ? el.color
      : '#222222';

    ctx.textAlign=['left','center','right'].includes(el.align)
      ? el.align
      : 'center';

    ctx.textBaseline='top';

    const x=ctx.textAlign==='left'
      ? el.x
      : ctx.textAlign==='right'
        ? el.x+el.w
        : el.x+el.w/2;

    // Preview uses display:flex, align-items:center, justify-content:center,
    // padding:3px, white-space:pre-wrap and line-height:1.2.
    // Keep export within the same inner content box and vertically center lines.
    // Match the editor's flex-centered text layout. The editor allows text
    // to overflow a short element; clipping here cut off the first line.
    const inset=3;
    const innerWidth=Math.max(1,el.w-inset*2);
    const lineHeight=size*1.2;
    const lines=wrapLines(ctx,el.text,innerWidth);
    const textX=ctx.textAlign==='left'?el.x+inset
      :ctx.textAlign==='right'?el.x+el.w-inset
      :el.x+el.w/2;
    const firstLineCenter=el.y+el.h/2-(lines.length-1)*lineHeight/2;

    ctx.save();
    ctx.textBaseline='middle';
    // Do not clip to el.h: CSS flex items may extend outside their box.
    lines.forEach((line,i)=>ctx.fillText(line,textX,firstLineCenter+i*lineHeight));
    ctx.restore();
  }

  return new Promise((resolve,reject)=>{
    canvas.toBlob(
      blob=>blob
        ? resolve(blob)
        : reject(Error('Could not encode PNG')),
      'image/png'
    );
  });
}

export async function downloadInvitationPng(
  design,
  qrUrl,
  filename='gift-invitation.png'
){
  const interaction=beginAction('INVITATION_DOWNLOAD');
  try{
  const blob=await renderInvitationPng(design,qrUrl);
  const url=URL.createObjectURL(blob);

  try{
    const a=document.createElement('a');
    a.href=url;
    a.download=filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    completeAction(interaction,'BROWSER_READY','PNG_HANDED_TO_BROWSER');
  }finally{
    setTimeout(()=>URL.revokeObjectURL(url),1500);
  }
  }catch(error){completeAction(interaction,'FAILURE','INVITATION_RENDER_OR_DOWNLOAD_FAILED');throw error;}
}
