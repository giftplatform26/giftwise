package com.giftplatform.analytics;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import com.giftplatform.security.ActorContext;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InteractionTrackingTest {
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void routeStructureDoesNotKeepInvitationTokensQueriesOrPersonalIdentifiers(){
  assertEquals("/e/:id",InteractionService.safeRoute("/e/11111111-1111-1111-1111-111111111111?email=secret@example.org"));
  assertEquals("/api/public/payments/confirm",InteractionService.safeRoute("/api/public/payments/confirm"));
  assertEquals("external",InteractionService.safeRoute("https://pay.example/?secret=value"));
  assertEquals("/events/:id/qr",InteractionService.safeRoute("/events/12345678-abcd-abcd-abcd-123456789abc/qr#secret"));
 }
 @Test void anonymousCannotAssignAnInteractionToAnArbitraryPrivateEvent(){
  var db=mock(JdbcTemplate.class);var scope=new InteractionScope(db).resolve(UUID.randomUUID(),null);
  assertNull(scope.host());assertNull(scope.event());assertEquals("GUEST",scope.role());verifyNoInteractions(db);
 }
 @Test void reportAndTimelineAreAdminOnly(){
  var db=mock(JdbcTemplate.class);var actor=mock(ActorContext.class);var admin=new InteractionAdminController(actor,db);
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->admin.report(null,null,null,null,"","","",0));
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->admin.timeline(UUID.randomUUID()));verifyNoInteractions(db);
 }
 @Test void invalidReportDateRangeDoesNotRunDatabaseQueries(){
  var db=mock(JdbcTemplate.class);var actor=mock(ActorContext.class);when(actor.isPlatformAdmin()).thenReturn(true);
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->new InteractionAdminController(actor,db).report(null,null,LocalDate.of(2026,2,2),LocalDate.of(2026,1,1),"","","",0));verifyNoInteractions(db);
 }
 @Test void countsAreRateLimitedBeforeWritingAnOversizedSessionBurst(){
  var service=mock(InteractionService.class);var controller=new InteractionController(service);var request=new MockHttpServletRequest();request.addHeader("X-Gift-Analytics-Consent","v2");
  request.getSession().setAttribute("analytics.window",System.currentTimeMillis());request.getSession().setAttribute("analytics.count",600);
  var input=new InteractionService.Input(UUID.randomUUID(),null,"CLICK","QR_DOWNLOAD","/events/:id/qr","button","","CLICKED","",null,null,null);
  assertEquals(429,controller.collect(new InteractionController.Batch(List.of(input)),request).getStatusCode().value());verifyNoInteractions(service);
 }
 @Test void serverCapturesActualHttpFailureAndCorrelationWithoutChangingResponse() throws Exception {
  var service=mock(InteractionService.class);var filter=new InteractionRequestFilter(service);UUID event=UUID.randomUUID();var request=new MockHttpServletRequest("GET","/api/v1/events/"+event+"/qr/png");request.addHeader("X-Gift-Analytics-Consent","v2");var response=new MockHttpServletResponse();
  filter.doFilter(request,response,(req,res)->{assertNotNull(((jakarta.servlet.http.HttpServletRequest)req).getSession(false));assertNotNull(req.getAttribute("gift.analytics.visitor"));((jakarta.servlet.http.HttpServletResponse)res).setStatus(409);});
  verify(service).request(eq(request),eq(409),anyLong(),eq(event),isNull(),eq("QR_IMAGE_FETCH"));assertEquals(409,response.getStatus());
 }
 @Test void analyticsFailureDoesNotBreakDownloadResponse() throws Exception {
  var service=mock(InteractionService.class);doThrow(new RuntimeException("DB unavailable")).when(service).request(any(),anyInt(),anyLong(),any(),any(),any());
  var request=new MockHttpServletRequest("GET","/api/v1/events/"+UUID.randomUUID()+"/qr/png");request.addHeader("X-Gift-Analytics-Consent","v2");var response=new MockHttpServletResponse();
  new InteractionRequestFilter(service).doFilter(request,response,(req,res)->res.getWriter().write("download"));assertEquals("download",response.getContentAsString());
 }
 @Test void trackingDoesNotRecursivelyTrackItselfOrBackgroundCatalogReads(){
  assertNull(InteractionRequestFilter.action("POST","/api/public/interactions"));
  assertNull(InteractionRequestFilter.action("GET","/api/localization/catalog"));
  assertEquals("INVITATION_OPEN",InteractionRequestFilter.action("GET","/api/public/events/11111111-1111-1111-1111-111111111111"));
  assertEquals("PAYMENT_REQUEST",InteractionRequestFilter.action("POST","/api/public/payments/confirm"));
 }
 @Test void invalidBrowserPayloadCannotClaimServerEvidence(){
  try(var factory=jakarta.validation.Validation.buildDefaultValidatorFactory()){
   var input=new InteractionService.Input(UUID.randomUUID(),null,"REQUEST","PAYMENT_CHECKOUT","/",null,null,"SUCCESS",null,null,null,null);
   assertFalse(factory.getValidator().validate(input).isEmpty());
   assertFalse(factory.getValidator().validate(new InteractionController.Batch(Collections.singletonList(null))).isEmpty());
  }
 }

 @org.junit.jupiter.api.Test void noConsentDoesNotCreateAnAnalyticsSession()throws Exception{
  var service=mock(InteractionService.class);var request=new MockHttpServletRequest("GET","/api/public/events/"+UUID.randomUUID());var response=new MockHttpServletResponse();new InteractionRequestFilter(service).doFilter(request,response,(req,res)->{});assertNull(request.getSession(false));verifyNoInteractions(service);
 }
}
