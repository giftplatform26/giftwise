package com.giftplatform.notification;

import com.giftplatform.security.ActorContext;
import java.util.*;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/host/notifications")
public class HostInboxController {
  private final JdbcTemplate jdbc;
  private final ActorContext actors;
  public HostInboxController(JdbcTemplate jdbc, ActorContext actors) {
    this.jdbc=jdbc; this.actors=actors;
  }
  public record Item(UUID id,UUID eventId,String eventTitle,String subject,String message,
                     String evidence,String sourceKind,String createdAt,boolean read) {}
  public record Inbox(long unread,List<Item> entries) {}
  @GetMapping
  @Transactional(readOnly=true)
  public ResponseEntity<Inbox> list(@RequestParam(defaultValue="30") int limit,
                                     @RequestParam(defaultValue="0") int offset) {
    if(limit<1||limit>100||offset<0||offset>100000)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid pagination");
    UUID actor=actors.requireAuthenticatedUserId();
    Long count=jdbc.queryForObject("""
      SELECT COUNT(*) FROM notification_outbox n JOIN events e ON e.id=n.event_id
      WHERE e.owner_id=? AND n.recipient_kind='HOST' AND n.channel='IN_APP' AND n.read_at IS NULL
      """,Long.class,actor);
    List<Item> items=jdbc.query("""
      SELECT n.id,n.event_id,e.title,n.subject,n.message,n.payment_evidence,n.source_kind,
             n.created_at,n.read_at
      FROM notification_outbox n JOIN events e ON e.id=n.event_id
      WHERE e.owner_id=? AND n.recipient_kind='HOST' AND n.channel='IN_APP'
      ORDER BY n.created_at DESC,n.id DESC LIMIT ? OFFSET ?
      """,(rs,i)->new Item(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),
      rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),
      rs.getObject(8).toString(),rs.getObject(9)!=null),actor,limit,offset);
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
      .body(new Inbox(count==null?0:count,items));
  }
  @PostMapping("/{notificationId}/read")
  @Transactional
  public ResponseEntity<Void> read(@PathVariable UUID notificationId) {
    UUID actor=actors.requireAuthenticatedUserId();
    int updated=jdbc.update("""
      UPDATE notification_outbox n SET read_at=COALESCE(read_at,CURRENT_TIMESTAMP)
      FROM events e WHERE e.id=n.event_id AND e.owner_id=? AND n.id=?
      AND n.recipient_kind='HOST' AND n.channel='IN_APP'
      """,actor,notificationId);
    if(updated!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found");
    return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
  }
}
