package com.giftplatform.event.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Embeddable
public record DeliveryAddress(
    @NotBlank @Size(max=200) String addressLine1,
    @NotBlank @Size(max=120) String city,
    @NotBlank @Size(max=120) String state,
    @NotBlank @Size(max=20) String postalCode,
    @NotBlank @Size(max=80) String country
) {}
