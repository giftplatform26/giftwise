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
 private final ActorContext actor=mock(ActorContext.class);
 private final GroupGiftCollectionController controller=new GroupGiftCollectionController(db,groups,actor);
 private final UUID token=UUID.randomUUID(),group=UUID.randomUUID(),creator=UUID.randomUUID(),gift=UUID.randomUUID(),event=UUID.randomUUID();
 private final MockHttpServletRequest request=new MockHttpServletRequest();
 @Test void unverifiedCreatorCannotReserve(){
  when(groups.requireGroupCreator(token,group,request)).thenReturn(creator);
  assertThatThrownBy(()->controller.reserve(token,group,new GroupGiftCollectionController.Reservation(gift),request)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("Verify your email or mobile");
  verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void verifiedCreatorCanReserveWithinOwnEvent(){
  when(groups.requireGroupCreator(token,group,request)).thenReturn(creator);
  when(db.queryForObject(contains("email_verified_at"),eq(Boolean.class),eq(creator))).thenReturn(true);
  when(db.queryForObject(contains("SELECT event_id"),eq(UUID.class),eq(group))).thenReturn(event);
  when(db.queryForList(contains("SELECT id FROM gifts"),eq(gift),eq(event))).thenReturn(List.of(Map.of("id",gift)));
  when(db.update(contains("UPDATE gift_groups"),eq(gift),eq(group))).thenReturn(1);
  assertThat(controller.reserve(token,group,new GroupGiftCollectionController.Reservation(gift),request)).containsEntry("message","A surprise is waiting");
 }
 @Test void unrelatedHostCannotReadReportOrNotifications(){
  when(actor.requireAuthenticatedUserId()).thenReturn(creator);
  assertThatThrownBy(()->controller.report(event)).isInstanceOf(ResponseStatusException.class);
  assertThatThrownBy(()->controller.notifications(event)).isInstanceOf(ResponseStatusException.class);
  verify(db,never()).queryForList(anyString(),eq(event));
 }
}
