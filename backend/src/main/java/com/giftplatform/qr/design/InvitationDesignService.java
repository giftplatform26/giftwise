package com.giftplatform.qr.design;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvitationDesignService {
 private final EventRepository events;private final InvitationDesignRepository designs;
 private final ActorContext actors;private final AuditService audit;
 private final InvitationImageProcessor images;private final InvitationComposer composer;private final String base;
 public InvitationDesignService(EventRepository events,InvitationDesignRepository designs,ActorContext actors,
    AuditService audit,InvitationImageProcessor images,InvitationComposer composer,
    @Value("${gift.guest-base-url:http://localhost:3000}") String base){
  this.events=events;this.designs=designs;this.actors=actors;this.audit=audit;
  this.images=images;this.composer=composer;this.base=base.replaceAll("/+$","");
 }
 @Transactional
 public DesignResponse create(UUID eventId,String name,InvitationStyle style,String greeting,byte[] upload){
  Event event=owned(eventId);
  if(event.getStatus()==EventStatus.COMPLETED||event.getStatus()==EventStatus.CANCELLED)
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Event cannot be edited");
  if(name==null||name.isBlank()||name.length()>100||style==null||greeting!=null&&greeting.length()>180)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid invitation details");
  byte[] clean=images.sanitize(upload);
  InvitationDesign d=designs.saveAndFlush(new InvitationDesign(event,name.trim(),style,greeting,clean));
  audit.recordChange(actors.requireAuthenticatedUserId(),AuditActorType.CUSTOMER,"QR_INVITATION",d.getId(),
   AuditAction.CREATE,null,Map.of("eventId",eventId.toString(),"style",style.name()),null);
  return response(d);
 }
 @Transactional(readOnly=true)
 public List<DesignResponse> list(UUID eventId){owned(eventId);return designs.findByEventIdOrderByIdAsc(eventId).stream().map(this::response).toList();}
 @Transactional(readOnly=true)
 public byte[] png(UUID eventId,UUID designId){
  Event e=owned(eventId);InvitationDesign d=designs.findById(designId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!d.getEvent().getId().equals(e.getId()))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return composer.render(base+"/e/"+e.getPublicToken(),e.getTitle(),d.getGreeting(),d.getStyle(),d.getImagePng());
 }
 private Event owned(UUID id){
  UUID actor=actors.requireAuthenticatedUserId();Event e=events.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!actor.equals(e.getOwnerId()))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return e;
 }
 private DesignResponse response(InvitationDesign d){return new DesignResponse(d.getId(),d.getEvent().getId(),d.getName(),d.getStyle(),d.getGreeting(),d.getImagePng()!=null);}
 public record DesignResponse(UUID id,UUID eventId,String name,InvitationStyle style,String greeting,boolean hasImage){}
}
