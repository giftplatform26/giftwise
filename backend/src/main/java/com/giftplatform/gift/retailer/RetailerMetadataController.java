package com.giftplatform.gift.retailer;

import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/gifts/retailer-metadata")
public class RetailerMetadataController {
    private final RetailerMetadataService service;
    public RetailerMetadataController(RetailerMetadataService service){this.service=service;}
    public record Lookup(@NotBlank @Size(max=2048) String productUrl){}
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> failure(org.springframework.web.server.ResponseStatusException error){
        var problem=org.springframework.http.ProblemDetail.forStatusAndDetail(error.getStatusCode(),
            error.getReason()==null?"Retailer lookup failed":error.getReason());
        return ResponseEntity.status(error.getStatusCode()).cacheControl(CacheControl.noStore()).body(problem);
    }
    @PostMapping
    public ResponseEntity<RetailerMetadataService.Preview> lookup(@PathVariable UUID eventId,@Valid @RequestBody Lookup request){
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.lookup(eventId,request.productUrl()));
    }
}
