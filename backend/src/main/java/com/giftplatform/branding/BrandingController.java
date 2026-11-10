package com.giftplatform.branding;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class BrandingController {
 private final BrandingService service;
 public BrandingController(BrandingService service){this.service=service;}
 @GetMapping("/api/public/branding") public ResponseEntity<BrandingService.Settings> get(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.get());}
 @GetMapping("/api/admin/branding") public BrandingService.Settings admin(){service.admin();return service.get();}
 @PostMapping("/api/admin/branding") public BrandingService.Settings save(@RequestBody BrandingService.Update body){return service.save(body);}
 @PostMapping(value="/api/admin/branding/{kind}",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public BrandingService.Settings upload(@PathVariable String kind,@RequestParam long revision,@RequestPart("file") MultipartFile file)throws IOException{service.admin();if(file.getSize()>2*1024*1024)throw new IllegalArgumentException("Choose an image up to 2 MB.");return service.upload(kind,file.getBytes(),revision);}
 @DeleteMapping("/api/admin/branding/{kind}") public BrandingService.Settings remove(@PathVariable String kind,@RequestParam long revision){return service.remove(kind,revision);}
 @GetMapping("/api/public/branding/assets/{id}") public ResponseEntity<byte[]> image(@PathVariable UUID id){return ResponseEntity.ok().cacheControl(CacheControl.maxAge(365,TimeUnit.DAYS).cachePublic().immutable()).contentType(MediaType.IMAGE_PNG).header("X-Content-Type-Options","nosniff").body(service.image(id));}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,String> invalid(IllegalArgumentException ex){return Map.of("message",ex.getMessage());}
}
