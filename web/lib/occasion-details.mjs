// Occasion-specific invitation details collected from the host when creating or editing an event.
// Keep the keys in sync with backend OccasionDetails.java (the backend rejects unknown keys).
// Values are short plain text that appear on the invitation and are used by automatic and AI invitation generation.

export const GIFT_MODES=Object.freeze({
 CONTRIBUTION_AND_GIFTS:{label:'Contributions and gifts',hint:'Guests can pick a gift from your wishlist or give any amount toward the celebration.'},
 CONTRIBUTION_ONLY:{label:'Contributions only',hint:'No wishlist. Guests simply give any amount toward the celebration.'}
});
export const DEFAULT_GIFT_MODE='CONTRIBUTION_AND_GIFTS';
export const isContributionOnly=mode=>mode==='CONTRIBUTION_ONLY';

const COMMON=[
 {key:'venueName',label:'Venue name',placeholder:'e.g. Rosewood Gardens',section:'where'},
 {key:'startTime',label:'Start time',placeholder:'e.g. 6:30 PM',section:'where'}
];
export const OCCASION_FIELDS=Object.freeze({
 WEDDING:{heading:'About the wedding',fields:[
  {key:'brideName',label:'Bride’s name',placeholder:'e.g. Anu'},
  {key:'groomName',label:'Groom’s name',placeholder:'e.g. Rahul'},
  {key:'brideFamily',label:'Bride’s parents or family',placeholder:'e.g. D/o Mr & Mrs Thomas',optional:true},
  {key:'groomFamily',label:'Groom’s parents or family',placeholder:'e.g. S/o Mr & Mrs Menon',optional:true},
  {key:'muhurtham',label:'Muhurtham / ceremony time',placeholder:'e.g. 10:30 AM – 11:15 AM',optional:true,section:'where'},
  {key:'receptionDetails',label:'Reception',placeholder:'e.g. 7 PM at the same venue',optional:true,section:'where'}
 ]},
 BIRTHDAY:{heading:'About the birthday',fields:[
  {key:'celebrantName',label:'Whose birthday is it?',placeholder:'e.g. Maya'},
  {key:'age',label:'Turning (age)',placeholder:'e.g. 5',optional:true,inputMode:'numeric',maxLength:3},
  {key:'theme',label:'Party theme',placeholder:'e.g. Under the sea',optional:true}
 ]},
 BABY_SHOWER:{heading:'About the baby shower',fields:[
  {key:'parentNames',label:'Parents-to-be',placeholder:'e.g. Priya & Arjun'},
  {key:'dueMonth',label:'Baby due',placeholder:'e.g. March 2027',optional:true},
  {key:'theme',label:'Theme',placeholder:'e.g. Twinkle twinkle little star',optional:true}
 ]},
 HOUSEWARMING:{heading:'About the housewarming',fields:[
  {key:'familyName',label:'Family name',placeholder:'e.g. The Nair family'},
  {key:'homeName',label:'Home name',placeholder:'e.g. Shanthi Nilayam',optional:true},
  {key:'ceremony',label:'Ceremony',placeholder:'e.g. Griha Pravesh pooja at 7 AM',optional:true}
 ]},
 ANNIVERSARY:{heading:'About the anniversary',fields:[
  {key:'coupleNames',label:'Couple’s names',placeholder:'e.g. Leela & Joseph'},
  {key:'years',label:'Years together',placeholder:'e.g. 25',optional:true,inputMode:'numeric',maxLength:3}
 ]},
 FAREWELL:{heading:'About the farewell',fields:[
  {key:'honoreeName',label:'Who are we saying farewell to?',placeholder:'e.g. Kavya'},
  {key:'organisation',label:'Team or organisation',placeholder:'e.g. Design team',optional:true},
  {key:'nextChapter',label:'Next chapter',placeholder:'e.g. Moving to Berlin',optional:true}
 ]},
 RETIREMENT:{heading:'About the retirement',fields:[
  {key:'honoreeName',label:'Who is retiring?',placeholder:'e.g. Mr Rao'},
  {key:'organisation',label:'Organisation',placeholder:'e.g. State Bank',optional:true},
  {key:'yearsOfService',label:'Years of service',placeholder:'e.g. 34',optional:true,inputMode:'numeric',maxLength:3}
 ]},
 REUNION:{heading:'About the reunion',fields:[
  {key:'groupName',label:'Group or school',placeholder:'e.g. St. Mary’s High School'},
  {key:'batchYear',label:'Batch year',placeholder:'e.g. Class of 2005',optional:true}
 ]},
 OTHER:{heading:'About the celebration',fields:[
  {key:'honoreeName',label:'Who is it for?',placeholder:'e.g. Grandma',optional:true},
  {key:'occasionName',label:'What is the occasion?',placeholder:'e.g. Naming ceremony',optional:true}
 ]}
});
const MAX=200;

