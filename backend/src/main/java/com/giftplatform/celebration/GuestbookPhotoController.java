package com.giftplatform.celebration;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class GuestbookPhotoController {
 private final JdbcTemplate db;
 private final EventRepository events;
 private final ActorContext actor;
 private final EffectiveModulePermissionService permissions;
 private final Path storage;
 public GuestbookPhotoController(JdbcTemplate db,EventRepository events,ActorContext actor,EffectiveModulePermissionService permissions,
     @Value("${gift.guestbook.photos.dir:${user.home}/.gift-platform/guestbook-photos}") String storageDir) {
  this.db=db;this.events=events;this.actor=actor;this.permissions=permissions;this.storage=Path.of(storageDir).toAbsolutePath().normalize();
 }
 private Event published(UUID token){return events.findByPublicToken(token)
   .filter(e->e.getStatus()==EventStatus.PUBLISHED)
   .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.cohost.EventAccess access;
 private Event guests(UUID id){return access.require(id,com.giftplatform.cohost.EventAccess.Capability.GUESTS,null).event();}
 private Event owned(UUID id){UUID user=actor.requireAuthenticatedUserId();return events.findById(id)
   .filter(e->user.equals(e.getOwnerId()))
   .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private static String text(String s,int max){if(s==null||s.isBlank()||s.strip().length()>max||
   s.chars().anyMatch(c->Character.isISOControl(c)&&c!='\n'))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid message details");return s.strip();}
 private static String signature(byte[] b){
  if(b.length>=3&&(b[0]&255)==255&&(b[1]&255)==216&&(b[2]&255)==255)return "JPEG";
  if(b.length>=8&&(b[0]&255)==137&&b[1]==80&&b[2]==78&&b[3]==71)return "PNG";
  if(b.length>=12&&new String(b,0,4,StandardCharsets.US_ASCII).equals("RIFF")&&new String(b,8,4,StandardCharsets.US_ASCII).equals("WEBP"))return "WEBP";
  if(b.length>=12&&new String(b,4,4,StandardCharsets.US_ASCII).equals("ftyp")){
   String brand=new String(b,8,4,StandardCharsets.US_ASCII);
   if(Set.of("heic","heix","hevc","hevx","heim","heis","mif1","msf1").contains(brand))return "HEIF";
  }
  return "UNKNOWN";
 }
 private static void runMagick(List<String> command) throws Exception {
  ProcessBuilder pb=new ProcessBuilder(command);pb.redirectErrorStream(true);
  pb.environment().put("MAGICK_TEMPORARY_PATH",System.getProperty("java.io.tmpdir"));
  Process proc=pb.start();boolean finished=proc.waitFor(20,TimeUnit.SECONDS);
  if(!finished){proc.destroyForcibly();throw new IllegalStateException("Image processing timed out");}
  if(proc.exitValue()!=0)throw new IllegalStateException("Image conversion unavailable or unsupported");
 }
 private byte[] normalize(MultipartFile image) {
  if(image==null||image.isEmpty()||image.getSize()>5L*1024*1024)
   throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Photo must be 5 MB or less");
  Path input=null,output=null;
  try{
   byte[] bytes=image.getBytes();String type=signature(bytes);
   if(type.equals("UNKNOWN"))throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Use JPG, PNG, WebP, HEIC or HEIF");
   input=Files.createTempFile("giftbook-in-",".image");output=Files.createTempFile("giftbook-out-",".jpg");
   Files.write(input,bytes);
   runMagick(List.of("magick","-limit","memory","128MiB","-limit","map","256MiB","-limit","disk","512MiB",
    input.toString()+"[0]","-auto-orient","-strip","-resize","1600x1600>","-quality","78","jpeg:"+output));
   byte[] result=Files.readAllBytes(output);
   if(result.length==0||result.length>3L*1024*1024||!signature(result).equals("JPEG"))
    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Optimized photo exceeds 3 MB");
   return result;
  }catch(ResponseStatusException e){throw e;}
   catch(Exception e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Photo conversion unavailable; try a smaller JPEG or PNG",e);}
   finally{try{if(input!=null)Files.deleteIfExists(input);}catch(Exception ignored){}try{if(output!=null)Files.deleteIfExists(output);}catch(Exception ignored){}}
 }
 private void limit(UUID eventId,HttpServletRequest request){
  try{
   String fp=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(request.getRemoteAddr().getBytes(StandardCharsets.UTF_8)));
   int n=db.update("INSERT INTO event_guest_submission_limits(event_id,fingerprint,submission_date,submission_type,count) VALUES (?,?,?,?,1) "+
    "ON CONFLICT(event_id,fingerprint,submission_date,submission_type) DO UPDATE SET count=event_guest_submission_limits.count+1 "+
    "WHERE event_guest_submission_limits.count < ?",eventId,fp,LocalDate.now(java.time.ZoneOffset.UTC),"WISH",5);
   if(n==0)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Please try again later");
  }catch(ResponseStatusException e){throw e;}catch(Exception e){throw new IllegalStateException(e);}
 }
 @PostMapping(path="/api/public/celebrations/{token}/wishes/photo",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 @ResponseStatus(HttpStatus.CREATED)
 @Transactional
 public Map<String,String> submit(@PathVariable UUID token,@RequestParam String guestName,@RequestParam String message,
   @RequestPart("photo") MultipartFile photo,HttpServletRequest request){
  Event event=published(token);permissions.require(event.getOwnerId(),"GUESTBOOK");String name=text(guestName,120),wish=text(message,1000);
  byte[] jpg=normalize(photo);limit(event.getId(),request);
  UUID id=UUID.randomUUID();String file=UUID.randomUUID()+".jpg";Path path=storage.resolve(file).normalize();
  try{
   Files.createDirectories(storage);
   if(!path.getParent().equals(storage))throw new IllegalStateException("Invalid photo path");
   Files.write(path,jpg,StandardOpenOption.CREATE_NEW);
   boolean auto="AUTO".equals(permissions.guestbookMode(event.getId()));
   db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,photo_path,status) VALUES (?,?,?,?,?,?)",id,event.getId(),name,wish,file,auto?"APPROVED":"PENDING");
   org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
    new org.springframework.transaction.support.TransactionSynchronization(){
     @Override public void afterCompletion(int status){if(status!=org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED)
       try{Files.deleteIfExists(path);}catch(Exception ignored){}}
    });
   return Map.of("message",auto?"Thank you for sharing your wishes and photo!":"Your wishes and photo have been sent for host approval.");
  }catch(Exception e){try{Files.deleteIfExists(path);}catch(Exception ignored){}throw e instanceof RuntimeException re?re:new IllegalStateException(e);}
 }
 private ResponseEntity<byte[]> photo(UUID wishId,UUID eventId,boolean requireApproved){
  List<String> files=db.query("SELECT photo_path FROM event_guestbook WHERE id=? AND event_id=? AND photo_path IS NOT NULL"+
   (requireApproved?" AND status='APPROVED'":""),(rs,i)->rs.getString(1),wishId,eventId);
  if(files.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  String name=files.get(0);
  if(!name.matches("[0-9a-fA-F-]{36}\\.jpg"))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  Path path=storage.resolve(name).normalize();if(!path.getParent().equals(storage))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  try{return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
    .cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").body(Files.readAllBytes(path));}
  catch(Exception e){throw new ResponseStatusException(HttpStatus.NOT_FOUND);}
 }
 @GetMapping("/api/public/celebrations/{token}/wishes/{wishId}/photo")
 public ResponseEntity<byte[]> publicPhoto(@PathVariable UUID token,@PathVariable UUID wishId){Event e=published(token);permissions.require(e.getOwnerId(),"GUESTBOOK");return photo(wishId,e.getId(),true);}
 @GetMapping("/api/v1/events/{id}/wishes/{wishId}/photo")
 public ResponseEntity<byte[]> hostPhoto(@PathVariable UUID id,@PathVariable UUID wishId){Event e=guests(id);permissions.require(e.getOwnerId(),"GUESTBOOK");return photo(wishId,id,false);}
 @GetMapping("/api/v1/events/{id}/album/{wishId}/photo")
 public ResponseEntity<byte[]> albumPhoto(@PathVariable UUID id,@PathVariable UUID wishId){
  Event e=owned(id);permissions.requireAlbum(e.getOwnerId(),id);return photo(wishId,id,false);
 }
}
