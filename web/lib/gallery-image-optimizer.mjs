export const MAX_UPLOAD_BYTES = 2 * 1024 * 1024;
export const MAX_SIDE = 4096;
export const MAX_PIXELS = 12_000_000;

export function fittedDimensions(width, height, maxSide=MAX_SIDE, maxPixels=MAX_PIXELS) {
 if(!Number.isFinite(width)||!Number.isFinite(height)||width<=0||height<=0)throw Error('Invalid image dimensions');
 const scale=Math.min(1,maxSide/width,maxSide/height,Math.sqrt(maxPixels/(width*height)));
 return {width:Math.max(1,Math.floor(width*scale)),height:Math.max(1,Math.floor(height*scale))};
}

export async function optimizeGalleryImage(file,category) {
 if(!file || !['image/png','image/jpeg','image/webp'].includes(file.type))throw Error('Choose a PNG, JPEG or WebP image');
 if(!['BACKGROUND','DECORATION'].includes(category))throw Error('Invalid image category');
 const url=URL.createObjectURL(file);
 let bitmap;
 try {bitmap=await createImageBitmap(file);}catch {throw Error('This image cannot be decoded by your browser');}
 finally {URL.revokeObjectURL(url);}
 try {
  const fitted=fittedDimensions(bitmap.width,bitmap.height,2400,4_320_000);
  const transparent=category==='DECORATION';
  const mime=transparent?'image/png':'image/jpeg';
  let width=fitted.width,height=fitted.height;
  for(let attempt=0;attempt<9;attempt++){
   const canvas=document.createElement('canvas');canvas.width=width;canvas.height=height;
   const ctx=canvas.getContext('2d');if(!ctx)throw Error('Canvas conversion unavailable');
   if(!transparent){ctx.fillStyle='#ffffff';ctx.fillRect(0,0,width,height);}
   ctx.drawImage(bitmap,0,0,width,height);
   const quality=transparent?undefined:Math.max(.56,.88-attempt*.05);
   const blob=await new Promise((resolve,reject)=>canvas.toBlob(b=>b?resolve(b):reject(Error('Image conversion failed')),mime,quality));
   canvas.width=0;canvas.height=0;
   if(blob.size<=MAX_UPLOAD_BYTES){
    const name=file.name.replace(/\.[^.]+$/,'')+(transparent?'.png':'.jpg');
    return new File([blob],name,{type:mime,lastModified:Date.now()});
   }
   const ratio=.78;
   width=Math.max(32,Math.floor(width*ratio));height=Math.max(32,Math.floor(height*ratio));
  }
  throw Error('Could not reduce this image below 2 MB without losing too much detail');
 }finally{bitmap.close?.();}
}
