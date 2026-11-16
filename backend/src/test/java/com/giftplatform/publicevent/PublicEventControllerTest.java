package com.giftplatform.publicevent;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.entity.GiftMode;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.gift.repository.GiftRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicEventControllerTest {
    private final EventRepository events = mock(EventRepository.class);
    private final GiftRepository gifts = mock(GiftRepository.class);
    private final EffectiveModulePermissionService permissions = mock(EffectiveModulePermissionService.class);
    // The JdbcTemplate field stays null: touching it (the gift_groups query) would fail the test.
    private final PublicEventController controller = new PublicEventController(events, gifts, permissions);

    private Event event(boolean gifting, GiftMode mode) {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
        when(event.getOccasionType()).thenReturn(OccasionType.BIRTHDAY);
        when(event.getTitle()).thenReturn("Party");
        when(event.isGiftingEnabled()).thenReturn(gifting);
        when(event.getGiftMode()).thenReturn(mode);
        when(event.getOccasionDetails()).thenReturn(Map.of("venueName", "Hall", "brideName", "A"));
        return event;
    }

    @Test
    void contributionOnlyEventsSkipBothGiftQueries() {
        UUID token = UUID.randomUUID();
        Event event = event(true, GiftMode.CONTRIBUTION_ONLY);
        when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
        var body = controller.view(token).getBody();
        assertNotNull(body);
        assertTrue(body.gifts().isEmpty());
        verifyNoInteractions(gifts);
    }

    @Test
    void giftingDisabledEventsSkipBothGiftQueries() {
        UUID token = UUID.randomUUID();
        Event event = event(false, GiftMode.CONTRIBUTION_AND_GIFTS);
        when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
        var body = controller.view(token).getBody();
        assertNotNull(body);
        assertTrue(body.gifts().isEmpty());
        assertFalse(body.occasionDetails().containsKey("venueName"));
        verifyNoInteractions(gifts);
    }
}
