package com.giftplatform.notification.email;
import tools.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@EnableScheduling
public class TransactionalEmailOutbox {
 private final JdbcTemplate db;private final BrandedEmailService email;private final ObjectMapper json;
 public TransactionalEmailOutbox(JdbcTemplate db,BrandedEmailService email,ObjectMapper json){this.db=db;this.email=email;this.json=json;}
 @Transactional public void enqueue(String source,String template,String recipient,Map<String,String> variables,String language){if(recipient==null||recipient.isBlank())return;try{db.update("INSERT INTO transactional_email_outbox(id,source_key,template_key,recipient,variables,language_code) VALUES(?,?,?,?,?::jsonb,?) ON CONFLICT(source_key) DO NOTHING",UUID.randomUUID(),source,template,recipient,json.writeValueAsString(variables),language==null?"en":language);}catch(tools.jackson.core.JacksonException ex){throw new IllegalArgumentException("Unable to prepare email.");}}
 @Scheduled(fixedDelayString="${gift.email.outbox-delay-ms:30000}",initialDelayString="${gift.email.outbox-initial-delay-ms:60000}")
 public void deliver(){if(!email.available())return;
  db.update("UPDATE transactional_email_outbox SET status='FAILED',last_error_code='RETRY_LIMIT_REACHED' WHERE status IN ('PENDING','SENDING') AND (created_at<=now()-interval '20 hours' OR (attempts>=6 AND next_attempt_at<=now()))");
  // Recover abandoned leases; one atomic claim works across application instances.
  for(int i=0;i<10;i++){
   var rows=db.queryForList("""
    UPDATE transactional_email_outbox SET status='SENDING',attempts=attempts+1,next_attempt_at=now()+interval '5 minutes'
    WHERE id=(SELECT id FROM transactional_email_outbox WHERE status IN ('PENDING','FAILED','SENDING')
      AND attempts<6 AND next_attempt_at<=now() AND created_at>now()-interval '20 hours'
      ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT 1)
    RETURNING id,template_key,recipient,variables::text,rendered_email::text,language_code,attempts
    """);if(rows.isEmpty())break;var row=rows.get(0);UUID id=(UUID)row.get("id");
   try{Map<String,String> values=json.readValue((String)row.get("variables"),new tools.jackson.core.type.TypeReference<Map<String,String>>(){});BrandedEmailService.Email rendered=row.get("rendered_email")==null?email.render((String)row.get("template_key"),values,(String)row.get("language_code")):json.readValue((String)row.get("rendered_email"),BrandedEmailService.Email.class);if(row.get("rendered_email")==null)db.update("UPDATE transactional_email_outbox SET rendered_email=?::jsonb WHERE id=?",json.writeValueAsString(rendered),id);email.send((String)row.get("recipient"),rendered,"giftwise-email-"+id);db.update("UPDATE transactional_email_outbox SET status='SENT',sent_at=now(),last_error_code=NULL WHERE id=?",id);}
   catch(Exception ex){db.update("UPDATE transactional_email_outbox SET status='FAILED',last_error_code='DELIVERY_FAILED',next_attempt_at=now()+interval '10 minutes' WHERE id=?",id);}
  }
 }
 public void registration(UUID id,String address){enqueue("registration:"+id,"registration",address,Map.of("signInUrl",email.url("/login")),"en");}
 public void completed(UUID event){var rows=db.queryForList("SELECT e.title,e.owner_id,u.email,COALESCE(p.event_notifications,true) AS enabled FROM events e JOIN users u ON u.id=e.owner_id LEFT JOIN user_profile_preferences p ON p.user_id=u.id WHERE e.id=?",event);if(rows.size()==1&&Boolean.TRUE.equals(rows.get(0).get("enabled"))){var r=rows.get(0);enqueue("completed:"+event,"event-completed",(String)r.get("email"),Map.of("eventName",(String)r.get("title"),"eventUrl",email.url("/events/"+event)),"en");}}
 public void captured(UUID contribution){var rows=db.queryForList("SELECT c.guest_email,c.amount_paise,e.title,e.id AS event_id,e.owner_id,e.public_token,u.email AS host_email,COALESCE(p.contribution_notifications,true) AS enabled FROM contributions c JOIN payment_orders po ON po.id=c.payment_order_id AND po.status='CAPTURED' JOIN events e ON e.id=c.event_id JOIN users u ON u.id=e.owner_id LEFT JOIN user_profile_preferences p ON p.user_id=u.id WHERE c.id=?",contribution);if(rows.size()!=1)return;var r=rows.get(0);String amount="INR "+java.math.BigDecimal.valueOf(((Number)r.get("amount_paise")).longValue(),2).toPlainString();
  enqueue("payment:"+contribution,"payment-success",(String)r.get("guest_email"),Map.of("formattedAmount",amount,"recipientName",(String)r.get("title"),"transactionReference",contribution.toString(),"paymentUrl",email.url("/e/"+r.get("public_token"))),"en");
  if(Boolean.TRUE.equals(r.get("enabled")))enqueue("contribution:"+contribution,"contribution-received",(String)r.get("host_email"),Map.of("eventName",(String)r.get("title"),"formattedAmount",amount,"transactionReference",contribution.toString(),"contributionUrl",email.url("/events/"+r.get("event_id")+"/contributions")),"en");
 }
}
