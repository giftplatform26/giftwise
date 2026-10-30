package com.giftplatform.payment.report;

import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HostContributionReportServiceTest {
  @Test void rejectsInvalidFiltersWithoutReadingDatabase(){
    JdbcTemplate jdbc=mock(JdbcTemplate.class);
    ActorContext actors=mock(ActorContext.class);
    var service=new HostContributionReportService(jdbc,actors);
    UUID id=UUID.randomUUID();
    assertEquals(HttpStatus.BAD_REQUEST,assertThrows(ResponseStatusException.class,
      ()->service.report(id,101,0,"ALL","ALL")).getStatusCode());
    assertThrows(ResponseStatusException.class,()->service.report(id,10,-1,"ALL","ALL"));
    assertThrows(ResponseStatusException.class,()->service.report(id,10,0,"OTHER","ALL"));
    assertThrows(ResponseStatusException.class,()->service.report(id,10,0,"ALL","UNKNOWN"));
    verifyNoInteractions(jdbc,actors);
  }
  @Test void refusesAccessToOtherHostsEvents(){
    JdbcTemplate jdbc=mock(JdbcTemplate.class);
    ActorContext actors=mock(ActorContext.class);
    UUID id=UUID.randomUUID(),actor=UUID.randomUUID();
    when(actors.requireAuthenticatedUserId()).thenReturn(actor);
    when(jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE id=? AND owner_id=?",Integer.class,id,actor)).thenReturn(0);
    assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,
      ()->new HostContributionReportService(jdbc,actors).report(id,10,0,"ALL","ALL")).getStatusCode());
  }
}
