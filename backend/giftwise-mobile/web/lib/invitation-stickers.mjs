import {
  platformDecorationId,
  validPlatformDecorationElement
} from './platform-decorations.mjs';

export const STICKERS = Object.freeze([
  {id:'rose-spray',name:'Rose spray',category:'Floral'},
  {id:'lotus',name:'Lotus',category:'Floral'},
  {id:'peacock-feather',name:'Peacock feather',category:'Botanical'},
  {id:'lace-medallion',name:'Lace medallion',category:'Frames'},
  {id:'wedding-bells',name:'Wedding bells',category:'Love'},
  {id:'love-doves',name:'Love doves',category:'Love'},
  {id:'cupcake',name:'Cupcake',category:'Celebration'},
  {id:'party-hat',name:'Party hat',category:'Celebration'},
  {id:'baby-carriage',name:'Baby carriage',category:'Celebration'},
  {id:'festive-lantern',name:'Festive lantern',category:'Celebration'},
  {id:'music-notes',name:'Music notes',category:'Celebration'},
  {id:'magnolia',name:'Magnolia',category:'Floral'},
  {id:'golden-leaf',name:'Golden leaf',category:'Botanical'},
  {id:'floral-corner',name:'Floral corner',category:'Frames'},
  {id:'mandala-star',name:'Mandala star',category:'Frames'},
  {id:'silk-banner',name:'Silk banner',category:'Frames'},

  {id:'butterfly',name:'Butterfly',category:'Love'},
  {id:'floral-wreath',name:'Floral wreath',category:'Floral'},
  {id:'cherry-blossom',name:'Cherry blossom',category:'Floral'},
  {id:'golden-sparkle',name:'Golden sparkle',category:'Celebration'},
  {id:'ribbon-bow',name:'Ribbon bow',category:'Love'},
  {id:'moon-stars',name:'Moon & stars',category:'Celebration'},
  {id:'fern-sprig',name:'Fern sprig',category:'Botanical'},
  {id:'vintage-frame',name:'Vintage frame',category:'Frames'},
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
export function validStickerElement(el){return el?.type==='sticker' && typeof el.stickerId==='string' && (stickerById(el.stickerId)?.src===el.src || validPlatformDecorationElement(el) || /^upload:[a-f0-9-]{36}$/i.test(el.stickerId)&&el.src===`/backend/api/v1/invitation-uploads/${el.stickerId.slice(7)}/image`)}
export function addSticker(design,id,authenticated=false){
  const sticker=stickerById(id) || (/^upload:[a-f0-9-]{36}$/i.test(id)?{id,src:`/backend/api/v1/invitation-uploads/${id.slice(7)}/image`}:null) ||
    (typeof id==='string' && id.startsWith('platform:')
      ? (() => {
          const assetId=id.slice('platform:'.length);
          try {
            if(platformDecorationId(assetId)!==id)return null;
            return {
              id,
              src:authenticated?`/backend/api/v1/commerce/templates/${assetId}/image`:`/backend/api/public/invitation-assets/${assetId}/image`
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
