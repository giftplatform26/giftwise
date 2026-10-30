package com.giftplatform.payment.report;

import com.giftplatform.security.ActorContext;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class HostReportPaginationIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17-alpine");
 static JdbcTemplate db;
 HostContributionReportService service;
 UUID host,event,gift;
 @BeforeAll static void database(){
  var source=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
  Flyway.configure().dataSource(source).load().migrate();db=new JdbcTemplate(source);
 }
 @BeforeEach void setup(){
  host=UUID.randomUUID();event=UUID.randomUUID();gift=UUID.randomUUID();
  db.update("INSERT INTO users(id,email,password_hash,username) VALUES (?,?,?,?)",host,host+"@example.test","hash","user_"+host.toString().replace("-","").substring(0,20));
  db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,?)",event,"Report test","BIRTHDAY","PUBLISHED",host,"Test Host");
  db.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES (?,?,?,?)",gift,event,"Gift",1000);
  ActorContext actors=mock(ActorContext.class);when(actors.requireAuthenticatedUserId()).thenReturn(host);service=new HostContributionReportService(db,actors);
 }
 UUID manual(UUID giftId,long amount,long giftPart,long walletPart,String status,String when){
  UUID id=UUID.randomUUID();
  db.update("INSERT INTO guest_direct_transfer_claims(id,event_id,gift_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key,status,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",id,event,giftId,UUID.randomUUID(),"UPI",amount,"Guest",id.toString().replace("-","")+id.toString().replace("-",""),id.toString(),status,OffsetDateTime.parse(when));
  db.update("INSERT INTO guest_manual_display_allocations(claim_id,event_id,gift_id,gift_paise,wallet_paise) VALUES (?,?,?,?,?)",id,event,giftId,giftPart,walletPart);return id;
 }
 @Test void totalsAndSelectedGiftProgressDoNotDependOnPage(){
  manual(gift,1000,700,300,"AWAITING_VERIFICATION","2026-10-20T10:00:00Z");
  UUID newest=manual(gift,2000,2000,0,"AWAITING_VERIFICATION","2026-10-21T10:00:00Z");
  var first=service.report(event,1,0,"ALL","ALL",gift);var second=service.report(event,1,1,"ALL","ALL",gift);
  assertEquals(2,first.matchingCount());assertEquals(newest,first.entries().getFirst().id());assertNotEquals(newest,second.entries().getFirst().id());
  assertEquals(3000,first.pendingPaise());assertEquals(first.pendingPaise(),second.pendingPaise());assertEquals(2700,first.selectedGiftReportedPaise());assertEquals(0,first.verifiedPaise());
  var beyond=service.report(event,1,99,"ALL","ALL",gift);assertTrue(beyond.entries().isEmpty());assertEquals(2,beyond.matchingCount());assertEquals(3000,beyond.pendingPaise());
 }
 @Test void utcDateStatusAndSplitFiltersKeepOriginalSemantics(){
  manual(gift,1000,700,300,"AWAITING_VERIFICATION","2026-10-21T23:59:59Z");
  manual(gift,2000,2000,0,"AWAITING_VERIFICATION","2026-10-22T00:00:00Z");
  manual(gift,4000,4000,0,"REJECTED","2026-10-21T12:00:00Z");
  var wallet=service.report(event,10,0,"WALLET","VERIFICATION_PENDING",gift,LocalDate.parse("2026-10-21"),LocalDate.parse("2026-10-21"));
  assertEquals(1,wallet.matchingCount());assertEquals(300,wallet.pendingPaise());assertEquals(0,wallet.reportedGiftPaise());assertEquals(300,wallet.reportedWalletPaise());
  assertEquals(2700,wallet.selectedGiftReportedPaise());assertEquals(0,wallet.verifiedPaise());
  assertEquals(1,service.report(event,10,0,"GIFT","FAILED",gift).matchingCount());
 }
 @Test void pendingProviderOrdersUseIntendedAmountAndAreNeverVerified(){
  UUID order=UUID.randomUUID();db.update("INSERT INTO payment_orders(id,event_id,gift_id,contribution_type,amount_paise,idempotency_key) VALUES (?,?,?,?,?,?)",order,event,gift,"GIFT_PARTIAL",2500,order.toString());
  db.update("INSERT INTO contributions(id,payment_order_id,event_id,gift_id,guest_name,amount_paise) VALUES (?,?,?,?,?,?)",UUID.randomUUID(),order,event,gift,"Guest",2500);
  var report=service.report(event,10,0,"GIFT","ALL",gift);assertEquals(2500,report.pendingPaise());assertEquals(0,report.verifiedPaise());assertEquals(0,report.reportedGiftPaise());assertEquals(1,report.matchingCount());
 }
 @Test void capturedSplitCountsAsVerifiedOnlyWithProviderEvidenceAndPosting(){
  UUID order=UUID.randomUUID(),contribution=UUID.randomUUID();
  db.update("INSERT INTO payment_orders(id,event_id,gift_id,contribution_type,amount_paise,idempotency_key,status) VALUES (?,?,?,?,?,?,?)",order,event,gift,"GIFT_PARTIAL",3000,order.toString(),"CAPTURED");
  db.update("INSERT INTO contributions(id,payment_order_id,event_id,gift_id,guest_name,amount_paise) VALUES (?,?,?,?,?,?)",contribution,order,event,gift,"Guest",3000);
  db.update("INSERT INTO payment_transactions(id,payment_order_id,provider_payment_id,amount_paise,status,verified_at) VALUES (?,?,?,?,?,now())",UUID.randomUUID(),order,UUID.randomUUID().toString(),3000,"CAPTURED");
  db.update("INSERT INTO contribution_allocations(id,contribution_id,event_id,gift_id,allocation_type,amount_paise) VALUES (?,?,?,?,?,?)",UUID.randomUUID(),contribution,event,gift,"GIFT",2000);
  db.update("INSERT INTO contribution_allocations(id,contribution_id,event_id,allocation_type,amount_paise) VALUES (?,?,?,?,?)",UUID.randomUUID(),contribution,event,"EVENT_WALLET",1000);
  assertEquals(0,service.report(event,10,0,"ALL","ALL").verifiedPaise());
  db.update("INSERT INTO contribution_allocation_postings(contribution_id,entry_group_id) VALUES (?,?)",contribution,UUID.randomUUID());
  var all=service.report(event,10,0,"ALL","ALL",gift);assertEquals(3000,all.verifiedPaise());assertEquals(2000,all.verifiedGiftPaise());assertEquals(1000,all.verifiedWalletPaise());assertEquals(2000,all.selectedGiftVerifiedPaise());assertEquals(0,all.pendingPaise());
  var wallet=service.report(event,10,0,"WALLET","VERIFIED",gift);assertEquals(1000,wallet.verifiedPaise());assertEquals(0,wallet.verifiedGiftPaise());
 }
 @Test void creatorDashboardCountsMembersWithoutOpeningMembersAndKeepsReportsUnverified(){
  UUID creator=UUID.randomUUID(),group=UUID.randomUUID(),destination=UUID.randomUUID(),token=UUID.randomUUID();
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,full_name) VALUES (?,?,?,?,?)",creator,event,"creator","hash","Test Creator");
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES (?,?,?,?,?,?)",group,event,creator,"Test Group","Test Creator","+919876543210");
  for(String status:List.of("APPROVED","PENDING","REJECTED"))db.update("INSERT INTO gift_group_members(id,group_id,display_name,username,password_hash,status) VALUES (?,?,?,?,?,?)",UUID.randomUUID(),group,status,status.toLowerCase(),"hash",status);
  db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,upi_ciphertext,upi_fingerprint) VALUES (?,?,?,?,?,?)",destination,group,"UPI","Test UPI","ciphertext","fingerprint");
  for(String state:List.of("REPORTED_UNVERIFIED","REJECTED"))db.update("INSERT INTO gift_group_guest_reports(id,group_id,destination_id,guest_name,amount_paise,method,transaction_reference,submission_key,state) VALUES (?,?,?,?,?,?,?,?,?)",UUID.randomUUID(),group,destination,"Guest",500,"UPI",UUID.randomUUID().toString(),UUID.randomUUID(),state);
  var events=mock(com.giftplatform.event.repository.EventRepository.class);var current=mock(com.giftplatform.event.entity.Event.class);when(current.getId()).thenReturn(event);when(current.getStatus()).thenReturn(com.giftplatform.event.entity.EventStatus.PUBLISHED);when(events.findByPublicToken(token)).thenReturn(Optional.of(current));
  var controller=new com.giftplatform.group.GroupContributionController(db,events,new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(4),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
  var request=new org.springframework.mock.web.MockHttpServletRequest();String page=UUID.randomUUID().toString();request.addHeader("X-Group-Page-Id",page);var session=request.getSession();session.setAttribute("gift.group.admin."+event,creator);session.setAttribute("gift.group.page."+event,page);session.setAttribute("gift.group.activity."+event,System.currentTimeMillis());session.setAttribute("gift.group.admin.epoch."+event,0L);
  var dashboard=controller.dashboard(token,request);var summary=(Map<?,?>)dashboard.get("summary");assertEquals(1L,((Number)summary.get("member_count")).longValue());assertEquals(1L,((Number)summary.get("pending_member_count")).longValue());assertEquals(500L,((Number)summary.get("reported_paise")).longValue());assertEquals(1L,((Number)summary.get("report_count")).longValue());assertNotNull(dashboard.get("privacy"));
 }
 @Test void searchFiltersAllPagesTotalsAndCsvAndTreatsWildcardsLiterally(){
  for(int i=0;i<23;i++)manual(gift,100,100,0,"AWAITING_VERIFICATION","2026-10-21T12:00:00Z");
  UUID special=manual(gift,500,500,0,"AWAITING_VERIFICATION","2026-10-22T12:00:00Z");
  db.update("UPDATE guest_direct_transfer_claims SET guest_name=? WHERE id=?","Asha 100%_",special);
  var first=service.report(event,10,0,"ALL","ALL",null,null,null,"guest");
  var last=service.report(event,10,20,"ALL","ALL",null,null,null,"GUEST");
  assertEquals(23,first.matchingCount());assertEquals(3,last.entries().size());
  assertEquals(2300,first.pendingPaise());assertEquals(first.pendingPaise(),last.pendingPaise());
  var literal=service.report(event,20,0,"ALL","ALL",null,null,null,"100%_");
  assertEquals(1,literal.matchingCount());assertEquals(special,literal.entries().getFirst().id());
  assertEquals(24,service.report(event,20,0,"ALL","ALL",null,null,null,"upi").matchingCount());
  assertEquals(24,service.report(event,20,0,"ALL","ALL",null,null,null,"gift").matchingCount());
  assertEquals(0,service.report(event,20,0,"ALL","ALL",null,null,null,"missing").matchingCount());
  String csv=service.exportCsv(event,"ALL","ALL",null,null,null,"asha");
  assertEquals(2,csv.lines().count());assertTrue(csv.contains("Asha 100%_"));
 }
 @Test void csvTraversesSqlPagesWithoutDroppingRecords(){
  for(int i=0;i<103;i++)manual(gift,100,100,0,"AWAITING_VERIFICATION","2026-10-21T12:00:00Z");
  String csv=service.exportCsv(event,"ALL","ALL",gift,null,null);assertEquals(104,csv.lines().count());assertTrue(csv.contains("1.00"));
 }
}
