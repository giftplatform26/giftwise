package com.giftplatform.asset;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class HostInvitationUploadController {
 private final HostInvitationUploadService service;
 public HostInvitationUploadController(HostInvitationUploadService service){this.service=service;}
 @GetMapping("/api/v1/invitation-uploads/access") public ResponseEntity<HostInvitationUploadService.Access> access(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.mine());}
 @GetMapping("/api/v1/invitation-uploads") public ResponseEntity<List<HostInvitationUploadService.Upload>> list(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.list());}
 @PostMapping(value="/api/v1/invitation-uploads",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public HostInvitationUploadService.Upload upload(@RequestParam String category,@RequestParam String title,@RequestPart("file") MultipartFile file)throws IOException{return service.upload(category,title,file.getBytes());}
 @GetMapping("/api/v1/invitation-uploads/{id}/image") public ResponseEntity<byte[]> image(@PathVariable UUID id){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_PNG).body(service.image(id));}
 @GetMapping("/api/admin/console/hosts/{hostId}/invitation-uploads") public ResponseEntity<HostInvitationUploadService.Access> adminAccess(@PathVariable UUID hostId){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.adminAccess(hostId));}
 public record Controls(@NotNull Boolean enabled,@NotNull Boolean requiresPayment){}
 @PutMapping("/api/admin/console/hosts/{hostId}/invitation-uploads") public HostInvitationUploadService.Access configure(@PathVariable UUID hostId,@Valid @RequestBody Controls controls){return service.configure(hostId,controls.enabled(),controls.requiresPayment());}
}
