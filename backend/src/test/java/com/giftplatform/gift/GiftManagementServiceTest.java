package com.giftplatform.gift;

import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.entity.*;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.gift.dto.GiftRequest;
import com.giftplatform.gift.entity.*;
import com.giftplatform.gift.repository.GiftRepository;
import com.giftplatform.gift.service.GiftManagementService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GiftManagementServiceTest {
    private final EventRepository events = mock(EventRepository.class);
    private final GiftRepository gifts = mock(GiftRepository.class);
    private final ActorContext actors = mock(ActorContext.class);
    private final AuditService audit = mock(AuditService.class);
    private final GiftManagementService service = new GiftManagementService(events, gifts, actors, audit);
    private final UUID user = UUID.randomUUID();
    private UUID eventId;
    private Event event;
    private Gift gift;

    @BeforeEach
    void setup() {
        event = new Event("Wedding", OccasionType.WEDDING);
        eventId = event.getId();
        event.assignOwner(user);

        gift = new Gift(event, "Mixer", new BigDecimal("10000.00"));

        when(actors.requireAuthenticatedUserId()).thenReturn(user);
        when(events.findById(eventId)).thenReturn(Optional.of(event));
        when(gifts.findById(gift.getId())).thenReturn(Optional.of(gift));
        when(gifts.saveAndFlush(any(Gift.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private GiftRequest request(FulfillmentPreference override) {
        return new GiftRequest("Mixer", "Kitchen appliance", null, new BigDecimal("10000.00"), override);
    }
    private void expectStatus(HttpStatus expected, Runnable action) {
        assertEquals(expected, assertThrows(ResponseStatusException.class, action::run).getStatusCode());
    }

    @Test void createInDraftAuditsChange() {
        var result = service.create(eventId, request(null));
        assertEquals("Mixer", result.name());
        assertEquals(FulfillmentPreference.AFTER_EVENT_CONFIRMATION, result.effectiveFulfillmentPreference());
        verify(gifts).saveAndFlush(any(Gift.class));
        verify(audit).recordChange(eq(user), any(), eq("GIFT"), any(), any(), isNull(), anyMap(), isNull());
    }
    @Test void savingRetailerGiftLinksServerFetchedAuditToSavedGift() {
        var retailer=mock(com.giftplatform.gift.retailer.RetailerMetadataService.class);
        var withRetailer=new GiftManagementService(events,gifts,actors,audit,retailer);
        UUID lookup=UUID.randomUUID();
        var input=new GiftRequest("Mixer","Kitchen appliance",null,new BigDecimal("999.95"),null,
            GiftPriority.MEDIUM,"https://retailer.com/mixer",lookup);
        var result=withRetailer.create(eventId,input);
        assertEquals("https://retailer.com/mixer",result.productUrl());
        verify(retailer).attach(lookup,eventId,result.id(),user,"https://retailer.com/mixer");
    }
    @Test void createAllowsExplicitOverride() {
        var result = service.create(eventId, request(FulfillmentPreference.DELIVER_WHEN_FUNDED));
        assertEquals(FulfillmentPreference.DELIVER_WHEN_FUNDED, result.effectiveFulfillmentPreference());
    }
    @Test void foreignEventIsHidden() {
        event = new Event("Other", OccasionType.WEDDING);
        event.assignOwner(UUID.randomUUID());
        when(events.findById(eventId)).thenReturn(Optional.of(event));
        expectStatus(HttpStatus.NOT_FOUND, () -> service.create(eventId, request(null)));
        verifyNoInteractions(audit);
    }
    @Test void missingEventIsNotFound() {
        when(events.findById(eventId)).thenReturn(Optional.empty());
        expectStatus(HttpStatus.NOT_FOUND, () -> service.list(eventId));
    }
    @Test void listUsesEventFilter() {
        when(gifts.findByEventIdOrderByCreatedAtAsc(eventId)).thenReturn(List.of(gift));
        assertEquals(1, service.list(eventId).size());
    }
    @Test
    void readGiftFromDifferentEventIsHidden() {
        Event anotherEvent = new Event("Birthday", OccasionType.BIRTHDAY);
        anotherEvent.assignOwner(user);

        Gift anotherGift = new Gift(
                anotherEvent,
                "Camera",
                new BigDecimal("15000.00")
        );

        when(gifts.findById(anotherGift.getId()))
                .thenReturn(Optional.of(anotherGift));

        expectStatus(
                HttpStatus.NOT_FOUND,
                () -> service.get(eventId, anotherGift.getId())
        );
    }
    @Test void updateActiveGiftAudits() {
        var result = service.update(eventId, gift.getId(), request(FulfillmentPreference.DELIVER_WHEN_FUNDED));
        assertEquals(FulfillmentPreference.DELIVER_WHEN_FUNDED, result.fulfillmentOverride());
        verify(audit).recordChange(eq(user), any(), eq("GIFT"), eq(gift.getId()), any(), anyMap(), anyMap(), isNull());
    }
    @Test void cancelDraftGiftSucceeds() {
        var result = service.cancel(eventId, gift.getId());
        assertEquals(GiftStatus.CANCELLED, result.status());
    }
    @Test void cancelPublishedGiftRejected() {
        event.transition(EventStatus.PUBLISHED);
        expectStatus(HttpStatus.CONFLICT, () -> service.cancel(eventId, gift.getId()));
    }
    @Test void completedEventCannotCreateGift() {
        event.transition(EventStatus.PUBLISHED);
        event.transition(EventStatus.COMPLETED);
        expectStatus(HttpStatus.CONFLICT, () -> service.create(eventId, request(null)));
    }
    @Test void cancelledGiftCannotBeUpdated() {
        gift.cancel();
        expectStatus(HttpStatus.CONFLICT, () -> service.update(eventId, gift.getId(), request(null)));
    }
    @Test void invalidGiftAmountRejectedByDomain() {
        assertThrows(IllegalArgumentException.class, () -> service.create(eventId,
                new GiftRequest("Mixer", null, null, BigDecimal.ZERO, null)));
        verifyNoInteractions(audit);
    }
}
