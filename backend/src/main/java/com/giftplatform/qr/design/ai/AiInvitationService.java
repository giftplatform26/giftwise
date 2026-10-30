package com.giftplatform.qr.design.ai;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
public class AiInvitationService {
    private final EventRepository events;private final ActorContext actor;private final AiInvitationClient client;private final JdbcTemplate db;private final TransactionTemplate tx;
    public AiInvitationService(EventRepository events,ActorContext actor,AiInvitationClient client,JdbcTemplate db,TransactionTemplate tx){this.events=events;this.actor=actor;this.client=client;this.db=db;this.tx=tx;}
    public record Preferences(@NotBlank @Pattern(regexp="ELEGANT|FLORAL|PLAYFUL|MINIMAL|TRADITIONAL") String visualStyle,
        @NotBlank @Pattern(regexp="WARM|FORMAL|JOYFUL") String tone,@NotBlank @Size(max=40) String language,
        @Size(max=200) String displayNames,@Size(max=160) String publicVenue,@Size(max=40) String startTime,@Size(max=400) String specialMessage){}
    public record Settings(boolean available,String message){}
    public record EventContext(UUID eventId,String title,String occasionType,LocalDate eventDate,String hostName,
        String description,String venue,String visualStyle){}
    public EventContext context(UUID id){
        Event event=owned(id);
        String style=switch(event.getOccasionType()){
            case BIRTHDAY -> "PLAYFUL";
            case BABY_SHOWER -> "FLORAL";
            case HOUSEWARMING -> "MINIMAL";
            default -> "ELEGANT";
        };
        return new EventContext(id,event.getTitle(),event.getOccasionType().name(),event.getEventDate(),event.getHostDisplayName(),clean(event.getDescription()),venue(event),style);
    }
    private static String venue(Event e){
        for(String[] parts:List.of(new String[]{e.getAddressLine1(),e.getCity(),e.getState(),e.getPostalCode(),e.getCountry()},
            new String[]{e.getAddressLine1(),e.getCity(),e.getState()},new String[]{e.getCity(),e.getState()},new String[]{e.getCity()})){
            String value=Arrays.stream(parts).filter(v->v!=null&&!v.isBlank()).map(String::strip).collect(java.util.stream.Collectors.joining(", "));
            if(value.length()<=160)return value;
        }
        return "";
    }
    public record Suggestion(UUID eventId,String eventTitle,String occasionType,String hostName,LocalDate eventDate,
        String publicVenue,String startTime,AiInvitationClient.Blueprint blueprint){}
    private Event owned(UUID id){
        UUID host=actor.requireAuthenticatedUserId();return events.findById(id).filter(e->host.equals(e.getOwnerId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
    }
    public Settings settings(UUID id){owned(id);return new Settings(client.available(),client.available()?"Ready to design":"AI design is not configured. Ask the administrator to set OPENAI_API_KEY.");}
    public Suggestion generate(UUID id,Preferences preferences){
        Event event=owned(id);
        if(!client.available())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"AI design is not configured. Set OPENAI_API_KEY in the backend environment.");
        UUID host=event.getOwnerId(),request=UUID.randomUUID();
        tx.executeWithoutResult(s->{
            db.queryForObject("SELECT id FROM users WHERE id=? FOR UPDATE",UUID.class,host);
            Integer count=db.queryForObject("SELECT count(*) FROM invitation_ai_requests WHERE host_id=? AND created_at>now()-interval '1 hour'",Integer.class,host);
            if(count!=null&&count>=10)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"You have reached the hourly AI design limit. Please try again later.");
            db.update("INSERT INTO invitation_ai_requests(id,host_id,event_id,status) VALUES(?,?,?,'PENDING')",request,host,id);
        });
        String names=clean(preferences.displayNames());if(names.isEmpty())names=event.getHostDisplayName();
        Map<String,Object> details=new LinkedHashMap<>();details.put("eventTitle",event.getTitle());details.put("occasion",event.getOccasionType().name());details.put("hostNames",names);
        if(event.getEventDate()!=null)details.put("eventDate",event.getEventDate().toString());
        details.put("visualStyle",preferences.visualStyle());details.put("tone",preferences.tone());details.put("language",preferences.language());
        details.put("publicVenue",clean(preferences.publicVenue()));details.put("startTime",clean(preferences.startTime()));details.put("personalMessage",clean(preferences.specialMessage()));
        try{
            var blueprint=client.generate(details);db.update("UPDATE invitation_ai_requests SET status='COMPLETED' WHERE id=?",request);
            return new Suggestion(id,event.getTitle(),event.getOccasionType().name(),names,event.getEventDate(),clean(preferences.publicVenue()),clean(preferences.startTime()),blueprint);
        }catch(RuntimeException ex){db.update("UPDATE invitation_ai_requests SET status='FAILED' WHERE id=?",request);throw ex;}
    }
    private static String clean(String value){return value==null?"":value.strip();}
}
