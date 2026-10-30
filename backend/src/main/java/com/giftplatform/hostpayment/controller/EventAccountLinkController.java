package com.giftplatform.hostpayment.controller;

import com.giftplatform.hostpayment.service.EventAccountLinkService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/host/payment-profiles/event-links/{eventId}/{kind}/{id}")
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class EventAccountLinkController {
    private final EventAccountLinkService service;
    public EventAccountLinkController(EventAccountLinkService service){this.service=service;}
    @PostMapping("/link") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void link(@PathVariable UUID eventId,@PathVariable String kind,@PathVariable UUID id){service.link(eventId,kind.toUpperCase(Locale.ROOT),id);}
    @PostMapping("/{action:preferred|enable|disable|unlink}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID eventId,@PathVariable String kind,@PathVariable UUID id,@PathVariable String action){service.update(eventId,kind.toUpperCase(Locale.ROOT),id,action);}
}
