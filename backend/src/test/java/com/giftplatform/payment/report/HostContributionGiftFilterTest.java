package com.giftplatform.payment.report;

import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HostContributionGiftFilterTest {
  @Test void invalidFilterIsRejectedBeforeAccessingData() {
    var jdbc=mock(JdbcTemplate.class);
    var actors=mock(ActorContext.class);
    var service=new HostContributionReportService(jdbc,actors);
    assertEquals(HttpStatus.BAD_REQUEST,assertThrows(ResponseStatusException.class,
      ()->service.report(UUID.randomUUID(),10,0,"INVALID","ALL",UUID.randomUUID())).getStatusCode());
    verifyNoInteractions(jdbc,actors);
  }
  @Test void nonOwnerCannotListEventGifts() {
    var jdbc=mock(JdbcTemplate.class);
    var actors=mock(ActorContext.class);
    UUID eventId=UUID.randomUUID(),hostId=UUID.randomUUID();
    when(actors.requireAuthenticatedUserId()).thenReturn(hostId);
    when(jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,hostId)).thenReturn(0);
    var service=new HostContributionReportService(jdbc,actors);
    assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,
      ()->service.report(eventId,10,0,"ALL","ALL",UUID.randomUUID())).getStatusCode());
  }
}
