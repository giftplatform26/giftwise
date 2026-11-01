package com.giftplatform.celebration;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MemoryAlbumController {
 private final JdbcTemplate db;
 private final EventRepository events;
 private final ActorContext actors;
 private final EffectiveModulePermissionService permissions;
 public MemoryAlbumController(JdbcTemplate db,EventRepository events,ActorContext actors,EffectiveModulePermissionService permissions){this.db=db;this.events=events;this.actors=actors;this.permissions=permissions;}
 public record Memory(UUID id,String guestName,String message,boolean photoAvailable,Instant createdAt,String status) {}
 public record Album(String title,LocalDate eventDate,List<Memory> memories,long totalCount,long photoCount,boolean hasMore,int nextOffset) {}
 @GetMapping("/api/v1/events/{id}/album")
 @Transactional(readOnly=true)
 public ResponseEntity<Album> album(@PathVariable UUID id,@RequestParam(defaultValue="60") int limit,
     @RequestParam(defaultValue="0") int offset,@RequestParam(defaultValue="false") boolean includeHidden){
  UUID host=actors.requireAuthenticatedUserId();
  var event=events.findById(id).filter(e->host.equals(e.getOwnerId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  permissions.requireAlbum(host,id);
  if(limit<1||limit>200||offset<0||offset>1000000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid album page");
  var counts=db.queryForMap("SELECT count(*) AS total,count(*) FILTER(WHERE photo_path IS NOT NULL) AS photos FROM event_guestbook WHERE event_id=? AND (? OR status<>'HIDDEN')",id,includeHidden);
  long total=((Number)counts.get("total")).longValue(),photos=((Number)counts.get("photos")).longValue();
  var memories=db.query("SELECT id,guest_name,message,photo_path IS NOT NULL AS photo_available,created_at,status FROM event_guestbook WHERE event_id=? AND (? OR status<>'HIDDEN') ORDER BY created_at,id LIMIT ? OFFSET ?",
   (rs,n)->new Memory(rs.getObject("id",UUID.class),rs.getString("guest_name"),rs.getString("message"),rs.getBoolean("photo_available"),rs.getTimestamp("created_at").toInstant(),rs.getString("status")),id,includeHidden,limit,offset);
  int next=offset+memories.size();
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Album(event.getTitle(),event.getEventDate(),memories,total,photos,next<total,next));
 }
}
