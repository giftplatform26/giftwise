package com.giftplatform.group;

import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class GroupGiftCollectionControllerTest {
 private final JdbcTemplate db=mock(JdbcTemplate.class);
 private final GroupContributionController groups=mock(GroupContributionController.class);
 private final GroupGiftLifecycleService lifecycle=mock(GroupGiftLifecycleService.class);
 private final ActorContext actor=mock(ActorContext.class);
 private final GroupGiftCollectionController controller=new GroupGiftCollectionController(db,groups,actor,lifecycle);
 private final UUID token=UUID.randomUUID(),group=UUID.randomUUID(),creator=UUID.randomUUID(),gift=UUID.randomUUID(),event=UUID.randomUUID();
 private final MockHttpServletRequest request=new MockHttpServletRequest();
 @Test void selectionAndReleaseRequireTheBoundCreator(){
  when(groups.requireGroupCreator(token,group,request)).thenReturn(creator);
  when(lifecycle.select(group,creator,gift)).thenReturn(Map.of("reserved",false));
  assertThat(controller.reserve(token,group,new GroupGiftCollectionController.Reservation(gift),request)).containsEntry("reserved",false);
  controller.release(token,group,request);
  verify(lifecycle).release(group,creator);
 }
 @Test void unrelatedHostCannotReadReportOrNotifications(){
  when(actor.requireAuthenticatedUserId()).thenReturn(creator);
  assertThatThrownBy(()->controller.report(event)).isInstanceOf(ResponseStatusException.class);
  assertThatThrownBy(()->controller.notifications(event)).isInstanceOf(ResponseStatusException.class);
  verify(db,never()).queryForList(anyString(),eq(event));
 }
 @Test void hostCannotReadAnotherEventsTransactionsOrActivity(){
  when(actor.requireAuthenticatedUserId()).thenReturn(creator);
  when(db.queryForObject(contains("FROM events"),eq(Integer.class),eq(event),eq(creator))).thenReturn(1);
  assertThatThrownBy(()->controller.details(event,group)).isInstanceOf(ResponseStatusException.class);
  assertThatThrownBy(()->controller.hostActivity(event,group,0)).isInstanceOf(ResponseStatusException.class);
  verify(db,never()).queryForList(anyString(),any(Object[].class));
 }
}
