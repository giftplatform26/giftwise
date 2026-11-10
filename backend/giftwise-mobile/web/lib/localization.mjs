import {createInstance} from 'i18next';
import {initReactI18next} from 'react-i18next';
export const localization=createInstance();
void localization.use(initReactI18next).init({lng:'en',fallbackLng:['en'],resources:{en:{translation:{}}},initAsync:false,keySeparator:false,nsSeparator:false,returnNull:false,returnEmptyString:false,interpolation:{escapeValue:false},react:{useSuspense:false}});
let templates=[];
/** @param {string} key @param {Record<string, any>} [options] @returns {string} */
export function localize(key,options={}){return String(localization.t(key,options));}
/** Translate only known UI/message templates; callers must not pass user-authored names/content. */
export function localizeText(text){
 if(typeof text!=='string')return text;
 if(localization.exists(text))return localize(text,{defaultValue:text});
 for(const item of templates){const match=item.pattern.exec(text);if(match){const values={defaultValue:text};item.names.forEach((name,i)=>{values[name]=match[i+1]});return localize(item.key,values)}}
 return text;
}
export function activeLocale(fallback='en-IN'){const code=localization.resolvedLanguage||localization.language||'en';return code==='en'?fallback:code;}
export function prepareCatalog(catalog){
 const source=catalog.sources||catalog.resources?.en||{};
 for(const code of Object.keys(localization.store.data||{}))localization.removeResourceBundle(code,'translation');
 for(const [code,messages] of Object.entries(catalog.resources||{})){
  const effective=code==='en'?{...source,...messages}:messages;const combined=Object.assign(Object.create(null),effective);for(const [key,value] of Object.entries(effective))if(source[key])combined[source[key]]=value;
  localization.addResourceBundle(code,'translation',combined,false,true);
 }
 localization.options.fallbackLng=[...new Set([catalog.defaultLanguage,catalog.fallbackLanguage,'en'])];
 templates=[];
 for(const [key,text] of Object.entries(source)){
  if(typeof text!=='string'||!text.includes('{{'))continue;const names=[];let cursor=0,pattern='^';const matches=text.matchAll(/\{\{([A-Za-z][A-Za-z0-9_]*)}}/g);
  for(const match of matches){pattern+=text.slice(cursor,match.index).replace(/[.*+?^${}()|[\]\\]/g,'\\$&')+'([\\s\\S]*?)';names.push(match[1]);cursor=match.index+match[0].length;}
  pattern+=text.slice(cursor).replace(/[.*+?^${}()|[\]\\]/g,'\\$&')+'$';templates.push({key,names,pattern:new RegExp(pattern)});
 }
 return localization.changeLanguage(catalog.selectedLanguage);
}
