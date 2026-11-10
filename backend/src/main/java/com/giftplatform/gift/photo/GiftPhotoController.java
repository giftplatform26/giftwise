package com.giftplatform.gift.photo;

import java.util.UUID;
import java.io.IOException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GiftPhotoController {
    private final GiftPhotoService photos;
    public GiftPhotoController(GiftPhotoService photos){this.photos=photos;}
    @PostMapping(path="/api/v1/events/{eventId}/gifts/photos",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public GiftPhotoService.Photo upload(@PathVariable UUID eventId,@RequestPart("photo") MultipartFile photo) throws IOException {
        if(photo.isEmpty()||photo.getSize()>2_000_000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a JPEG or PNG photo under 2 MB");
        return photos.upload(eventId,photo.getBytes());
    }
    @GetMapping("/api/v1/events/{eventId}/gifts/photos/{photoId}")
    public ResponseEntity<byte[]> owned(@PathVariable UUID eventId,@PathVariable UUID photoId){return image(photos.owned(eventId,photoId));}
    @GetMapping("/api/public/events/{token}/gifts/{giftId}/photo")
    public ResponseEntity<byte[]> published(@PathVariable UUID token,@PathVariable UUID giftId,@RequestParam(defaultValue="false") boolean thumbnail){return image(photos.publicPhoto(token,giftId,thumbnail));}
    private ResponseEntity<byte[]> image(byte[] bytes){return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").body(bytes);}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> error(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),e.getReason()==null?"Photo unavailable":e.getReason()));}
}
