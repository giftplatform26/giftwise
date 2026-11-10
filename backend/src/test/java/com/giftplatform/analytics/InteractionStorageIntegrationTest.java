package com.giftplatform.analytics;

import com.giftplatform.security.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class InteractionStorageIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 static JdbcTemplate db;InteractionService service;UUID host,event,token;MockHttpServletRequest request;
 @BeforeAll static void schema() throws Exception {
  db=new JdbcTemplate(new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword()));
  db.execute("CREATE TABLE users(id UUID PRIMARY KEY,username TEXT);CREATE TABLE events(id UUID PRIMARY KEY,owner_id UUID,public_token UUID,status TEXT,title TEXT);CREATE TABLE localization_sources(message_key TEXT PRIMARY KEY,source_text TEXT)");
  db.execute(Files.readString(Path.of("src/main/resources/db/migration/V99__application_interaction_analytics.sql")));
 }
 @BeforeEach void setup(){db.execute("TRUNCATE application_interactions,events,users,localization_sources");SecurityContextHolder.clearContext();host=UUID.randomUUID();event=UUID.randomUUID();token=UUID.randomUUID();db.update("INSERT INTO users VALUES(?,?)",host,"host-one");db.update("INSERT INTO events VALUES(?,?,?,'PUBLISHED','Birthday')",event,host,token);service=new InteractionService(db,new InteractionScope(db));request=new MockHttpServletRequest();}
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 InteractionService.Input input(UUID id,String outcome){return new InteractionService.Input(id,null,"CLICK","INVITATION_LINK_COPY","/e/"+token+"?secret=private","button:nth-child(1)","external",outcome,"BROWSER_OPERATION_COMPLETED",null,token,42L);}
 @Test void clickAndOutcomeUpdatesCountOnceAndCannotBeChangedByAnotherSession(){
  UUID id=UUID.randomUUID();service.browser(input(id,"CLICKED"),request);service.browser(input(id,"SUCCESS"),request);service.browser(input(id,"FAILURE"),new MockHttpServletRequest());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM application_interactions",Integer.class));var row=db.queryForMap("SELECT * FROM application_interactions");assertEquals("SUCCESS",row.get("outcome"));assertEquals(host,row.get("host_id"));assertEquals(event,row.get("event_id"));assertEquals("/e/:id",row.get("page"));
 }
 @Test void browserCannotOverwriteServerEvidenceAndReportSeparatesClickAndRequestCounts(){
  UUID click=UUID.randomUUID();service.browser(input(click,"CLICKED"),request);request.setRequestURI("/api/public/events/"+token);request.addHeader("X-Gift-Interaction",click.toString());service.request(request,409,20,null,token,"INVITATION_OPEN");
  UUID serverId=db.queryForObject("SELECT id FROM application_interactions WHERE source='SERVER'",UUID.class);service.browser(input(serverId,"SUCCESS"),request);
  assertEquals("FAILURE",db.queryForObject("SELECT outcome FROM application_interactions WHERE id=?",String.class,serverId));
  var actor=mock(ActorContext.class);when(actor.isPlatformAdmin()).thenReturn(true);var admin=new InteractionAdminController(actor,db);var report=admin.report(host,event,null,null,"","","",0).getBody();var totals=(Map<?,?>)report.get("summary");assertEquals(1L,((Number)totals.get("clicks")).longValue());assertEquals(1L,((Number)totals.get("requests")).longValue());assertEquals(1L,((Number)totals.get("failures")).longValue());assertEquals(2,admin.timeline(click).getBody().size());
  assertEquals(0L,((Number)((Map<?,?>)admin.report(UUID.randomUUID(),null,null,null,"","","",0).getBody().get("summary")).get("records")).longValue());
 }
 @Test void privateEventTokenAndSpoofedPrivateEventIdDoNotAssignGuestActivity(){
  db.update("UPDATE events SET status='DRAFT'");service.browser(input(UUID.randomUUID(),"CLICKED"),request);
  assertNull(db.queryForMap("SELECT host_id,event_id FROM application_interactions").get("host_id"));
 }
 @Test void authenticatedHostCannotAssignAnotherHostsPrivateEvent(){
  UUID other=UUID.randomUUID();var principal=new GiftUserPrincipal(other,"unused","unused","CUSTOMER",true);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));
  var scope=new InteractionScope(db).resolve(event,null);assertEquals(other,scope.host());assertNull(scope.event());
 }
}
