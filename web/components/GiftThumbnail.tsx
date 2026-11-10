'use client';
import {giftThumbnail} from '../lib/growth.mjs';
export default function GiftThumbnail({src,alt}:{src:string;alt:string}){return <img src={giftThumbnail(src)} alt={alt} width={480} height={480} loading="lazy" decoding="async" style={{width:'100%',height:'100%',objectFit:'contain'}}/>}
