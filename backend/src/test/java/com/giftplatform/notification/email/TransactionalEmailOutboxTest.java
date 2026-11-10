package com.giftplatform.notification.email;
import tools.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class TransactionalEmailOutboxTest {
 @Test void disabledDeliveryDoesNotClaimOrSendMessages(){var db=mock(JdbcTemplate.class);var mail=mock(BrandedEmailService.class);new TransactionalEmailOutbox(db,mail,new ObjectMapper()).deliver();verifyNoInteractions(db);verify(mail,never()).send(anyString(),any(),anyString());}
 @Test void retryUsesSavedContentAndStableIdempotencyKey()throws Exception{
  var db=mock(JdbcTemplate.class);var mail=mock(BrandedEmailService.class);var json=new ObjectMapper();UUID id=UUID.randomUUID();var saved=new BrandedEmailService.Email("Original brand","Receipt","<p>Receipt</p>");
  when(mail.available()).thenReturn(true);Map<String,Object> row=new HashMap<>(Map.of("id",id,"template_key","registration","recipient","guest@example.com","variables","{}","language_code","en","attempts",2,"rendered_email",json.writeValueAsString(saved)));
  when(db.queryForList(contains("RETURNING"))).thenReturn(List.of(row),List.of());
  new TransactionalEmailOutbox(db,mail,json).deliver();verify(mail).send("guest@example.com",saved,"giftwise-email-"+id);verify(mail,never()).render(anyString(),anyMap(),anyString());verify(db).update(contains("status='SENT'"),eq(id));
 }
 @Test void failureRemainsRetryableAndNeverBecomesSent()throws Exception{
  var db=mock(JdbcTemplate.class);var mail=mock(BrandedEmailService.class);when(mail.available()).thenReturn(true);UUID id=UUID.randomUUID();var rendered=new BrandedEmailService.Email("Receipt","Receipt","<p>Receipt</p>");
  when(db.queryForList(contains("RETURNING"))).thenReturn(List.of(new HashMap<>(Map.of("id",id,"template_key","registration","recipient","guest@example.com","variables","{}","language_code","en","attempts",1))),List.of());when(mail.render(anyString(),anyMap(),anyString())).thenReturn(rendered);doThrow(new IllegalStateException("Provider unavailable")).when(mail).send(anyString(),any(),anyString());
  new TransactionalEmailOutbox(db,mail,new ObjectMapper()).deliver();verify(db).update(contains("last_error_code='DELIVERY_FAILED'"),eq(id));verify(db,never()).update(contains("status='SENT'"),eq(id));
 }
}
