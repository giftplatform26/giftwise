package com.giftplatform.gift.service;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.gift.dto.GiftRequest;
import com.giftplatform.gift.dto.GiftResponse;
import com.giftplatform.gift.entity.Gift;
import com.giftplatform.gift.entity.GiftStatus;
import com.giftplatform.gift.entity.GiftVisibility;
import com.giftplatform.gift.repository.GiftRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GiftManagementService {
    private final EventRepository events;
    private final GiftRepository gifts;
    private final ActorContext actors;
    private final AuditService audit;

    private final com.giftplatform.gift.retailer.RetailerMetadataService retailer;
    private final com.giftplatform.gift.photo.GiftPhotoService photos;
    @org.springframework.beans.factory.annotation.Autowired
    public GiftManagementService(EventRepository events, GiftRepository gifts, ActorContext actors, AuditService audit,
                                 com.giftplatform.gift.retailer.RetailerMetadataService retailer,com.giftplatform.gift.photo.GiftPhotoService photos) {
        this.events=events;this.gifts=gifts;this.actors=actors;this.audit=audit;this.retailer=retailer;this.photos=photos;
    }
    public GiftManagementService(EventRepository events,GiftRepository gifts,ActorContext actors,AuditService audit,com.giftplatform.gift.retailer.RetailerMetadataService retailer){this(events,gifts,actors,audit,retailer,null);}
    public GiftManagementService(EventRepository events,GiftRepository gifts,ActorContext actors,AuditService audit){this(events,gifts,actors,audit,null,null);}

    @Transactional
    public GiftResponse create(UUID eventId, GiftRequest request) {
        UUID actor = actors.requireAuthenticatedUserId();
        Event event = ownedEvent(eventId, actor);
        ensureEditable(event);
        Gift gift = new Gift(event, request.name(), request.targetAmount());
        UUID lookup=request.retailerLookupId();
        if(retailer!=null && lookup==null && request.productUrl()!=null && !request.productUrl().isBlank() && !request.productUrl().trim().equals(gift.getProductUrl()))
            lookup=retailer.lookup(eventId,request.productUrl().trim()).lookupId();
        if(photos!=null)photos.validateReference(eventId,request.imageKey());
        apply(gift, request);
        gifts.saveAndFlush(gift);
        if(lookup!=null) {
            if(retailer==null)throw new IllegalStateException("Retailer audit service unavailable");
            retailer.attach(lookup,eventId,gift.getId(),actor,gift.getProductUrl());
        }
        record(actor, gift, AuditAction.CREATE, null, snapshot(gift));
        return GiftResponse.from(gift);
    }

    @Transactional(readOnly = true)
    public List<GiftResponse> list(UUID eventId) {
        ownedEvent(eventId, actors.requireAuthenticatedUserId());
        return gifts.findByEventIdOrderByCreatedAtAsc(eventId).stream().map(GiftResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GiftResponse get(UUID eventId, UUID giftId) {
        UUID actor = actors.requireAuthenticatedUserId();
        ownedEvent(eventId, actor);
        return GiftResponse.from(ownedGift(eventId, giftId));
    }

    @Transactional
    public GiftResponse update(UUID eventId, UUID giftId, GiftRequest request) {
        UUID actor = actors.requireAuthenticatedUserId();
        Event event = ownedEvent(eventId, actor);
        ensureEditable(event);
        Gift gift = ownedGift(eventId, giftId);
        ensureActive(gift);
        Map<String, Object> before = snapshot(gift);
        UUID lookup=request.retailerLookupId();
        if(retailer!=null && lookup==null && request.productUrl()!=null && !request.productUrl().isBlank() && !request.productUrl().trim().equals(gift.getProductUrl()))
            lookup=retailer.lookup(eventId,request.productUrl().trim()).lookupId();
        if(photos!=null)photos.validateReference(eventId,request.imageKey());
        apply(gift, request);
        gifts.saveAndFlush(gift);
        if(lookup!=null) {
            if(retailer==null)throw new IllegalStateException("Retailer audit service unavailable");
            retailer.attach(lookup,eventId,gift.getId(),actor,gift.getProductUrl());
        }
        record(actor, gift, AuditAction.UPDATE, before, snapshot(gift));
        return GiftResponse.from(gift);
    }

    @Transactional
    public GiftResponse cancel(UUID eventId, UUID giftId) {
        UUID actor = actors.requireAuthenticatedUserId();
        Event event = ownedEvent(eventId, actor);
        // Once contributions are introduced, this endpoint must check reserved funds before allowing cancellation.
        // Until then, only DRAFT events can cancel gifts, where guest contributions cannot exist.
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gift cancellation requires a draft event");
        }
        Gift gift = ownedGift(eventId, giftId);
        ensureActive(gift);
        Map<String, Object> before = snapshot(gift);
        gift.cancel();
        gifts.saveAndFlush(gift);
        record(actor, gift, AuditAction.STATUS_CHANGE, before, snapshot(gift));
        return GiftResponse.from(gift);
    }

    @Transactional
    public GiftResponse setPublication(UUID eventId, UUID giftId, GiftVisibility visibility) {
        UUID actor = actors.requireAuthenticatedUserId();
        Event event = ownedEvent(eventId, actor);
        ensureEditable(event);
        Gift gift = ownedGift(eventId, giftId);
        ensureActive(gift);
        Map<String, Object> before = snapshot(gift);
        gift.setVisibility(visibility);
        gifts.saveAndFlush(gift);
        record(actor, gift, AuditAction.UPDATE, before, snapshot(gift));
        return GiftResponse.from(gift);
    }

    @Transactional
    public GiftResponse setProductLink(UUID eventId, UUID giftId, String url) {
        UUID actor = actors.requireAuthenticatedUserId();
        Event event = ownedEvent(eventId, actor);
        ensureEditable(event);
        Gift gift = ownedGift(eventId, giftId);
        ensureActive(gift);
        if (url != null && !url.isBlank()) {
            try {
                java.net.URI uri = java.net.URI.create(url);
                if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null ||
                    uri.getRawUserInfo() != null || url.length() > 2048 ||
                    uri.getHost().equalsIgnoreCase("localhost") ||
                    uri.getHost().endsWith(".localhost") ||
                    uri.getHost().endsWith(".local") ||
                    uri.getHost().matches("(?i)(127\\..*|10\\..*|192\\.168\\..*|172\\.(1[6-9]|2[0-9]|3[01])\\..*)"))
                    throw new IllegalArgumentException("Invalid product URL");
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product URL");
            }
        }
        UUID lookup=null;
        if(retailer!=null && url!=null && !url.isBlank() && !url.equals(gift.getProductUrl()))lookup=retailer.lookup(eventId,url).lookupId();
        Map<String, Object> before = snapshot(gift);
        gift.setProductUrl(url == null || url.isBlank() ? null : url);
        gifts.saveAndFlush(gift);
        if(lookup!=null)retailer.attach(lookup,eventId,giftId,actor,gift.getProductUrl());
        // Do not store retailer URLs in administrator audit payloads.
        record(actor, gift, AuditAction.UPDATE, before, snapshot(gift));
        return GiftResponse.from(gift);
    }

    private Event ownedEvent(UUID id, UUID actor) {
        Event event = events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!actor.equals(event.getOwnerId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return event;
    }

    private Gift ownedGift(UUID eventId, UUID giftId) {
        Gift gift = gifts.findById(giftId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!eventId.equals(gift.getEvent().getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return gift;
    }

    private void ensureEditable(Event event) {
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.PUBLISHED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event cannot be edited");
    }

    private void ensureActive(Gift gift) {
        if (gift.getStatus() != GiftStatus.ACTIVE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gift is not active");
    }

    private void apply(Gift gift, GiftRequest request) {
        if(request.productUrl()!=null) {
            String url=request.productUrl().trim();
            try{if(!url.isEmpty())com.giftplatform.gift.retailer.RetailerPageClient.validateUrl(url);}
            catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());}
            gift.setProductUrl(url.isEmpty()?null:url);
        }
        gift.setDetails(request.name(), request.description(), request.imageKey(), request.targetAmount());
        gift.setFulfillmentOverride(request.fulfillmentOverride());
        gift.setPriority(request.priority() == null ? com.giftplatform.gift.entity.GiftPriority.MEDIUM : request.priority());
    }

    private Map<String, Object> snapshot(Gift gift) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("eventId", gift.getEvent().getId().toString());
        values.put("name", gift.getName());
        values.put("targetAmount", gift.getTargetAmount().toPlainString());
        values.put("priority", gift.getPriority().name());
        values.put("status", gift.getStatus().name());
        values.put("visibility", gift.getVisibility().name());
        values.put("effectiveFulfillmentPreference", gift.effectiveFulfillmentPreference().name());
        if (gift.getFulfillmentOverride() != null) values.put("fulfillmentOverride", gift.getFulfillmentOverride().name());
        // Deliberately omit image storage paths, shipping details, guest details and financial secrets.
        return values;
    }

    private void record(UUID actor, Gift gift, AuditAction action,
                        Map<String, Object> before, Map<String, Object> after) {
        audit.recordChange(actor, AuditActorType.CUSTOMER, "GIFT", gift.getId(), action, before, after, null);
    }
}
