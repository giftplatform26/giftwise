package com.giftplatform.qr.design;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/events/{eventId}/qr/invitations")
public class InvitationDesignController {
 private final InvitationDesignService service;
 public InvitationDesignController(InvitationDesignService service){this.service=service;}
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<InvitationDesignService.DesignResponse> create(
  @PathVariable UUID eventId,@RequestParam String name,@RequestParam InvitationStyle style,
  @RequestParam(required=false) String greeting,@RequestPart(required=false) MultipartFile image) throws IOException {
  if(image!=null&&image.getSize()>InvitationImageProcessor.MAX_UPLOAD_BYTES)
   throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE,"Image too large");
  return ResponseEntity.status(201).body(service.create(eventId,name,style,greeting,image==null?null:image.getBytes()));
 }
 @GetMapping public List<InvitationDesignService.DesignResponse> list(@PathVariable UUID eventId){return service.list(eventId);}
 @GetMapping(value="/{designId}/png",produces=MediaType.IMAGE_PNG_VALUE)
 public ResponseEntity<byte[]> png(@PathVariable UUID eventId,@PathVariable UUID designId){
  return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(service.png(eventId,designId));
 }
}
