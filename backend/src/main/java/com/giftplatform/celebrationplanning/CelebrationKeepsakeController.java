package com.giftplatform.celebrationplanning;
import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
@RestController
public class CelebrationKeepsakeController {
 private final EventRepository events;private final ActorContext actor;private final JdbcTemplate db;private final EffectiveModulePermissionService permissions;
 public CelebrationKeepsakeController(EventRepository events,ActorContext actor,JdbcTemplate db,EffectiveModulePermissionService permissions){this.events=events;this.actor=actor;this.db=db;this.permissions=permissions;}
 @GetMapping(value="/api/v1/events/{id}/album/keepsake",produces="text/html;charset=UTF-8") @Transactional(readOnly=true)
 public ResponseEntity<String> download(@PathVariable UUID id){UUID host=actor.requireAuthenticatedUserId();var e=events.findById(id).filter(value->host.equals(value.getOwnerId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));permissions.requireAlbum(host,id);var rows=db.query("SELECT guest_name,message FROM event_guestbook WHERE event_id=? AND status='APPROVED' ORDER BY created_at,id LIMIT 501",(rs,i)->new CelebrationKeepsake.Message(rs.getString(1),rs.getString(2)),id);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=celebration-keepsake.html").header("Content-Security-Policy","default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; frame-ancestors 'none'; sandbox").body(CelebrationKeepsake.render(e.getTitle(),e.getEventDate(),rows.stream().limit(500).toList(),rows.size()>500));}
}
