package com.giftplatform.gift;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.gift.entity.Gift;
import com.giftplatform.gift.entity.GiftVisibility;
import com.giftplatform.gift.repository.GiftRepository;
import com.giftplatform.gift.service.GiftManagementService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GiftPublicationSecurityTest {
    private final EventRepository events = mock(EventRepository.class);
    private final GiftRepository gifts = mock(GiftRepository.class);
    private final ActorContext actors = mock(ActorContext.class);
    private final AuditService audit = mock(AuditService.class);
    private final GiftManagementService service = new GiftManagementService(events, gifts, actors, audit);
    private final UUID owner = UUID.randomUUID();
    private Event event;
    private Gift gift;

    @BeforeEach void setup() {
        event = new Event("Wedding", OccasionType.WEDDING);
        event.assignOwner(owner);
        gift = new Gift(event, "Mixer", new BigDecimal("10000.00"));
        when(actors.requireAuthenticatedUserId()).thenReturn(owner);
        when(events.findById(event.getId())).thenReturn(Optional.of(event));
        when(gifts.findById(gift.getId())).thenReturn(Optional.of(gift));
        when(gifts.saveAndFlush(any(Gift.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test void newGiftStartsPrivate() { assertEquals(GiftVisibility.DRAFT, gift.getVisibility()); }

    @Test void ownerCanPublishAndAudit() {
        var result = service.setPublication(event.getId(), gift.getId(), GiftVisibility.PUBLIC);
        assertEquals(GiftVisibility.PUBLIC, result.visibility());
        verify(gifts).saveAndFlush(gift);
        verify(audit).recordChange(eq(owner), any(), eq("GIFT"), eq(gift.getId()), eq(AuditAction.UPDATE), anyMap(), anyMap(), isNull());
    }

    @Test void ownerCanUnpublish() {
        service.setPublication(event.getId(), gift.getId(), GiftVisibility.PUBLIC);
        service.setPublication(event.getId(), gift.getId(), GiftVisibility.DRAFT);
        assertEquals(GiftVisibility.DRAFT, gift.getVisibility());
    }

    @Test void anotherHostCannotPublish() {
        when(actors.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        var error = assertThrows(ResponseStatusException.class,
                () -> service.setPublication(event.getId(), gift.getId(), GiftVisibility.PUBLIC));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(gifts, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void anotherEventsGiftCannotBePublished() {
        Event other = new Event("Other", OccasionType.BIRTHDAY);
        other.assignOwner(owner);
        Gift otherGift = new Gift(other, "Camera", new BigDecimal("5000"));
        when(gifts.findById(otherGift.getId())).thenReturn(Optional.of(otherGift));
        var error = assertThrows(ResponseStatusException.class,
                () -> service.setPublication(event.getId(), otherGift.getId(), GiftVisibility.PUBLIC));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(audit);
    }

    @Test void acceptsHttpsProductLink() {
        var result = service.setProductLink(event.getId(), gift.getId(), "https://www.example.com/product/123");
        assertEquals("https://www.example.com/product/123", result.productUrl());
        verify(audit).recordChange(eq(owner), any(), eq("GIFT"), any(), eq(AuditAction.UPDATE), anyMap(), anyMap(), isNull());
    }

    @Test void rejectsUnsafeProductLinksWithoutWrites() {
        for (String url : new String[] {"http://example.com/item", "https://localhost/item",
                "https://127.0.0.1/item", "https://192.168.1.1/item",
                "https://user:password@example.com/item", "file:///etc/passwd"}) {
            var error = assertThrows(ResponseStatusException.class,
                    () -> service.setProductLink(event.getId(), gift.getId(), url), url);
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode(), url);
        }
        verify(gifts, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void clearsProductLink() {
        service.setProductLink(event.getId(), gift.getId(), "https://example.com/item");
        var result = service.setProductLink(event.getId(), gift.getId(), "");
        assertNull(result.productUrl());
    }
}
