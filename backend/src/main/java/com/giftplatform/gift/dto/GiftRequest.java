package com.giftplatform.gift.dto;

import com.giftplatform.gift.entity.FulfillmentPreference;
import com.giftplatform.gift.entity.GiftPriority;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record GiftRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 10000) String description,
        @Size(max = 512) String imageKey,
        @NotNull @DecimalMin("0.01") @Digits(integer = 13, fraction = 2) BigDecimal targetAmount,
        FulfillmentPreference fulfillmentOverride,
        GiftPriority priority,
        @Size(max = 2048) String productUrl,
        java.util.UUID retailerLookupId
) {
    public GiftRequest(String name, String description, String imageKey, BigDecimal targetAmount,
                       FulfillmentPreference fulfillmentOverride, GiftPriority priority) {
        this(name, description, imageKey, targetAmount, fulfillmentOverride, priority, null, null);
    }
    public GiftRequest(String name, String description, String imageKey, BigDecimal targetAmount,
                       FulfillmentPreference fulfillmentOverride) {
        this(name, description, imageKey, targetAmount, fulfillmentOverride, null);
    }
}
