package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Administrator-owned policy overrides. No policy change grants account access. */
@RestController
@RequestMapping("/api/admin/account-policies")
public class AccountPolicyController {
 private static final UUID ROOT = new UUID(0,0);
 private final JdbcTemplate db;
 private final ActorContext actor;
 public AccountPolicyController(JdbcTemplate db, ActorContext actor){this.db=db;this.actor=actor;}
 private UUID admin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);return actor.requireAuthenticatedUserId();}
 public record Policy(boolean requireEmail,boolean requireMobile,boolean verifyEmail,boolean verifyMobile,boolean allowGroupPin){}
 private String scope(String value){if(!List.of("PLATFORM","HOST","EVENT").contains(value))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid policy scope");return value;}
 private UUID target(String scope,UUID id){if("PLATFORM".equals(scope)){if(!ROOT.equals(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);return ROOT;}if(ROOT.equals(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);String table="HOST".equals(scope)?"users":"events";String extra="HOST".equals(scope)?" AND role='CUSTOMER'":"";Integer count=db.queryForObject("SELECT count(*) FROM "+table+" WHERE id=?"+extra,Integer.class,id);if(count==null||count!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Target not found");return id;}
 @GetMapping("/{scope}/{id}")
 public Map<String,Object> read(@PathVariable String scope,@PathVariable UUID id){admin();scope=scope(scope);target(scope,id);return Map.of("effective",effective(scope,id),"override",db.queryForList("SELECT require_email,require_mobile,verify_email,verify_mobile,allow_group_pin FROM account_registration_policies WHERE scope=? AND scope_id=?",scope,id));}
 private Policy row(String scope,UUID id){var list=db.query("SELECT require_email,require_mobile,verify_email,verify_mobile,allow_group_pin FROM account_registration_policies WHERE scope=? AND scope_id=?",(rs,n)->new Policy(rs.getBoolean(1),rs.getBoolean(2),rs.getBoolean(3),rs.getBoolean(4),rs.getBoolean(5)),scope,id);return list.isEmpty()?null:list.get(0);}
 private Policy merge(Policy base,Policy override){if(override==null)return base;return new Policy(base.requireEmail()||override.requireEmail(),base.requireMobile()||override.requireMobile(),base.verifyEmail()||override.verifyEmail(),base.verifyMobile()||override.verifyMobile(),base.allowGroupPin()&&override.allowGroupPin());}
 public Policy effective(String scope,UUID id){scope=scope(scope);Policy p=row("PLATFORM",ROOT);if(p==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Platform policy unavailable");if("PLATFORM".equals(scope))return p;if("HOST".equals(scope))return merge(p,row("HOST",id));UUID host="EVENT".equals(scope)?db.queryForObject("SELECT owner_id FROM events WHERE id=?",UUID.class,id):id;return merge(merge(p,row("HOST",host)),row("EVENT",id));}
 @PutMapping("/{scope}/{id}") @Transactional
 public Map<String,Object> update(@PathVariable String scope,@PathVariable UUID id,@RequestBody Policy input){UUID who=admin();scope=scope(scope);target(scope,id);if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);if(!input.requireEmail()||!input.requireMobile()||!input.verifyEmail()||!input.verifyMobile())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Verified email and mobile are mandatory security minimums");db.update("INSERT INTO account_registration_policies(scope,scope_id,require_email,require_mobile,verify_email,verify_mobile,allow_group_pin,changed_by) VALUES(?,?,?,?,?,?,?,?) ON CONFLICT(scope,scope_id) DO UPDATE SET require_email=EXCLUDED.require_email,require_mobile=EXCLUDED.require_mobile,verify_email=EXCLUDED.verify_email,verify_mobile=EXCLUDED.verify_mobile,allow_group_pin=EXCLUDED.allow_group_pin,changed_by=EXCLUDED.changed_by,changed_at=now()",scope,id,input.requireEmail(),input.requireMobile(),input.verifyEmail(),input.verifyMobile(),input.allowGroupPin(),who);db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)",UUID.randomUUID(),who,"ACCOUNT_POLICY_CHANGED",ROOT.equals(id)?null:id,null);return Map.of("effective",effective(scope,id));}
}
