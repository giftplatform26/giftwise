package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/**
 * Read-only oversight for platform administrators: the user directory, a verification inbox that
 * shows everything waiting for someone to verify it, and the list of group gifts with their
 * account-switching permission (changed through GroupCreatorPaymentController#adminSwitch).
 *
 * Nothing here verifies money or accounts. Payment receipt is decided by the host or group creator,
 * and account verification needs provider evidence, so the inbox only shows who acts and where.
 * Responses never include guest names, contacts, transfer references, phone numbers or account numbers.
 */
@RestController
public class AdminOversightController {
 static final int PAGE_SIZE=25;
 static final int INBOX_ITEMS=20;
 private static final Set<String> ROLES=Set.of("ALL","CUSTOMER","PLATFORM_ADMIN");
 private static final Set<String> STATUSES=Set.of("ALL","ACTIVE","DISABLED","DELETED");
 private final JdbcTemplate db;
 private final ActorContext actor;
 public AdminOversightController(JdbcTemplate db,ActorContext actor){this.db=db;this.actor=actor;}
 private void admin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform admin only");actor.requireAuthenticatedUserId();}
 private static String search(String value){String q=value==null?"":value.strip();if(q.length()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Search too long");return "%"+q.toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";}
 private static String choice(String value,Set<String> allowed){String v=value==null?"ALL":value.toUpperCase(Locale.ROOT);if(!allowed.contains(v))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown filter");return v;}

 @GetMapping("/api/admin/console/users")
 public Map<String,Object> users(@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="ALL") String role,@RequestParam(defaultValue="ALL") String status,@RequestParam(defaultValue="0") int page){
  admin();String q=search(search);String r=choice(role,ROLES),s=choice(status,STATUSES);
  StringBuilder where=new StringBuilder(" WHERE (LOWER(COALESCE(u.username,'')) LIKE ? ESCAPE '\\' OR LOWER(u.email) LIKE ? ESCAPE '\\' OR LOWER(COALESCE(u.full_name,'')) LIKE ? ESCAPE '\\')");
  List<Object> args=new ArrayList<>(List.of(q,q,q));
  if(!"ALL".equals(r)){where.append(" AND u.role=?");args.add(r);}
  switch(s){case "ACTIVE"->where.append(" AND u.enabled AND u.deleted_at IS NULL");case "DISABLED"->where.append(" AND NOT u.enabled AND u.deleted_at IS NULL");case "DELETED"->where.append(" AND u.deleted_at IS NOT NULL");default->{}}
  long total=Optional.ofNullable(db.queryForObject("SELECT count(*) FROM users u"+where,Long.class,args.toArray())).orElse(0L);
  int pages=(int)Math.max(1,(total+PAGE_SIZE-1)/PAGE_SIZE);int p=Math.max(0,Math.min(page,pages-1));
  List<Object> pageArgs=new ArrayList<>(args);pageArgs.add(PAGE_SIZE);pageArgs.add(p*PAGE_SIZE);
  List<Map<String,Object>> rows=db.queryForList("SELECT u.id,u.username,u.full_name,u.email,u.role,u.enabled,(u.deleted_at IS NOT NULL) AS deleted,u.created_at,(u.email_verified_at IS NOT NULL) AS email_verified,(u.phone_verified_at IS NOT NULL) AS phone_verified,"+
   "(SELECT count(*) FROM events e WHERE e.owner_id=u.id) AS events,(SELECT count(*) FROM events e WHERE e.owner_id=u.id AND e.status='PUBLISHED') AS published_events FROM users u"+where+" ORDER BY u.created_at DESC,u.id LIMIT ? OFFSET ?",pageArgs.toArray());
  return Map.of("entries",rows,"total",total,"page",p,"pageSize",PAGE_SIZE,"totalPages",pages);
 }

 @GetMapping("/api/admin/console/verification-inbox")
 public Map<String,Object> verificationInbox(){
  admin();
  List<Map<String,Object>> queues=new ArrayList<>();
  queues.add(queue("GUEST_TRANSFERS",
   "SELECT count(*) AS count,min(created_at) AS oldest FROM guest_direct_transfer_claims WHERE status='AWAITING_VERIFICATION'",
   "SELECT c.id,e.title AS subject,u.username AS owner,c.method AS kind,c.amount_paise,c.created_at FROM guest_direct_transfer_claims c JOIN events e ON e.id=c.event_id LEFT JOIN users u ON u.id=e.owner_id WHERE c.status='AWAITING_VERIFICATION' ORDER BY c.created_at LIMIT "+INBOX_ITEMS));
  queues.add(queue("GROUP_REPORTS",
   "SELECT count(*) AS count,min(created_at) AS oldest FROM gift_group_guest_reports WHERE state='REPORTED_UNVERIFIED'",
   "SELECT r.id,g.name AS subject,g.creator_name AS owner,r.method AS kind,r.amount_paise,r.created_at FROM gift_group_guest_reports r JOIN gift_groups g ON g.id=r.group_id WHERE r.state='REPORTED_UNVERIFIED' ORDER BY r.created_at LIMIT "+INBOX_ITEMS));
  queues.add(queue("HOST_ACCOUNTS",
   "SELECT count(*) AS count,min(created_at) AS oldest FROM (SELECT created_at FROM host_upi_accounts WHERE verification_status='PENDING' AND archived_at IS NULL AND enabled UNION ALL SELECT created_at FROM host_bank_accounts WHERE verification_status='PENDING' AND archived_at IS NULL AND enabled) x",
   "SELECT * FROM (SELECT a.id,COALESCE(a.account_label,a.upi_id_masked) AS subject,u.username AS owner,'UPI' AS kind,NULL::bigint AS amount_paise,a.created_at,(a.host_confirmed_at IS NOT NULL) AS host_confirmed FROM host_upi_accounts a JOIN users u ON u.id=a.host_id WHERE a.verification_status='PENDING' AND a.archived_at IS NULL AND a.enabled"+
   " UNION ALL SELECT b.id,COALESCE(b.account_label,b.account_number_masked) AS subject,u.username AS owner,'BANK' AS kind,NULL::bigint AS amount_paise,b.created_at,(b.host_confirmed_at IS NOT NULL) AS host_confirmed FROM host_bank_accounts b JOIN users u ON u.id=b.host_id WHERE b.verification_status='PENDING' AND b.archived_at IS NULL AND b.enabled) x ORDER BY created_at LIMIT "+INBOX_ITEMS));
  queues.add(queue("GROUP_ACCOUNTS",
   "SELECT count(*) AS count,min(created_at) AS oldest FROM gift_group_payment_destinations WHERE status='REGISTERED'",
   "SELECT d.id,g.name AS subject,g.creator_name AS owner,d.kind,NULL::bigint AS amount_paise,d.created_at,d.group_id FROM gift_group_payment_destinations d JOIN gift_groups g ON g.id=d.group_id WHERE d.status='REGISTERED' ORDER BY d.created_at LIMIT "+INBOX_ITEMS));
  queues.add(queue("SHOWCASE",
   "SELECT count(*) AS count,min(submitted_at) AS oldest FROM event_showcase WHERE status='PENDING'",
   "SELECT s.event_id AS id,s.display_title AS subject,u.username AS owner,'SHOWCASE' AS kind,NULL::bigint AS amount_paise,s.submitted_at AS created_at FROM event_showcase s JOIN events e ON e.id=s.event_id LEFT JOIN users u ON u.id=e.owner_id WHERE s.status='PENDING' ORDER BY s.submitted_at LIMIT "+INBOX_ITEMS));
  return Map.of("queues",queues);
 }
 private Map<String,Object> queue(String key,String countSql,String itemsSql){
  Map<String,Object> head=db.queryForMap(countSql);
  Map<String,Object> out=new LinkedHashMap<>();
  out.put("key",key);out.put("count",head.get("count"));out.put("oldest",head.get("oldest"));
  out.put("items",db.queryForList(itemsSql));
  return out;
 }

 @GetMapping("/api/admin/group-payments")
 public Map<String,Object> groupPayments(@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="0") int page){
  admin();String q=search(search);
  String where=" WHERE (LOWER(g.name) LIKE ? ESCAPE '\\' OR LOWER(g.creator_name) LIKE ? ESCAPE '\\' OR LOWER(e.title) LIKE ? ESCAPE '\\')";
  long total=Optional.ofNullable(db.queryForObject("SELECT count(*) FROM gift_groups g JOIN events e ON e.id=g.event_id"+where,Long.class,q,q,q)).orElse(0L);
  int pages=(int)Math.max(1,(total+PAGE_SIZE-1)/PAGE_SIZE);int p=Math.max(0,Math.min(page,pages-1));
  List<Map<String,Object>> rows=db.queryForList("SELECT g.id,g.name,g.creator_name,g.status,g.created_at,e.title AS event_title,COALESCE(pp.switching_enabled,FALSE) AS switching_enabled,pp.changed_at,"+
   "(SELECT count(*) FROM gift_group_payment_destinations d WHERE d.group_id=g.id AND d.status='VERIFIED') AS verified_accounts,"+
   "(SELECT count(*) FROM gift_group_payment_destinations d WHERE d.group_id=g.id AND d.status='REGISTERED') AS unverified_accounts "+
   "FROM gift_groups g JOIN events e ON e.id=g.event_id LEFT JOIN gift_group_payment_permissions pp ON pp.group_id=g.id"+where+" ORDER BY g.created_at DESC,g.id LIMIT ? OFFSET ?",q,q,q,PAGE_SIZE,p*PAGE_SIZE);
  return Map.of("entries",rows,"total",total,"page",p,"pageSize",PAGE_SIZE,"totalPages",pages);
 }
}
