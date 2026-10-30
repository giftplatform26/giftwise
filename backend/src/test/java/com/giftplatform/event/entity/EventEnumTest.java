package com.giftplatform.event.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventEnumTest {

    @Test
    void shouldSupportDraftStatus() {
        assertEquals(
                EventStatus.DRAFT,
                EventStatus.valueOf("DRAFT")
        );
    }

    @Test
    void shouldSupportPublishedStatus() {
        assertEquals(
                EventStatus.PUBLISHED,
                EventStatus.valueOf("PUBLISHED")
        );
    }

    @Test
    void shouldRejectInvalidEventStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EventStatus.valueOf("INVALID")
        );
    }

    @Test
    void shouldSupportWeddingOccasion() {
        assertEquals(
                OccasionType.WEDDING,
                OccasionType.valueOf("WEDDING")
        );
    }

    @Test
    void shouldRejectInvalidOccasionType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> OccasionType.valueOf("UNKNOWN")
        );
    }
}