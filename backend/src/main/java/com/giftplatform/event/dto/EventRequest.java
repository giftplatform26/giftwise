package com.giftplatform.event.dto;

import com.giftplatform.event.entity.OccasionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EventRequest(
    @NotBlank @Size(max=200) String title,
    @Size(max=10000) String description,
    @NotNull OccasionType occasionType,
    LocalDate eventDate,
    @Size(max=200) String addressLine1,
    @Size(max=120) String city,
    @Size(max=120) String state,
    @Size(max=20) String postalCode,
    @Size(max=80) String country,
    @Size(max=150) String contactName,
    @Size(max=30) String contactPhone,
    @Size(max=254) String contactEmail,
    @NotBlank @Size(max=200) String hostName,
    Boolean deliverySameAsEvent,
    @jakarta.validation.Valid com.giftplatform.event.entity.DeliveryAddress deliveryAddress
) {
    public EventRequest(String title,String description,OccasionType occasionType,LocalDate eventDate,String addressLine1,String city,String state,String postalCode,String country,String contactName,String contactPhone,String contactEmail,String hostName){
        this(title,description,occasionType,eventDate,addressLine1,city,state,postalCode,country,contactName,contactPhone,contactEmail,hostName,true,null);
    }
    public EventRequest(String title,String description,OccasionType occasionType,LocalDate eventDate){
        this(title,description,occasionType,eventDate,null,null,null,null,null,null,null,null,"Event Host");
    }
    public EventRequest(String title,String description,OccasionType occasionType,LocalDate eventDate,String addressLine1,String city,String state,String postalCode,String country,String contactName,String contactPhone,String contactEmail){
      this(title,description,occasionType,eventDate,addressLine1,city,state,postalCode,country,contactName,contactPhone,contactEmail,contactName==null||contactName.isBlank()?"Event Host":contactName);
    }
}
