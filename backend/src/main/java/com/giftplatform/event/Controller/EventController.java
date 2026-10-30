package com.giftplatform.event.Controller;

import com.giftplatform.event.dto.*;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.service.EventManagementService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventManagementService service;
    public EventController(EventManagementService service){this.service=service;}
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping public List<EventResponse> list(){return service.list();}
    @GetMapping("/{id}") public EventResponse get(@PathVariable UUID id){return service.get(id);}
    @PutMapping("/{id}") public EventResponse update(@PathVariable UUID id,@Valid @RequestBody EventRequest request){return service.update(id,request);}
    @PostMapping("/{id}/publish") public EventResponse publish(@PathVariable UUID id){return service.changeStatus(id,EventStatus.PUBLISHED);}
    @PostMapping("/{id}/complete") public EventResponse complete(@PathVariable UUID id){return service.changeStatus(id,EventStatus.COMPLETED);}
    @PostMapping("/{id}/cancel") public EventResponse cancel(@PathVariable UUID id){return service.changeStatus(id,EventStatus.CANCELLED);}
    @PostMapping("/{id}/unpublish")
    public EventResponse unpublish(@PathVariable UUID id) {
        return service.changeStatus(id, EventStatus.DRAFT);
    }

}
