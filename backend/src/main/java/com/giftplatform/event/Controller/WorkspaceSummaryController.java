package com.giftplatform.event.Controller;

import com.giftplatform.admin.*;
import com.giftplatform.security.ActorContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
public class WorkspaceSummaryController {
 private final ActorContext actor;private final JdbcTemplate db;private final GrowthSettingsService growth;private final EffectiveModulePermissionService permissions;
 public WorkspaceSummaryController(ActorContext actor,JdbcTemplate db,GrowthSettingsService growth,EffectiveModulePermissionService permissions){this.actor=actor;this.db=db;this.growth=growth;this.permissions=permissions;}
 @GetMapping("/api/v1/events/workspace-summary")
 public ResponseEntity<List<Map<String,Object>>> summary(){
  UUID who=actor.requireAuthenticatedUserId();if(!growth.enabled("SMART_DASHBOARD"))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Dashboard insights disabled");
  boolean rsvp=permissions.enabled(who,"RSVP");
  var rows=db.queryForList("""
   SELECT e.id,e.title,e.status,e.event_date,
    (SELECT count(*) FROM gifts g WHERE g.event_id=e.id AND g.status='ACTIVE') AS gift_count,
    CASE WHEN ? THEN (SELECT count(*) FROM event_rsvps r WHERE r.event_id=e.id) ELSE NULL END AS responses,
    CASE WHEN ? THEN (SELECT COALESCE(sum(r.party_size),0) FROM event_rsvps r WHERE r.event_id=e.id AND r.attendance='YES') ELSE NULL END AS attending
   FROM events e WHERE e.owner_id=? AND e.status IN ('DRAFT','PUBLISHED','COMPLETED')
   ORDER BY CASE WHEN e.status='PUBLISHED' AND (e.event_date>=CURRENT_DATE OR e.event_date IS NULL) THEN 0 WHEN e.status='DRAFT' THEN 1 ELSE 2 END,e.event_date ASC NULLS LAST,e.id
   LIMIT 6
   """,rsvp,rsvp,who);
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(rows);
 }
}
