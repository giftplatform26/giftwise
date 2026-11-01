package com.giftplatform.event.dto;

import com.giftplatform.event.entity.*;
import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(UUID id, UUID ownerId, String title, String description,
                            OccasionType occasionType, LocalDate eventDate, EventStatus status,UUID publicToken, String hostName, boolean canPublish, java.util.List<String> publishDisabledReasons, DeliveryAddress eventAddress, DeliveryAddress deliveryAddress, boolean deliverySameAsEvent) {
    public EventResponse(UUID id, UUID ownerId, String title, String description, OccasionType occasionType, LocalDate eventDate, EventStatus status, UUID publicToken, String hostName, boolean canPublish, java.util.List<String> reasons){
        this(id,ownerId,title,description,occasionType,eventDate,status,publicToken,hostName,canPublish,reasons,null,null,true);
    }
    public EventResponse(UUID id, UUID ownerId, String title, String description, OccasionType occasionType,
                         LocalDate eventDate, EventStatus status, UUID publicToken, String hostName, boolean canPublish) {
        this(id,ownerId,title,description,occasionType,eventDate,status,publicToken,hostName,canPublish,java.util.List.of());
    }
    public static EventResponse from(Event e) { return from(e, false); }
    public static EventResponse from(Event e, boolean canPublish) { return from(e,canPublish,java.util.List.of()); }
    public static EventResponse from(Event e, boolean canPublish, java.util.List<String> reasons) {
        return new EventResponse(e.getId(), e.getOwnerId(), e.getTitle(), e.getDescription(),
                e.getOccasionType(), e.getEventDate(), e.getStatus(),e.getPublicToken(),e.getHostDisplayName(),canPublish,reasons,new DeliveryAddress(e.getAddressLine1(),e.getCity(),e.getState(),e.getPostalCode(),e.getCountry()),e.getDeliveryAddress(),e.isDeliverySameAsEvent());
    }
}
