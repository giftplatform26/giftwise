package com.giftplatform.celebrationplanning;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** RFC 5545 text escaping, UTC times, CRLF and UTF-8 octet line folding. */
public final class CelebrationCalendar {
 private CelebrationCalendar(){}
 private static final DateTimeFormatter UTC=DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
 static String escape(String value){return value.replace("\\","\\\\").replace("\r\n","\n").replace("\r","\n").replace("\n","\\n").replace(";","\\;").replace(",","\\,");}
 static String line(String value){StringBuilder out=new StringBuilder();int count=0;for(int i=0;i<value.length();){int cp=value.codePointAt(i);String ch=new String(Character.toChars(cp));int bytes=ch.getBytes(StandardCharsets.UTF_8).length;if(count+bytes>75){out.append("\r\n ");count=1;}out.append(ch);count+=bytes;i+=Character.charCount(cp);}return out+"\r\n";}
 public static void validateTime(LocalDate date,LocalTime time,String zone){
  ZoneId id=ZoneId.of(zone);
  if(date!=null&&time!=null&&id.getRules().getValidOffsets(date.atTime(time)).size()!=1)
   throw new DateTimeException("Choose a start time outside a daylight-saving gap or overlap");
 }
 public static String render(UUID token,String title,LocalDate date,LocalTime time,String zone,int duration,String venue){
  validateTime(date,time,zone);
  String start,end;if(time==null){start="DTSTART;VALUE=DATE:"+date.format(DateTimeFormatter.BASIC_ISO_DATE);end="DTEND;VALUE=DATE:"+date.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);}else{Instant instant=date.atTime(time).atZone(ZoneId.of(zone)).toInstant();start="DTSTART:"+UTC.format(instant);end="DTEND:"+UTC.format(instant.plusSeconds(duration*60L));}
  return line("BEGIN:VCALENDAR")+line("VERSION:2.0")+line("PRODID:-//Gift Platform//Celebrations//EN")+line("BEGIN:VEVENT")+line("UID:"+token+"@gift-platform")+line("DTSTAMP:"+UTC.format(Instant.now()))+line(start)+line(end)+line("SUMMARY:"+escape(title))+line("LOCATION:"+escape(venue))+line("END:VEVENT")+line("END:VCALENDAR");
 }
}
