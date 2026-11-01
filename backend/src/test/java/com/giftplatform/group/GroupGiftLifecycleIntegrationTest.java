package com.giftplatform.group;

import com.giftplatform.payment.report.HostContributionReportService;
import com.giftplatform.security.ActorContext;
import java.util.*;
import java.util.concurrent.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class GroupGiftLifecycleIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 static JdbcTemplate db;static TransactionTemplate tx;static GroupGiftLifecycleService lifecycle;
 @BeforeAll static void migrate(){var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());Flyway.configure().dataSource(ds).load().migrate();db=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));lifecycle=new GroupGiftLifecycleService(db);}
 record Fixture(UUID host,UUID event,UUID creator,UUID group,UUID gift,UUID destination){}
 Fixture fixture(boolean verified){
  UUID host=UUID.randomUUID(),event=UUID.randomUUID(),creator=UUID.randomUUID(),group=UUID.randomUUID(),gift=UUID.randomUUID(),destination=UUID.randomUUID();
  db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,'hash','CUSTOMER')",host,host+"@example.test");
  db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES(?,'Party','BIRTHDAY',?,'Host')",event,host);
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,email,email_verified_at) VALUES(?,?,'creator','hash','creator@example.test',CASE WHEN ? THEN now() ELSE NULL END)",creator,event,verified);
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES(?,?,?,'Friends','Creator','+919876543210')",group,event,creator);
  db.update("INSERT INTO gifts(id,event_id,name,description,product_url,target_amount,visibility) VALUES(?,?,'Coffee maker','Morning joy','https://example.test/coffee',1000,'PUBLIC')",gift,event);
  db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,upi_ciphertext,upi_fingerprint) VALUES(?,?,'UPI','Account','cipher',?)",destination,group,destination.toString());
  return new Fixture(host,event,creator,group,gift,destination);
 }
 UUID report(Fixture f){UUID id=UUID.randomUUID();db.update("INSERT INTO gift_group_guest_reports(id,group_id,destination_id,guest_name,guest_contact,amount_paise,method,transaction_reference,submission_key) VALUES(?,?,?,'Guest','guest@example.test',25000,'UPI',?,?)",id,f.group,f.destination,id.toString(),UUID.randomUUID());return id;}
 HostContributionReportService reports(Fixture f){var actor=mock(ActorContext.class);when(actor.requireAuthenticatedUserId()).thenReturn(f.host);return new HostContributionReportService(db,actor);}
 @Test void selectionAutofillsButDoesNotReserveUntilCreatorEmailIsVerified(){
  var f=fixture(false);
  var result=tx.execute(s->lifecycle.select(f.group,f.creator,f.gift));
  assertEquals(false,result.get("reserved"));
  assertNull(db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,f.group));
  var details=db.queryForMap("SELECT gift_name,gift_description,gift_link,target_paise FROM gift_groups WHERE id=?",f.group);
  assertEquals("Coffee maker",details.get("gift_name"));assertEquals("Morning joy",details.get("gift_description"));assertEquals("https://example.test/coffee",details.get("gift_link"));assertEquals(100000L,((Number)details.get("target_paise")).longValue());
  db.update("UPDATE gift_group_accounts SET mobile_verified_at=now() WHERE id=?",f.creator);
  tx.executeWithoutResult(s->lifecycle.activateForCreator(f.creator));
  assertNull(db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,f.group));
  db.update("UPDATE gift_group_accounts SET email_verified_at=now() WHERE id=?",f.creator);
  tx.executeWithoutResult(s->lifecycle.activateForCreator(f.creator));
  assertEquals(f.gift,db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,f.group));
 }
 @Test void draftTransactionsStayPrivateAndPublishedReleasePreservesHistoryAndTotals(){
  var f=fixture(true);tx.executeWithoutResult(s->lifecycle.select(f.group,f.creator,f.gift));UUID transfer=report(f);
  var reports=reports(f);assertTrue(reports.report(f.event,20,0,"ALL","ALL").entries().isEmpty());
  db.update("UPDATE gift_groups SET status='PUBLISHED',collection_published_at=now() WHERE id=?",f.group);
  var before=reports.report(f.event,20,0,"ALL","ALL",f.gift);
  assertEquals(25000,before.pendingPaise());assertEquals(0,before.verifiedPaise());assertEquals(25000,before.selectedGiftReportedPaise());assertEquals("GROUP",before.entries().getFirst().source());
  tx.executeWithoutResult(s->lifecycle.release(f.group,f.creator));
  assertNull(db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,f.group));
  assertNull(db.queryForObject("SELECT gift_name FROM gift_groups WHERE id=?",String.class,f.group));
  assertEquals(f.gift,db.queryForObject("SELECT gift_id_snapshot FROM gift_group_guest_reports WHERE id=?",UUID.class,transfer));
  assertEquals(25000,reports.report(f.event,20,0,"ALL","ALL").pendingPaise());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM gift_group_activity WHERE group_id=? AND action='GIFT_RELEASED'",Integer.class,f.group));
  assertEquals(1,db.queryForObject("SELECT count(*) FROM gift_group_activity WHERE group_id=? AND action='CONTRIBUTION_REPORTED'",Integer.class,f.group));
  assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("DELETE FROM gift_group_activity WHERE group_id=?",f.group));
 }
 @Test void generalCollectionCanChooseGiftAfterCollectingWithoutRewritingTransactions(){
  var f=fixture(true);UUID transfer=report(f);
  tx.executeWithoutResult(s->lifecycle.select(f.group,f.creator,f.gift));
  assertEquals(f.gift,db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,f.group));
  assertNull(db.queryForObject("SELECT gift_id_snapshot FROM gift_group_guest_reports WHERE id=?",UUID.class,transfer));
  assertEquals(25000,db.queryForObject("SELECT amount_paise FROM gift_group_guest_reports WHERE id=?",Long.class,transfer));
 }
 @Test void anotherEventAndAnotherCreatorsGroupCannotBeModified(){
  var f=fixture(true);var other=fixture(true);
  assertThrows(ResponseStatusException.class,()->tx.execute(s->lifecycle.select(f.group,f.creator,other.gift)));
  assertThrows(ResponseStatusException.class,()->tx.execute(s->lifecycle.release(f.group,other.creator)));
 }
 @Test void onlyOneVerifiedGroupCanReserveTheSameGiftConcurrently() throws Exception {
  var f=fixture(true);UUID otherCreator=UUID.randomUUID(),otherGroup=UUID.randomUUID();
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,email_verified_at) VALUES(?,?,'other','hash',now())",otherCreator,f.event);
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES(?,?,?,'Other','Other','+919876543210')",otherGroup,f.event,otherCreator);
  var gate=new CountDownLatch(1);
  try(var executor=Executors.newFixedThreadPool(2)){
   var first=executor.submit(()->compete(gate,f.group,f.creator,f.gift));var second=executor.submit(()->compete(gate,otherGroup,otherCreator,f.gift));gate.countDown();
   assertEquals(1,first.get(20,TimeUnit.SECONDS)+second.get(20,TimeUnit.SECONDS));
  }
  assertEquals(1,db.queryForObject("SELECT count(*) FROM gift_groups WHERE reserved_gift_id=?",Integer.class,f.gift));
 }
 int compete(CountDownLatch gate,UUID group,UUID creator,UUID gift) throws Exception {gate.await();try{tx.executeWithoutResult(s->lifecycle.select(group,creator,gift));return 1;}catch(ResponseStatusException ex){assertEquals(409,ex.getStatusCode().value());return 0;}}
 org.springframework.mock.web.MockHttpServletRequest creatorRequest(UUID event,UUID creator){
  var req=new org.springframework.mock.web.MockHttpServletRequest();String page=UUID.randomUUID().toString();req.addHeader("X-Group-Page-Id",page);
  var session=req.getSession();session.setAttribute("gift.group.admin."+event,creator);session.setAttribute("gift.group.page."+event,page);
  session.setAttribute("gift.group.activity."+event,System.currentTimeMillis());session.setAttribute("gift.group.admin.epoch."+event,0L);return req;
 }
 @Test void enteringTheEmailCodeAutomaticallyActivatesTheSelectedGift(){
  var f=fixture(false);UUID token=UUID.randomUUID();db.update("UPDATE events SET public_token=?,status='PUBLISHED' WHERE id=?",token,f.event);
  tx.executeWithoutResult(s->lifecycle.select(f.group,f.creator,f.gift));
  var delivery=mock(com.giftplatform.security.delivery.MessageDelivery.class);var code=new java.util.concurrent.atomic.AtomicReference<String>();
  org.mockito.Mockito.doAnswer(call->{var matcher=java.util.regex.Pattern.compile("\\b[0-9]{6}\\b").matcher(call.<String>getArgument(1));assertTrue(matcher.find());code.set(matcher.group());return null;}).when(delivery).send(org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
  var controller=new com.giftplatform.security.recovery.GroupEmailEnrollmentController(db,new com.giftplatform.security.recovery.RecoveryChallengeStore(db),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class),delivery,Base64.getEncoder().encodeToString(new byte[32]));
  org.springframework.test.util.ReflectionTestUtils.setField(controller,"giftLifecycle",lifecycle);
  var req=creatorRequest(f.event,f.creator);
  var started=tx.execute(s->controller.start(token,new com.giftplatform.security.recovery.GroupEmailEnrollmentController.Start("creator@example.test",null),req));
  tx.executeWithoutResult(s->controller.complete(token,new com.giftplatform.security.recovery.GroupEmailEnrollmentController.Complete(started.challengeId(),code.get(),null),req));
  assertEquals(f.gift,db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,f.group));
 }
 @Test void setupFromWishlistUsesTrustedGiftDetails(){
  var f=fixture(true);UUID creator=UUID.randomUUID(),token=UUID.randomUUID();
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,email_verified_at) VALUES(?,?,'newcreator','hash',now())",creator,f.event);
  var events=mock(com.giftplatform.event.repository.EventRepository.class);var event=mock(com.giftplatform.event.entity.Event.class);
  when(event.getId()).thenReturn(f.event);when(event.getStatus()).thenReturn(com.giftplatform.event.entity.EventStatus.PUBLISHED);when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  var groups=new GroupContributionController(db,events,mock(org.springframework.security.crypto.password.PasswordEncoder.class),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
  org.springframework.test.util.ReflectionTestUtils.setField(groups,"giftLifecycle",lifecycle);
  var req=creatorRequest(f.event,creator);
  var created=tx.execute(s->groups.setup(token,new GroupContributionController.Setup("New group","Creator","+919876543210",null,"Untrusted name",null,"/affiliate/preview",1L,f.gift),req));
  assertEquals("Coffee maker",created.giftName());assertEquals(100000L,created.targetPaise());
  assertEquals(f.gift,db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,created.id()));
  var dashboard=groups.dashboard(token,req);
  assertEquals(f.gift,dashboard.get("selectedGiftId"));assertEquals(true,dashboard.get("creatorEmailVerified"));
 }
 @Test void memberTransfersAndReviewsAreRetainedInTheActivityLog(){
  var f=fixture(true);UUID member=UUID.randomUUID(),report=UUID.randomUUID();
  db.update("INSERT INTO gift_group_members(id,group_id,display_name,username,password_hash,status) VALUES(?,?,'Member','member','hash','PENDING')",member,f.group);
  db.update("UPDATE gift_group_members SET status='APPROVED' WHERE id=?",member);
  db.update("INSERT INTO gift_group_payment_reports(id,group_id,member_id,amount_paise,method,transaction_reference) VALUES(?,?,?,5000,'BANK','BANK123456')",report,f.group,member);
  assertEquals(1,db.queryForObject("SELECT count(*) FROM group_collection_transactions WHERE group_id=? AND contributor_type='MEMBER'",Integer.class,f.group));
  var actions=db.queryForList("SELECT action FROM gift_group_activity WHERE group_id=?",String.class,f.group);
  assertTrue(actions.containsAll(List.of("MEMBER_JOINED","MEMBER_UPDATED","CONTRIBUTION_REPORTED")));
 }
 @Test void guestGiftSelectionHasOneWinnerAndHostSeesPlannedGift() throws Exception {
  var f=fixture(true);UUID gift=UUID.randomUUID();
  db.update("INSERT INTO guest_gifts(id,event_id,added_by,name) VALUES(?,?,?,'Surprise')",gift,f.event,f.creator);
  var actor=mock(ActorContext.class);when(actor.requireAuthenticatedUserId()).thenReturn(f.host);
  var controller=new GuestGiftController(db,mock(GroupContributionController.class),mock(com.giftplatform.event.repository.EventRepository.class),actor);
  assertTrue(controller.hostReport(f.event).isEmpty());
  var pool=Executors.newFixedThreadPool(2);
  try {
   var start=new CountDownLatch(1);
   Callable<Integer> claim=()->{start.await();return db.update("UPDATE guest_gifts SET selected_by=?,contribution_type='INDIVIDUAL' WHERE id=? AND selected_by IS NULL",f.creator,gift);};
   var a=pool.submit(claim);var b=pool.submit(claim);start.countDown();
   assertEquals(1,a.get(10,TimeUnit.SECONDS)+b.get(10,TimeUnit.SECONDS));
  } finally {pool.shutdownNow();}
  assertEquals(gift,controller.hostReport(f.event).getFirst().get("id"));
  db.update("UPDATE guest_gifts SET published_at=now() WHERE id=?",gift);
  assertEquals(gift,controller.hostReport(f.event).getFirst().get("id"));
  var other=fixture(true);when(actor.requireAuthenticatedUserId()).thenReturn(other.host);assertTrue(controller.hostReport(other.event).isEmpty());
 }

 @Test void giftPublicationCountsOnlyConfirmedMoneyAndHidesDraftDetails(){
  var f=fixture(true);db.update("UPDATE gift_groups SET independent_gift=TRUE,gift_name='Coffee maker',target_paise=100000 WHERE id=?",f.group);
  var groups=mock(GroupContributionController.class);var request=new org.springframework.mock.web.MockHttpServletRequest();
  when(groups.event(f.event)).thenReturn(f.event);
  var completion=new GroupGiftCompletionController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  UUID payment=report(f);
  assertEquals(0L,db.queryForObject("SELECT collected_paise FROM group_gift_progress WHERE id=?",Long.class,f.group));
  assertThrows(ResponseStatusException.class,()->tx.execute(s->completion.publish(f.event,f.group,request)));
  var hidden=completion.board(f.event).getFirst();assertNull(hidden.get("gift_name"));assertFalse(hidden.containsKey("target_paise"));
  db.update("UPDATE gift_group_guest_reports SET amount_paise=100000,state='VERIFIED' WHERE id=?",payment);
  tx.executeWithoutResult(s->completion.publish(f.event,f.group,request));
  assertEquals("Coffee maker",completion.board(f.event).getFirst().get("gift_name"));
  assertEquals("100% Contributed ✅",completion.board(f.event).getFirst().get("display_status"));
  var actor=mock(ActorContext.class);when(actor.requireAuthenticatedUserId()).thenReturn(f.host);
  var report=new GroupGiftCollectionController(db,groups,actor,lifecycle).report(f.event).getFirst();
  assertEquals("100% Contributed",report.get("display_status"));assertEquals(100000L,report.get("collected_paise"));
 }
 @Test void hostSeesDraftGroupProgress(){
  var f=fixture(true);var actor=mock(ActorContext.class);when(actor.requireAuthenticatedUserId()).thenReturn(f.host);
  var controller=new GroupGiftCollectionController(db,mock(GroupContributionController.class),actor,lifecycle);
  assertEquals("Something is Cooking",controller.report(f.event).getFirst().get("display_status"));
  assertTrue(controller.details(f.event,f.group).isEmpty());
 }

 @Test void creatorCanAcknowledgeReceiptWithoutVerifyingPayment(){
  var f=fixture(true);tx.executeWithoutResult(s->lifecycle.select(f.group,f.creator,f.gift));
  UUID payment=report(f);var groups=mock(GroupContributionController.class);
  var controller=new GroupGiftCompletionController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  var request=new org.springframework.mock.web.MockHttpServletRequest();
  tx.executeWithoutResult(s->controller.acknowledgeReceipt(f.event,f.group,payment,request));
  assertEquals(25000L,db.queryForObject("SELECT collected_paise FROM group_gift_progress WHERE id=?",Long.class,f.group));
  assertEquals("REPORTED_UNVERIFIED",db.queryForObject("SELECT state FROM gift_group_guest_reports WHERE id=?",String.class,payment));
  tx.executeWithoutResult(s->controller.acknowledgeReceipt(f.event,f.group,payment,request));
  assertEquals(25000L,db.queryForObject("SELECT collected_paise FROM group_gift_progress WHERE id=?",Long.class,f.group));
 }
 @Test void individualPrivateFieldsOnlyReachSelectorAndHost(){
  var f=fixture(true);var groups=mock(GroupContributionController.class);var actor=mock(ActorContext.class);
  var request=new org.springframework.mock.web.MockHttpServletRequest();
  when(groups.event(f.event)).thenReturn(f.event);when(groups.sessionAdmin(f.event,request)).thenReturn(f.creator);
  when(actor.requireAuthenticatedUserId()).thenReturn(f.host);
  var controller=new GuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class),actor);
  UUID gift=controller.individual(f.event,new GuestGiftController.IndividualGift("Coffee","Coffee maker",null,"Guest","guest@example.test","+919876543210",50000L),request).get("id");
  var publicGift=controller.list(f.event).getFirst();
  assertFalse(publicGift.containsKey("guest_email"));assertFalse(publicGift.containsKey("contribution_paise"));
  assertEquals(50000L,controller.mine(f.event,request).getFirst().get("contribution_paise"));
  assertEquals("guest@example.test",controller.hostReport(f.event).getFirst().get("guest_email"));
  UUID other=UUID.randomUUID();db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,'other','hash')",other,f.event);
  db.update("UPDATE guest_gifts SET added_by=? WHERE id=?",other,gift);
  when(groups.sessionAdmin(f.event,request)).thenReturn(other);
  assertNull(controller.mine(f.event,request).getFirst().get("guest_email"));
  assertNull(controller.mine(f.event,request).getFirst().get("contribution_paise"));
 }

 @Test void individualEntryNeedsNoGroupAccountAndKeepsContactPrivate(){
  var f=fixture(true);var groups=mock(GroupContributionController.class);when(groups.event(f.event)).thenReturn(f.event);
  var request=new org.springframework.mock.web.MockHttpServletRequest();
  var controller=new IndividualGuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  var input=new IndividualGuestGiftController.Entry(null,"Coffee maker","A kitchen gift","https://example.test/coffee","Alex","alex@example.test","+919876543210",IndividualGuestGiftController.Method.LINK_AND_CONTRIBUTION,50000L);
  UUID id=tx.execute(s->controller.save(f.event,input,request)).get("id");
  assertEquals(id,controller.mine(f.event,request).getFirst().get("id"));
  assertTrue(controller.mine(f.event,new org.springframework.mock.web.MockHttpServletRequest()).isEmpty());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM gift_groups WHERE event_id=?",Integer.class,f.event));
  verify(groups,never()).sessionAdmin(any(),any());
  var actor=mock(ActorContext.class);when(actor.requireAuthenticatedUserId()).thenReturn(f.host);
  var report=new GuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class),actor);
  assertEquals("alex@example.test",report.hostReport(f.event).getFirst().get("guest_email"));
  assertFalse(report.list(f.event).getFirst().containsKey("guest_email"));
  assertThrows(ResponseStatusException.class,()->controller.tracking(f.event,id,new GuestGiftController.Delivery(GuestGiftController.DeliveryStatus.TRACKING_SHARED,"Courier 123"),new org.springframework.mock.web.MockHttpServletRequest()));
  controller.tracking(f.event,id,new GuestGiftController.Delivery(GuestGiftController.DeliveryStatus.TRACKING_SHARED,"Courier 123"),request);
  assertEquals("Courier 123",db.queryForObject("SELECT tracking_details FROM guest_gifts WHERE id=?",String.class,id));
 }
 @Test void individualEntrySelectsAvailableGiftsAndPreventsDoubleSelection(){
  var f=fixture(true);UUID id=UUID.randomUUID();db.update("INSERT INTO guest_gifts(id,event_id,added_by,name) VALUES(?,?,?,'Coffee')",id,f.event,f.creator);
  var groups=mock(GroupContributionController.class);when(groups.event(f.event)).thenReturn(f.event);
  var controller=new IndividualGuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  var input=new IndividualGuestGiftController.Entry(id,"Coffee","Kitchen gift","https://example.test/coffee","Alex","alex@example.test","+919876543210",IndividualGuestGiftController.Method.DIRECT_DELIVERY,null);
  tx.executeWithoutResult(s->controller.save(f.event,input,new org.springframework.mock.web.MockHttpServletRequest()));
  assertNull(db.queryForObject("SELECT contribution_paise FROM guest_gifts WHERE id=?",Long.class,id));
  assertEquals("DIRECT_DELIVERY",db.queryForObject("SELECT giving_method FROM guest_gifts WHERE id=?",String.class,id));
  assertThrows(ResponseStatusException.class,()->tx.execute(s->controller.save(f.event,input,new org.springframework.mock.web.MockHttpServletRequest())));
 }
 @Test void individualEntryValidatesGivingMethodRequirements(){
  var groups=mock(GroupContributionController.class);var controller=new IndividualGuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  assertThrows(ResponseStatusException.class,()->controller.save(UUID.randomUUID(),new IndividualGuestGiftController.Entry(null,"Gift","Description",null,"Alex","alex@example.test","+919876543210",IndividualGuestGiftController.Method.DIRECT_DELIVERY,null),new org.springframework.mock.web.MockHttpServletRequest()));
  assertThrows(ResponseStatusException.class,()->controller.save(UUID.randomUUID(),new IndividualGuestGiftController.Entry(null,"Gift","Description",null,"Alex","alex@example.test","+919876543210",IndividualGuestGiftController.Method.LINK_AND_CONTRIBUTION,null),new org.springframework.mock.web.MockHttpServletRequest()));
  verifyNoInteractions(groups);
 }

 @Test void individualHostContributionUsesSavedAmountAndIsLinkedIdempotently(){
  var f=fixture(true);var groups=mock(GroupContributionController.class);when(groups.event(f.event)).thenReturn(f.event);
  var request=new org.springframework.mock.web.MockHttpServletRequest();
  var controller=new IndividualGuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  var input=new IndividualGuestGiftController.Entry(null,"Coffee","For your kitchen","https://example.test/coffee","Alex","alex@example.test","+919876543210",IndividualGuestGiftController.Method.LINK_AND_CONTRIBUTION,50000L);
  UUID id=tx.execute(s->controller.save(f.event,input,request)).get("id"),claim=UUID.randomUUID(),destination=UUID.randomUUID();
  db.update("INSERT INTO guest_direct_transfer_claims(id,event_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key) VALUES(?,?,?,'BANK',50000,'Alex',?,?)",claim,f.event,destination,claim.toString().replace("-","")+"01234567890123456789012345678901",claim.toString());
  var transfers=mock(com.giftplatform.payment.direct.GuestDirectTransferService.class);
  org.springframework.test.util.ReflectionTestUtils.setField(controller,"transfers",transfers);
  when(transfers.claim(eq(f.event),any())).thenReturn(new com.giftplatform.payment.direct.GuestDirectTransferService.ClaimResult(claim,"AWAITING_VERIFICATION","Reported"));
  var payment=new IndividualGuestGiftController.HostContribution(destination,"BANK","BANK12345678");
  tx.executeWithoutResult(s->controller.contribute(f.event,id,payment,request));
  tx.executeWithoutResult(s->controller.contribute(f.event,id,payment,request));
  var sent=org.mockito.ArgumentCaptor.forClass(com.giftplatform.payment.direct.GuestDirectTransferService.ClaimRequest.class);
  verify(transfers,times(2)).claim(eq(f.event),sent.capture());
  assertEquals(50000L,sent.getValue().amountPaise());assertEquals("alex@example.test",sent.getValue().guestEmail());
  assertNull(sent.getValue().giftId()); // A special gift pays the host, not a different wishlist gift.
  assertEquals(sent.getAllValues().getFirst().idempotencyKey(),sent.getValue().idempotencyKey());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM guest_gift_host_contributions WHERE guest_gift_id=?",Integer.class,id));
  assertEquals(claim,controller.payment(f.event,id,request).get("claim_id"));
  assertEquals("AWAITING_VERIFICATION",controller.payment(f.event,id,request).get("payment_status"));
  assertThrows(ResponseStatusException.class,()->controller.payment(f.event,id,new org.springframework.mock.web.MockHttpServletRequest()));
  assertThrows(ResponseStatusException.class,()->controller.contribute(f.event,id,payment,new org.springframework.mock.web.MockHttpServletRequest()));
  var actor=mock(ActorContext.class);when(actor.requireAuthenticatedUserId()).thenReturn(f.host);
  var reports=new GuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class),actor);
  assertEquals(claim,reports.hostReport(f.event).getFirst().get("host_payment_claim_id"));
  var guestView=reports.list(f.event).getFirst();assertEquals("Presented with love ♡",guestView.get("selection_label"));
  assertFalse(guestView.containsKey("host_payment_claim_id"));assertFalse(guestView.containsKey("contribution_paise"));
 }
 @Test void directDeliveryDoesNotCreateAHostContribution(){
  var f=fixture(true);var groups=mock(GroupContributionController.class);when(groups.event(f.event)).thenReturn(f.event);
  var request=new org.springframework.mock.web.MockHttpServletRequest();var controller=new IndividualGuestGiftController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class));
  UUID id=tx.execute(s->controller.save(f.event,new IndividualGuestGiftController.Entry(null,"Gift",null,"https://example.test/gift","Alex","alex@example.test","+919876543210",IndividualGuestGiftController.Method.DIRECT_DELIVERY,null),request)).get("id");
  var transfers=mock(com.giftplatform.payment.direct.GuestDirectTransferService.class);org.springframework.test.util.ReflectionTestUtils.setField(controller,"transfers",transfers);
  assertThrows(ResponseStatusException.class,()->tx.execute(s->controller.contribute(f.event,id,new IndividualGuestGiftController.HostContribution(UUID.randomUUID(),"UPI","UPI12345678"),request)));
  verifyNoInteractions(transfers);
 }

 @Test void independentGiftPlansStayOutsideWishlistAndListImmediately(){
  var f=fixture(true);db.update("UPDATE gift_groups SET independent_gift=TRUE WHERE id=?",f.group);
  var groups=mock(GroupContributionController.class);when(groups.event(f.event)).thenReturn(f.event);
  when(groups.requireGroupCreator(eq(f.event),eq(f.group),any())).thenReturn(f.creator);
  var request=new org.springframework.mock.web.MockHttpServletRequest();
  var plans=new IndependentGroupGiftController(db,groups,mock(com.giftplatform.gift.retailer.RetailerPageClient.class),mock(com.giftplatform.gift.retailer.RetailerMetadataService.class));
  tx.executeWithoutResult(s->plans.plan(f.event,f.group,new IndependentGroupGiftController.Plan("Our gift","For your kitchen","https://example.test/custom",75000L),request));
  var board=new GroupGiftCompletionController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class)).board(f.event);
  assertEquals(1,board.size());assertEquals("Friends",board.getFirst().get("group_name"));assertNull(board.getFirst().get("gift_name"));
  assertEquals("Something is Cooking 🍳",board.getFirst().get("display_status"));
  assertThrows(ResponseStatusException.class,()->tx.execute(s->lifecycle.select(f.group,f.creator,f.gift)));
  UUID transfer=report(f);tx.executeWithoutResult(s->plans.plan(f.event,f.group,new IndependentGroupGiftController.Plan(null,null,null,100000L),request));
  assertNull(db.queryForObject("SELECT gift_name FROM gift_groups WHERE id=?",String.class,f.group));
  assertEquals(25000L,db.queryForObject("SELECT amount_paise FROM gift_group_guest_reports WHERE id=?",Long.class,transfer));
  var other=fixture(true);when(groups.event(other.event)).thenReturn(other.event);assertTrue(new GroupGiftCompletionController(db,groups,mock(com.giftplatform.event.repository.EventRepository.class)).board(other.event).isEmpty());
 }
}