/** Fields for an occasion, split into "who" (people) and "where" (time/venue) sections. */
export function occasionFields(occasion){
 const config=OCCASION_FIELDS[occasion]||OCCASION_FIELDS.OTHER;
 const all=[...config.fields,...COMMON].map(f=>({optional:true,section:'who',maxLength:MAX,...f}));
 // Every detail is optional so creating an event never becomes harder than before; names are encouraged, never required.
 return {heading:config.heading,who:all.filter(f=>f.section==='who'),where:all.filter(f=>f.section==='where'),keys:all.map(f=>f.key)};
}

/** Keeps only non-blank, trimmed values that belong to this occasion. */
export function normalizeOccasionDetails(occasion,details){
 const out={};if(!details||typeof details!=='object')return out;
 for(const key of occasionFields(occasion).keys){
  const value=details[key];if(typeof value!=='string')continue;const trimmed=value.trim();if(!trimmed)continue;
  if(trimmed.length>MAX||/[<>\u0000-\u001f]/.test(trimmed))throw Error('Please shorten or simplify the celebration details.');
  out[key]=trimmed;
 }
 return out;
}

const join=(sep,...parts)=>parts.filter(v=>typeof v==='string'&&v.trim()).map(v=>v.trim()).join(sep);

/** The people this invitation is about, e.g. "Anu & Rahul". Empty when not provided. */
export function featuredNames(occasion,d={}){
 switch(occasion){
  case 'WEDDING':return join(' & ',d.brideName,d.groomName);
  case 'BIRTHDAY':return join('',d.celebrantName);
  case 'BABY_SHOWER':return join('',d.parentNames);
  case 'HOUSEWARMING':return join('',d.familyName);
  case 'ANNIVERSARY':return join('',d.coupleNames);
  case 'REUNION':return join('',d.groupName);
  default:return join('',d.honoreeName);
 }
}

/** A short, occasion-specific invitation line built only from what the host entered; null when there is nothing to say. */
export function occasionHeadline(occasion,d={}){
 const ordinal=n=>{const v=Number(n);if(!Number.isInteger(v)||v<=0||v>200)return '';const s=['th','st','nd','rd'],m=v%100;return v+(s[(m-20)%10]||s[m]||s[0]);};
 switch(occasion){
  case 'WEDDING':{const names=featuredNames(occasion,d);return names?`${names} are getting married`:null;}
  case 'BIRTHDAY':{if(!d.celebrantName)return null;const n=ordinal(d.age);return n?`${d.celebrantName.trim()}’s ${n} birthday`:`${d.celebrantName.trim()}’s birthday`;}
  case 'BABY_SHOWER':return d.parentNames?`A baby shower for ${d.parentNames.trim()}`:null;
  case 'HOUSEWARMING':return d.familyName?join(' · ',`${d.familyName.trim()} welcome you home`,d.homeName):null;
  case 'ANNIVERSARY':{if(!d.coupleNames)return null;const n=ordinal(d.years);return n?`${d.coupleNames.trim()}’s ${n} anniversary`:`${d.coupleNames.trim()}’s anniversary`;}
  case 'FAREWELL':return d.honoreeName?`Farewell to ${d.honoreeName.trim()}`:null;
  case 'RETIREMENT':return d.honoreeName?(d.yearsOfService?`Celebrating ${d.honoreeName.trim()}’s ${d.yearsOfService.trim()} years`:`Celebrating ${d.honoreeName.trim()}’s retirement`):null;
  case 'REUNION':return d.groupName?join(' · ',`${d.groupName.trim()} reunion`,d.batchYear):null;
  default:return join(' · ',d.occasionName,d.honoreeName?`for ${d.honoreeName.trim()}`:'')||null;
 }
}

/** Secondary lines for the invitation (families, theme, ceremony, reception...). */
export function occasionExtraLines(occasion,d={}){
 switch(occasion){
  case 'WEDDING':return [d.brideFamily&&d.groomFamily?`${d.brideFamily.trim()} · ${d.groomFamily.trim()}`:join('',d.brideFamily,d.groomFamily),d.receptionDetails?`Reception: ${d.receptionDetails.trim()}`:''].filter(Boolean);
  case 'BIRTHDAY':case 'BABY_SHOWER':return [d.theme?`Theme: ${d.theme.trim()}`:'',d.dueMonth?`Baby due ${d.dueMonth.trim()}`:''].filter(Boolean);
  case 'HOUSEWARMING':return [join('',d.ceremony)].filter(Boolean);
  case 'FAREWELL':return [join(' · ',d.organisation,d.nextChapter)].filter(Boolean);
  case 'RETIREMENT':return [join('',d.organisation)].filter(Boolean);
  default:return [];
 }
}

/** Time to show on the invitation: an explicit start time, else the wedding muhurtham. */
export const invitationTime=(d={})=>join('',d.startTime)||join('',d.muhurtham);
