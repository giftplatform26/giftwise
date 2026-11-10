package com.giftplatform.audit.api;
import com.giftplatform.security.ActorContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController
@RequestMapping("/api/admin/audit/activity")
public class BusinessAuditController {
 private final JdbcTemplate db;private final ActorContext actors;
 public BusinessAuditController(JdbcTemplate db,ActorContext actors){this.db=db;this.actors=actors;}
 @GetMapping public ResponseEntity<Map<String,Object>> list(@RequestParam(defaultValue="") String entityType,@RequestParam(defaultValue="0") int page){if(!actors.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);actors.requireAuthenticatedUserId();if(page<0||page>100000||(!entityType.isEmpty()&&!Set.of("USER","EVENT","GIFT","CONTRIBUTION","PAYMENT").contains(entityType)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose valid audit filters.");
  String where=entityType.isEmpty()?" WHERE entity_type IN ('USER','EVENT','GIFT','CONTRIBUTION','PAYMENT')":" WHERE entity_type=?";List<Object> args=new ArrayList<>();if(!entityType.isEmpty())args.add(entityType);Long count=db.queryForObject("SELECT count(*) FROM audit_logs"+where,Long.class,args.toArray());args.add(20);args.add(page*20);var rows=db.queryForList("SELECT id,actor_id,actor_type,entity_type,entity_id,action,occurred_at FROM audit_logs"+where+" ORDER BY occurred_at DESC,id DESC LIMIT ? OFFSET ?",args.toArray());return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("entries",rows,"total",count==null?0:count,"page",page,"pageSize",20));
 }
}
