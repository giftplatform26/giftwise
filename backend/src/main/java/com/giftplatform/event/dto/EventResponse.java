package com.giftplatform.event.dto;

import com.giftplatform.event.entity.*;
import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(UUID id, UUID ownerId, String title, String description,
                            OccasionType occasionType, LocalDate eventDate, EventStatus status,UUID publicToken, String hostName, boolean canPublish) {
    public static EventResponse from(Event e) { return from(e, false); }
    public static EventResponse from(Event e, boolean canPublish) {
        return new EventResponse(e.getId(), e.getOwnerId(), e.getTitle(), e.getDescription(),
                e.getOccasionType(), e.getEventDate(), e.getStatus(),e.getPublicToken(),e.getHostDisplayName(),canPublish);
    }
}
