'use client';
import {useEffect,useId,useRef,useState} from 'react';
import './gift-description.css';
export default function GiftDescription({name,description}:{name:string;description:string|null}){
 const paragraph=useRef<HTMLParagraphElement>(null),dialog=useRef<HTMLDialogElement>(null),title=useId();
 const [truncated,setTruncated]=useState(false);
 useEffect(()=>{const element=paragraph.current;if(!element)return;const check=()=>setTruncated(element.scrollHeight>element.clientHeight+1);check();const observer=new ResizeObserver(check);observer.observe(element);return()=>observer.disconnect()},[description]);
 if(!description)return null;
 return <div className="giftDescription"><p ref={paragraph} className="giftDescriptionPreview">{description}</p>{truncated&&<button type="button" className="giftDescriptionMore" onClick={()=>dialog.current?.showModal()}>View full description</button>}<dialog className="giftDescriptionDialog" ref={dialog} aria-labelledby={title} onClick={e=>{if(e.target===dialog.current)dialog.current?.close()}}><div><header><h2 id={title}>{name}</h2><button type="button" aria-label="Close description" onClick={()=>dialog.current?.close()}>Close ×</button></header><p>{description}</p></div></dialog></div>;
}
