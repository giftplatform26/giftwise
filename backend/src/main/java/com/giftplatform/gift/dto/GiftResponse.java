package com.giftplatform.gift.dto;

import com.giftplatform.gift.entity.*;
import java.math.BigDecimal;
import java.util.UUID;

public record GiftResponse(UUID id, UUID eventId, long version, String name, String description,
                           String imageKey, BigDecimal targetAmount, GiftStatus status, GiftVisibility visibility, String productUrl,
                           FulfillmentPreference fulfillmentOverride,
                           FulfillmentPreference effectiveFulfillmentPreference, GiftPriority priority) {
    public static GiftResponse from(Gift gift) {
        return new GiftResponse(gift.getId(), gift.getEvent().getId(), gift.getVersion(),
                gift.getName(), gift.getDescription(), gift.getImageKey(), gift.getTargetAmount(),
                gift.getStatus(), gift.getVisibility(), gift.getProductUrl(), gift.getFulfillmentOverride(), gift.effectiveFulfillmentPreference(), gift.getPriority());
    }
}
