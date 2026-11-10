package com.giftplatform.notification.email;
import com.giftplatform.security.ActorContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController
@RequestMapping("/api/v1/events/{eventId}/invitation-email")
public class EventInvitationEmailController {
 private final JdbcTemplate db;private final ActorContext actor;private final TransactionalEmailOutbox outbox;private final BrandedEmailService service;
 public EventInvitationEmailController(JdbcTemplate db,ActorContext actor,TransactionalEmailOutbox outbox,BrandedEmailService service){this.db=db;this.actor=actor;this.outbox=outbox;this.service=service;}
 public record Invitation(String email,UUID requestId){}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED) @Transactional public Map<String,String> send(@PathVariable UUID eventId,@RequestBody Invitation input){UUID user=actor.requireAuthenticatedUserId();var rows=db.queryForList("SELECT title,host_display_name,event_date,public_token FROM events WHERE id=? AND owner_id=? AND status='PUBLISHED' FOR UPDATE",eventId,user);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Publish your event before sending invitations.");if(input==null||input.email()==null||input.email().length()>254||!input.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||input.requestId()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Please enter a valid email address.");if(!service.available())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email delivery is not configured. Contact platform support.");Long count=db.queryForObject("SELECT count(*) FROM transactional_email_outbox WHERE source_key LIKE ? AND created_at>now()-interval '1 hour'",Long.class,"invitation:"+eventId+":%");if(count!=null&&count>=100)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Invitation email limit reached. Try again later.");var e=rows.get(0);outbox.enqueue("invitation:"+eventId+":"+input.requestId(),"event-invitation",input.email(),Map.of("eventName",(String)e.get("title"),"hostName",Objects.toString(e.get("host_display_name"),"Your host"),"eventDate",Objects.toString(e.get("event_date"),"See invitation"),"invitationUrl",service.url("/e/"+e.get("public_token"))),"en");return Map.of("message","Invitation email queued successfully.");}
}
