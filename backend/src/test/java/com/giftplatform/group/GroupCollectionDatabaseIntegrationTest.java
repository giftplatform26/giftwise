package com.giftplatform.group;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class GroupCollectionDatabaseIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 static JdbcTemplate db;
 @BeforeAll static void migrate(){var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());Flyway.configure().dataSource(ds).load().migrate();db=new JdbcTemplate(ds);}
 @Test void invitationCanBeReadyBeforeCollectionPublicationAndPoliciesRemainHostScoped(){
  UUID host=UUID.randomUUID(),event=UUID.randomUUID(),admin=UUID.randomUUID(),group=UUID.randomUUID();
  db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,'hash','CUSTOMER')",host,host+"@example.test");
  db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES(?,'Party','BIRTHDAY',?,'Host')",event,host);
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,email_verified_at) VALUES(?,?,'creator','hash',now())",admin,event);
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES(?,?,?,'Friends','Creator','+919876543210')",group,event,admin);
  var ready=new GroupReadinessService(db);
  assertFalse(ready.invitation(group).enabled());
  db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,upi_ciphertext,upi_fingerprint) VALUES(?,?,'UPI','Account','cipher','fingerprint')",UUID.randomUUID(),group);
  assertTrue(ready.invitation(group).enabled());
  assertNull(db.queryForObject("SELECT collection_published_at FROM gift_groups WHERE id=?",java.sql.Timestamp.class,group));
  assertFalse(ready.memberEmailRequired(event));
  db.update("INSERT INTO group_member_email_policies(host_id,host_enabled) VALUES(?,TRUE)",host);
  assertTrue(ready.memberEmailRequired(event));
  db.update("UPDATE group_member_email_policies SET host_enabled=FALSE,admin_enabled=TRUE WHERE host_id=?",host);
  assertTrue(ready.memberEmailRequired(event));
  db.update("UPDATE group_member_email_policies SET admin_enabled=FALSE WHERE host_id=?",host);
  assertFalse(ready.memberEmailRequired(event));

  // A ready invitation and reserved gift still reveal no collection to the host.
  UUID token=UUID.randomUUID(),gift=UUID.randomUUID();
  db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  db.update("INSERT INTO gifts(id,event_id,name,target_amount,visibility) VALUES(?,?,'Surprise',1000,'PUBLIC')",gift,event);
  var events=org.mockito.Mockito.mock(com.giftplatform.event.repository.EventRepository.class);
  var model=org.mockito.Mockito.mock(com.giftplatform.event.entity.Event.class);
  org.mockito.Mockito.when(model.getId()).thenReturn(event);
  org.mockito.Mockito.when(model.getStatus()).thenReturn(com.giftplatform.event.entity.EventStatus.PUBLISHED);
  org.mockito.Mockito.when(events.findByPublicToken(token)).thenReturn(java.util.Optional.of(model));
  var versions=org.mockito.Mockito.mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class);
  var groups=new GroupContributionController(db,events,new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(),versions);
  var request=new org.springframework.mock.web.MockHttpServletRequest();
  String page=UUID.randomUUID().toString();request.addHeader("X-Group-Page-Id",page);
  var session=request.getSession();
  session.setAttribute("gift.group.admin."+event,admin);
  session.setAttribute("gift.group.admin.epoch."+event,0L);
  session.setAttribute("gift.group.activity."+event,System.currentTimeMillis());
  session.setAttribute("gift.group.page."+event,page);
  var actor=org.mockito.Mockito.mock(com.giftplatform.security.ActorContext.class);
  org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(host);
  var collections=new GroupGiftCollectionController(db,groups,actor);
  collections.reserve(token,group,new GroupGiftCollectionController.Reservation(gift),request);
  assertEquals(gift,db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,group));
  assertTrue(collections.report(event).isEmpty());
  assertTrue(collections.notifications(event).isEmpty());
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->collections.details(event,group));
  groups.publish(token,group,request);
  assertEquals(1,collections.report(event).size());
  assertEquals("Surprise",collections.report(event).getFirst().get("gift_name"));
  assertEquals(1,collections.notifications(event).size());
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->groups.publish(token,group,request));
  assertEquals(1,collections.notifications(event).size());
 }
}
