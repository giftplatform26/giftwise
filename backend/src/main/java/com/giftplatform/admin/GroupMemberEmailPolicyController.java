package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController
public class GroupMemberEmailPolicyController {
 private final JdbcTemplate db; private final ActorContext actor;
 public GroupMemberEmailPolicyController(JdbcTemplate db,ActorContext actor){this.db=db;this.actor=actor;}
 public record Input(boolean enabled){}
 private void admin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 private void host(UUID id){if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM users WHERE id=? AND role='CUSTOMER'",Integer.class,id)))throw new ResponseStatusException(HttpStatus.NOT_FOUND);}
 private Map<String,Object> policy(UUID id){return db.queryForMap("SELECT COALESCE(p.admin_enabled,FALSE) AS admin_enabled,COALESCE(p.host_enabled,FALSE) AS host_enabled,COALESCE(p.admin_enabled OR p.host_enabled,FALSE) AS email_verification_required FROM users u LEFT JOIN group_member_email_policies p ON p.host_id=u.id WHERE u.id=?",id);}
 @GetMapping("/api/admin/hosts/{hostId}/group-member-email-policy") public Map<String,Object> read(@PathVariable UUID hostId){admin();host(hostId);return policy(hostId);}
 @PutMapping("/api/admin/hosts/{hostId}/group-member-email-policy") @Transactional
 public Map<String,Object> update(@PathVariable UUID hostId,@RequestBody Input input){admin();host(hostId);if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);db.update("INSERT INTO group_member_email_policies(host_id,admin_enabled) VALUES(?,?) ON CONFLICT(host_id) DO UPDATE SET admin_enabled=EXCLUDED.admin_enabled",hostId,input.enabled());db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id) VALUES(?,?,?,?)",UUID.randomUUID(),actor.requireAuthenticatedUserId(),"GROUP_MEMBER_EMAIL_POLICY_CHANGED",hostId);return policy(hostId);}
 @GetMapping("/api/host/group-member-email-policy") public Map<String,Object> mine(){UUID id=actor.requireAuthenticatedUserId();host(id);return policy(id);}
 @PutMapping("/api/host/group-member-email-policy") @Transactional
 public Map<String,Object> updateMine(@RequestBody Input input){UUID id=actor.requireAuthenticatedUserId();host(id);if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);db.update("INSERT INTO group_member_email_policies(host_id,host_enabled) VALUES(?,?) ON CONFLICT(host_id) DO UPDATE SET host_enabled=EXCLUDED.host_enabled",id,input.enabled());return policy(id);}
}
