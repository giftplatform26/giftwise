package com.giftplatform.asset;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
public class GalleryAssetController {
 private final GalleryAssetService service;
 public GalleryAssetController(GalleryAssetService service){this.service=service;}
 @GetMapping("/api/public/invitation-assets")
 public List<GalleryAssetService.AssetInfo> published(){return service.published();}
 @GetMapping("/api/public/invitation-assets/{id}/image")
 public ResponseEntity<byte[]> publishedImage(@PathVariable UUID id){return image(service.publishedImage(id));}
 @GetMapping("/api/v1/commerce/templates/{id}/image")
 public ResponseEntity<byte[]> purchasedImage(@PathVariable UUID id){return image(service.purchasedImage(id));}
 public record Tier(String tier){}
 @PutMapping("/api/admin/invitation-assets/{id}/tier")
 public GalleryAssetService.AssetInfo tier(@PathVariable UUID id,@RequestBody Tier input){return service.tier(id,input==null?null:input.tier());}
 @GetMapping("/api/admin/invitation-assets")
 public List<GalleryAssetService.AssetInfo> all(){return service.all();}
 @GetMapping("/api/admin/invitation-assets/{id}/image")
 public ResponseEntity<byte[]> adminImage(@PathVariable UUID id){return image(service.adminImage(id));}
 @PostMapping(value="/api/admin/invitation-assets",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public GalleryAssetService.AssetInfo upload(@RequestParam String category,@RequestParam String occasion,@RequestParam String title,
   @RequestPart("file") MultipartFile file) throws IOException {
  return service.upload(category,occasion,title,file.getBytes());
 }
 @PostMapping("/api/admin/invitation-assets/{id}/publish")
 public GalleryAssetService.AssetInfo publish(@PathVariable UUID id){return service.publish(id,true);}
 @PostMapping("/api/admin/invitation-assets/{id}/unpublish")
 public GalleryAssetService.AssetInfo unpublish(@PathVariable UUID id){return service.publish(id,false);}
 private ResponseEntity<byte[]> image(GalleryAsset a){return ResponseEntity.ok()
  .contentType(MediaType.IMAGE_PNG).cacheControl(CacheControl.noStore()).body(a.getImagePng());}
}
