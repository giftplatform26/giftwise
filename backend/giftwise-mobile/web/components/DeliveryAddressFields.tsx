'use client';
/* localization:instrumented */
import {localize,localizeText} from '../lib/localization.mjs';
import {useLocalizationRender} from './LocalizationProvider';

export type DeliveryAddress={addressLine1:string;city:string;state:string;postalCode:string;country:string};
export const emptyDeliveryAddress:DeliveryAddress={addressLine1:'',city:'',state:'',postalCode:'',country:'India'};
export default function DeliveryAddressFields({same,address,onSameChange,onAddressChange}:{same:boolean;address:DeliveryAddress;onSameChange:(same:boolean)=>void;onAddressChange:(address:DeliveryAddress)=>void}){useLocalizationRender();
 return <fieldset><legend>{localize("ui.031e33813f5d609b",{defaultValue:"Gift delivery address"})}</legend><label style={{display:'flex',alignItems:'center',gap:10}}><input type="checkbox" checked={same} onChange={e=>onSameChange(e.target.checked)} style={{width:'auto'}}/>{localize("ui.455df35dd66a82ca",{defaultValue:"Same as event address"})}</label><p className="finePrint">{localize("ui.8c2ecd949547e931",{defaultValue:"The guest managing a selected gift can view this address to arrange delivery."})}</p>{!same&&<>{([['addressLine1','Street address',200,'street-address'],['city','City',120,'address-level2'],['state','State',120,'address-level1'],['postalCode','PIN / postal code',20,'postal-code'],['country','Country',80,'country-name']] as const).map(([key,label,maxLength,autoComplete])=><label key={key}>{localizeText(label)}<input required maxLength={maxLength} autoComplete={`section-delivery shipping ${autoComplete}`} value={address[key]} onChange={e=>onAddressChange({...address,[key]:e.target.value})}/></label>)}</>}</fieldset>;
}
