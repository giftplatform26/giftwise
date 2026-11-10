package com.giftplatform.celebrationplanning;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class CelebrationCalendarTest {
 @Test void preventsCalendarPropertyInjectionAndFoldsUnicodeByOctets(){String value=CelebrationCalendar.render(UUID.randomUUID(),"A farewell\r\nATTENDEE:intruder;hello,"+"🎉".repeat(70),LocalDate.of(2026,12,1),null,"Asia/Kolkata",120,"");assertFalse(value.contains("\r\nATTENDEE:"));assertTrue(value.contains("farewell\\nATTENDEE:"));assertTrue(value.contains("DTEND;VALUE=DATE:20261202"));for(String line:value.split("\r\n"))assertTrue(line.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=75);}
 @Test void timedCalendarUsesUtcAcrossDayBoundary(){String value=CelebrationCalendar.render(UUID.randomUUID(),"Family gathering",LocalDate.of(2026,12,1),LocalTime.of(1,0),"Asia/Kolkata",120,"Home");assertTrue(value.contains("DTSTART:20261130T193000Z"));assertTrue(value.contains("DTEND:20261130T213000Z"));}
 @Test void daylightSavingGapAndOverlapAreRejectedInsteadOfMovingTheEvent(){
  assertThrows(DateTimeException.class,()->CelebrationCalendar.validateTime(LocalDate.of(2027,3,28),LocalTime.of(1,30),"Europe/London"));
  assertThrows(DateTimeException.class,()->CelebrationCalendar.validateTime(LocalDate.of(2027,10,31),LocalTime.of(1,30),"Europe/London"));
  assertDoesNotThrow(()->CelebrationCalendar.validateTime(LocalDate.of(2027,3,28),LocalTime.of(3,30),"Europe/London"));
 }

}
