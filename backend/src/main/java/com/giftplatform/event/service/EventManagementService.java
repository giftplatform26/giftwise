package com.giftplatform.event.service;

import com.giftplatform.audit.entity.*;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.dto.*;
import com.giftplatform.event.entity.*;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class EventManagementService {
    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.monetization.CommercialAccessService commerce;
    private final EventRepository repository;
    private final ActorContext actors;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.notification.email.TransactionalEmailOutbox emails;
    private final AuditService audit;
    public EventManagementService(EventRepository repository, ActorContext actors, AuditService audit, EventPublishReadiness readiness) {
        this.readiness=readiness; this.repository=repository; this.actors=actors; this.audit=audit;
    }
    private final EventPublishReadiness readiness;
    private EventResponse response(Event e) { return EventResponse.from(e,e.getStatus()==EventStatus.DRAFT && readiness.canPublish(e),readiness.reasons(e)); }
    @Transactional
    public EventResponse create(EventRequest input) {
        UUID actor=actors.requireAuthenticatedUserId();
        Event event=new Event(input.title().trim(), input.occasionType());
        validateContact(input);
        applyDelivery(event,input);
        event.assignOwner(actor);
        event.setHostDisplayName(input.hostName().trim());
        event.setEventContact(input.addressLine1().trim(),input.city().trim(),input.state().trim(),
            input.postalCode().trim(),input.country().trim(),input.contactName().trim(),
            input.contactPhone().trim(),input.contactEmail()==null?null:input.contactEmail().trim());
        event.update(input.title().trim(), input.description(), input.occasionType(), input.eventDate());
        repository.saveAndFlush(event);
        log(actor,event,AuditAction.CREATE,null,snapshot(event));
        return response(event);
    }
    @Transactional(readOnly=true)
    public EventResponse get(UUID id) { return response(owned(id)); }
    @Transactional(readOnly=true)
    public List<EventResponse> list() {
        return repository.findByOwnerIdOrderByCreatedAtDesc(actors.requireAuthenticatedUserId())
                .stream().map(this::response).toList();
    }
    @Transactional
    public EventResponse update(UUID id, EventRequest input) {
        Event event=owned(id);
        if (event.getStatus()!=EventStatus.DRAFT&&event.getStatus()!=EventStatus.PUBLISHED) throw new ResponseStatusException(HttpStatus.CONFLICT,"Completed or cancelled celebrations can no longer be edited");
        Map<String,Object> before=snapshot(event);
        validateContact(input);
        applyDelivery(event,input);
        event.setEventContact(input.addressLine1().trim(),input.city().trim(),input.state().trim(),
            input.postalCode().trim(),input.country().trim(),input.contactName().trim(),
            input.contactPhone().trim(),input.contactEmail()==null?null:input.contactEmail().trim());
        event.setHostDisplayName(input.hostName().trim());
        event.update(input.title().trim(),input.description(),input.occasionType(),input.eventDate());
        repository.saveAndFlush(event);
        log(actors.requireAuthenticatedUserId(),event,AuditAction.UPDATE,before,snapshot(event));
        return response(event);
    }
    @Transactional
    public EventResponse changeStatus(UUID id, EventStatus next) {
        Event event=owned(id);
        if(next==EventStatus.PUBLISHED && event.getStatus()!=EventStatus.PUBLISHED && commerce!=null)commerce.requireEventCapacity(event.getOwnerId(),event.getId());
        if(next==EventStatus.PUBLISHED)event=repository.findForUpdate(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        Map<String,Object> before=snapshot(event);
        if(next==EventStatus.PUBLISHED && !readiness.canPublish(event))
            throw new ResponseStatusException(HttpStatus.CONFLICT,String.join("; ",readiness.reasons(event)));
        try { event.transition(next); }
        catch (IllegalStateException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid status transition"); }
        repository.saveAndFlush(event);
        log(actors.requireAuthenticatedUserId(),event,AuditAction.STATUS_CHANGE,before,snapshot(event));
        if(next==EventStatus.COMPLETED&&emails!=null)emails.completed(event.getId());
        return response(event);
    }
    private void applyDelivery(Event event,EventRequest input){
        boolean same=!Boolean.FALSE.equals(input.deliverySameAsEvent());
        var a=input.deliveryAddress();
        if(!same && (a==null || a.addressLine1()==null || a.addressLine1().isBlank()
            || a.city()==null || a.city().isBlank() || a.state()==null || a.state().isBlank()
            || a.postalCode()==null || a.postalCode().isBlank() || a.country()==null || a.country().isBlank()
            || ("India".equalsIgnoreCase(a.country()) && !a.postalCode().matches("[1-9][0-9]{5}"))))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Valid delivery address required");
        event.setDeliveryAddress(same,a);
    }
    private void validateContact(EventRequest r){
      if(r.addressLine1()==null||r.addressLine1().isBlank()||r.city()==null||r.city().isBlank()
        ||r.state()==null||r.state().isBlank()||r.postalCode()==null||r.postalCode().isBlank()
        ||r.country()==null||r.country().isBlank()||r.contactName()==null||r.contactName().isBlank()
        ||r.contactPhone()==null||!r.contactPhone().matches("[+0-9()\\- ]{7,30}")
        ||(r.contactEmail()!=null&&!r.contactEmail().isBlank()&&!r.contactEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
        ||(r.country()!=null&&r.country().equalsIgnoreCase("India")&&!r.postalCode().matches("[1-9][0-9]{5}")))
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Valid event address and contact details are required");
    }
    private Event owned(UUID id) {
        UUID actor=actors.requireAuthenticatedUserId();
        Event e=repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!actor.equals(e.getOwnerId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return e;
    }
    private Map<String,Object> snapshot(Event e) {
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("ownerId",e.getOwnerId().toString()); m.put("title",e.getTitle());
        m.put("hostName",e.getHostDisplayName());
        m.put("occasionType",e.getOccasionType().name()); m.put("status",e.getStatus().name());
        if(e.getDescription()!=null)m.put("description",e.getDescription());
        if(e.getEventDate()!=null)m.put("eventDate",e.getEventDate().toString());
        if(e.getAddressLine1()!=null)m.put("addressLine1",e.getAddressLine1());
        if(e.getCity()!=null)m.put("city",e.getCity());
        if(e.getState()!=null)m.put("state",e.getState());
        if(e.getPostalCode()!=null)m.put("postalCode",e.getPostalCode());
        if(e.getCountry()!=null)m.put("country",e.getCountry());
        if(e.getContactName()!=null)m.put("contactName",e.getContactName());
        if(e.getContactPhone()!=null)m.put("contactPhone",e.getContactPhone());
        if(e.getContactEmail()!=null)m.put("contactEmail",e.getContactEmail());
        m.put("deliverySameAsEvent",e.isDeliverySameAsEvent());
        m.put("deliveryAddress",e.getDeliveryAddress());
        return m;
    }
    private void log(UUID actor,Event e,AuditAction action,Map<String,Object> oldValues,Map<String,Object> newValues) {
        audit.recordChange(actor,AuditActorType.CUSTOMER,"EVENT",e.getId(),action,oldValues,newValues,null);
    }
}
