export function giftPhotoUrl(eventId,key){
 if(!/^[0-9a-f-]{36}$/i.test(eventId||'')||!/^gift-photo:[0-9a-f-]{36}$/i.test(key||''))return '';
 return `/backend/api/v1/events/${eventId}/gifts/photos/${key.slice(11)}`;
}
export function validateGiftPhotoFile(file){
 if(!file||!['image/jpeg','image/png','image/webp'].includes(file.type))throw Error('Choose a JPEG, PNG or WebP photo.');
 if(file.size<1||file.size>20_000_000)throw Error('Choose a photo under 20 MB. It will be compressed before upload.');
 return file;
}
export function giftPhotoDimensions(width,height){
 if(!Number.isFinite(width)||!Number.isFinite(height)||width<160||height<160)throw Error('Choose a clear photo at least 160 × 160 pixels.');
 if(width*height>40_000_000)throw Error('This photo is too large to process. Choose one under 40 megapixels.');
 const scale=Math.min(1,1200/Math.max(width,height));
 const size={width:Math.round(width*scale),height:Math.round(height*scale)};
 if(Math.min(size.width,size.height)<160)throw Error('Photo is too narrow. Use a less panoramic image.');
 return size;
}
async function decodePhoto(file){
 if(typeof createImageBitmap==='function')return createImageBitmap(file,{imageOrientation:'from-image'});
 const url=URL.createObjectURL(file);
 try{return await new Promise((resolve,reject)=>{const img=new Image();img.onload=()=>resolve(img);img.onerror=()=>reject(Error('This photo could not be read. Try a JPEG or PNG.'));img.src=url;});}finally{URL.revokeObjectURL(url);}
}
export async function prepareGiftPhoto(file){
 validateGiftPhotoFile(file);
 let image;
 try{image=await decodePhoto(file);}catch{throw Error('This photo could not be read. Try a JPEG or PNG.');}
 try{
  const size=giftPhotoDimensions(image.width,image.height);
  const canvas=document.createElement('canvas');canvas.width=size.width;canvas.height=size.height;
  const context=canvas.getContext('2d');if(!context)throw Error('Your browser could not prepare this photo. Try another browser.');
  context.fillStyle='#ffffff';context.fillRect(0,0,size.width,size.height);
  context.drawImage(image,0,0,size.width,size.height);
  for(const quality of [.84,.72,.6,.48]){
   const blob=await new Promise(resolve=>canvas.toBlob(resolve,'image/jpeg',quality));
   if(blob&&blob.size>0&&blob.size<=2_000_000)return new File([blob],`${file.name.replace(/\.[^.]+$/,'')||'gift-photo'}.jpg`,{type:'image/jpeg'});
  }
  throw Error('This photo could not be compressed enough. Choose a smaller image.');
 }finally{image?.close?.();}
}
