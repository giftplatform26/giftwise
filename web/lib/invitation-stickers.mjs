import {
  platformDecorationId,
  validPlatformDecorationElement
} from './platform-decorations.mjs';

export const STICKERS = Object.freeze([
  {id:'flower',name:'Flower',category:'Floral'},
  {id:'bouquet',name:'Bouquet',category:'Floral'},
  {id:'leaf',name:'Leaf',category:'Floral'},
  {id:'balloon',name:'Balloon',category:'Celebration'},
  {id:'balloons',name:'Balloons',category:'Celebration'},
  {id:'ribbon',name:'Ribbon',category:'Celebration'},
  {id:'heart',name:'Heart',category:'Love'},
  {id:'rings',name:'Rings',category:'Love'},
  {id:'star',name:'Star',category:'Celebration'},
  {id:'confetti',name:'Confetti',category:'Celebration'},
  {id:'giftbox',name:'Gift box',category:'Celebration'},
  {id:'crown',name:'Crown',category:'Celebration'}
].map(s=>Object.freeze({...s,src:`/invitation-stickers/${s.id}.svg`})));
export function stickerById(id){return STICKERS.find(s=>s.id===id)}
export function validStickerElement(el){return el?.type==='sticker' && typeof el.stickerId==='string' && (stickerById(el.stickerId)?.src===el.src || validPlatformDecorationElement(el))}
export function addSticker(design,id){
  const sticker=stickerById(id) ||
    (typeof id==='string' && id.startsWith('platform:')
      ? (() => {
          const assetId=id.slice('platform:'.length);
          try {
            if(platformDecorationId(assetId)!==id)return null;
            return {
              id,
              src:`/backend/api/public/invitation-assets/${assetId}/image`
            };
          } catch {
            return null;
          }
        })()
      : null);
  if(!sticker || !Array.isArray(design?.elements) || design.elements.length>=100)return design;
  for(let y=12;y<=design.height-92;y+=20){
    for(let x=12;x<=design.width-92;x+=20){
      const el={id:`sticker-${globalThis.crypto?.randomUUID?.()??Math.random().toString(36).slice(2)}`,type:'sticker',stickerId:id,src:sticker.src,text:'',x,y,w:80,h:80,fontSize:20,color:'#000000',fontWeight:400,align:'center'};
      const next={...design,elements:[...design.elements,el]};
      // import locally to avoid cycles: geometry is independent of stickers
      if(validCandidate(next))return next;
    }
  }
  return design;
}
import {validateSafeLayout as validCandidate} from './invitation-geometry.mjs';
