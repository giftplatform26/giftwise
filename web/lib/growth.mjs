export const GROWTH_FEATURES={
 GUIDED_CREATION:['Guided event creation','Offer the autosaved step-by-step event form in both application views.'],
 OCCASION_STARTERS:['Occasion starter wording','Suggest editable titles and welcome messages when choosing an occasion.'],
 SMART_DASHBOARD:['Dashboard next steps','Show a bounded event summary, RSVP totals and useful next actions.'],
 POLISHED_GUEST_VIEW:['Polished mobile invitations','Use the mobile invitation layout in Classic view too. Focused view retains its existing invitation layout.'],
 PUBLIC_DEMO:['Public sample celebration','Let visitors explore a fictional sample without registration or data submission.'],
 SHARE_PREVIEWS:['Rich sharing previews','Allow hosts to opt in to title, occasion and date previews in messaging services.'],
 PUBLIC_KEEPSAKES:['Shareable approved-wishes keepsakes','Allow hosts to share approved guestbook messages after an event. Private albums and photos remain private.'],
 GUEST_DISCOVERY:['Guest discovery link','Show an unobtrusive create-your-own invitation link on guest pages.'],
 INTERACTION_ANALYTICS:['Optional interaction analytics','Allow consented browser and server interaction collection. Disabling stops new collection; existing reports remain.']
};
export function normalizeGrowth(value){return Object.fromEntries(Object.keys(GROWTH_FEATURES).map(key=>[key,value?.features?.[key]===true]));}
export const occasionStarters={
 WEDDING:{title:'Together begins here',description:'Join us as we celebrate love, laughter and a beautiful new beginning.',theme:'ELEGANT_GOLD',art:'sage-leaves'},
 BIRTHDAY:{title:'A little older, a lot to celebrate',description:'Good company, happy moments and birthday wishes. Come celebrate with us!',theme:'BIRTHDAY_CELEBRATION',art:'balloon-dreams'},
 BABY_SHOWER:{title:'A little one, a whole lot of love',description:'Help us welcome a new adventure with warm wishes and wonderful company.',theme:'BABY_SHOWER',art:'cloud-dream'},
 HOUSEWARMING:{title:'New home, familiar faces',description:'Our new place would feel even more like home with you in it. Join us!',theme:'HOUSEWARMING',art:'cream-paper'},
 ANNIVERSARY:{title:'Another year of us',description:'We are celebrating another chapter together and would love you to be part of it.',theme:'ANNIVERSARY',art:'rose-garden'},
 FAREWELL:{title:'A farewell filled with gratitude',description:'Share favourite memories, say thank you and wish someone special a wonderful next chapter.',theme:'ELEGANT_GOLD',art:'sage-leaves'},
 RETIREMENT:{title:'Here’s to your next chapter',description:'A time to celebrate the journey, the people and all the possibilities ahead.',theme:'MODERN_MINIMAL',art:'cream-paper'},
 REUNION:{title:'Together again',description:'Familiar faces, favourite stories and new memories. We would love to see you again.',theme:'MODERN_MINIMAL',art:'cream-paper'},
 OTHER:{title:'A moment worth sharing',description:'Let us make a little time for the people and moments that matter.',theme:'MODERN_MINIMAL',art:'cream-paper'}
};
export function applyOccasionStarter(form,occasion,enabled){
 const old=occasionStarters[form.occasionType],next=occasionStarters[occasion];
 if(!enabled||!next)return {...form,occasionType:occasion};
 return {...form,occasionType:occasion,
 title:!form.title||form.title===old?.title?next.title:form.title,
 description:!form.description||form.description===old?.description?next.description:form.description,
 theme:!form.theme||form.theme==='MODERN_MINIMAL'||form.theme===old?.theme?next.theme:form.theme};
}
export function giftThumbnail(url){return typeof url==='string'&&/^\/backend\/api\/public\/events\/[a-f0-9-]+\/gifts\/[a-f0-9-]+\/photo$/i.test(url)?url+'?thumbnail=true':url;}
