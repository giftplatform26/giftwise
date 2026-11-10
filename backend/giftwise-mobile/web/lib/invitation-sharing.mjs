const escapeCalendar=value=>String(value).replace(/\\/g,'\\\\').replace(/\r?\n/g,'\\n').replace(/;/g,'\\;').replace(/,/g,'\\,');
export function calendarInvitation({token,title,date,url,now=new Date()}){
 if(!/^\d{4}-\d{2}-\d{2}$/.test(date||''))return null;
 const start=new Date(`${date}T00:00:00Z`);if(!Number.isFinite(start.getTime())||start.toISOString().slice(0,10)!==date)return null;
 const end=new Date(start.getTime()+86400000).toISOString().slice(0,10).replaceAll('-','');
 return ['BEGIN:VCALENDAR','VERSION:2.0','PRODID:-//GiftWise//Celebrations//EN','BEGIN:VEVENT',`UID:${escapeCalendar(token)}@giftplatform`,`DTSTAMP:${now.toISOString().replace(/[-:]/g,'').replace(/\.\d{3}/,'')}`,`DTSTART;VALUE=DATE:${date.replaceAll('-','')}`,`DTEND;VALUE=DATE:${end}`,`SUMMARY:${escapeCalendar(title)}`,`DESCRIPTION:${escapeCalendar('Your invitation: '+url)}`,'END:VEVENT','END:VCALENDAR',''].join('\r\n');
}
export function socialLinks(url,title){
 const text=encodeURIComponent(`You're invited to ${title}!`),link=encodeURIComponent(url);
 return {whatsapp:`https://wa.me/?text=${encodeURIComponent(`You're invited to ${title}! ${url}`)}`,facebook:`https://www.facebook.com/sharer/sharer.php?u=${link}`,x:`https://twitter.com/intent/tweet?text=${text}&url=${link}`,email:`mailto:?subject=${encodeURIComponent(title)}&body=${text}%0A${link}`};
}
