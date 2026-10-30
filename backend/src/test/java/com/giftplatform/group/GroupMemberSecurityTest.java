package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupMemberSecurityTest {
  private final JdbcTemplate db=mock(JdbcTemplate.class);
  private final EventRepository events=mock(EventRepository.class);
  private final GroupContributionController controller=new GroupContributionController(db,events,new BCryptPasswordEncoder(4),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
  private final UUID token=UUID.randomUUID(),group=UUID.randomUUID();
  private void published(){Event e=mock(Event.class);when(e.getStatus()).thenReturn(EventStatus.PUBLISHED);when(e.getId()).thenReturn(UUID.randomUUID());when(events.findByPublicToken(token)).thenReturn(Optional.of(e));}
  @Test void anonymousCannotReviewMembers(){published();assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.review(token,group,new GroupContributionController.Review(UUID.randomUUID(),"APPROVE"),new MockHttpServletRequest())).getStatusCode().value());}
  @Test void anonymousCannotPublish(){published();assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.publish(token,group,new MockHttpServletRequest())).getStatusCode().value());}
  @Test void anonymousCannotListMemberContacts(){published();assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.members(token,group,new MockHttpServletRequest())).getStatusCode().value());}
  @Test void unpublishedEventCannotExposeGroup(){Event e=mock(Event.class);when(e.getStatus()).thenReturn(EventStatus.DRAFT);when(events.findByPublicToken(token)).thenReturn(Optional.of(e));assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.publicGroup(token,group)).getStatusCode().value());}
}
