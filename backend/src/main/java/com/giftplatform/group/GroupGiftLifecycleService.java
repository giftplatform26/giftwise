package com.giftplatform.group;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Gift planning never moves money or rewrites contribution history. */
@Service
public class GroupGiftLifecycleService {
 private final JdbcTemplate db;
 public GroupGiftLifecycleService(JdbcTemplate db){this.db=db;}
 private Map<String,Object> group(UUID id,UUID creator){
  var rows=db.queryForList("SELECT event_id,admin_id,selected_gift_id,reserved_gift_id,gift_published_at,independent_gift FROM gift_groups WHERE id=? AND admin_id=? FOR UPDATE",id,creator);
  if(rows.size()!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Group not found");
  if(rows.getFirst().get("gift_published_at")!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Published gift plans cannot be changed");
  return rows.getFirst();
 }
 @Transactional
 public Map<String,Object> select(UUID group,UUID creator,UUID gift){
  if(gift==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a wishlist gift");
  var g=group(group,creator);
  if(Boolean.TRUE.equals(g.get("independent_gift")))throw new ResponseStatusException(HttpStatus.CONFLICT,"This group chooses its own gift outside the host wishlist");
  if(g.get("selected_gift_id")!=null&&!gift.equals(g.get("selected_gift_id")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Release the current gift before choosing another");
  var gifts=db.queryForList("SELECT id,name,description,product_url,(target_amount*100)::bigint AS target_paise FROM gifts WHERE id=? AND event_id=? AND status='ACTIVE' AND visibility='PUBLIC' FOR UPDATE",gift,g.get("event_id"));
  if(gifts.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"This gift is no longer available");
  if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM gift_groups WHERE reserved_gift_id=? AND id<>?)",Boolean.class,gift,group)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Another group is already planning a lovely surprise for this gift");
  var details=gifts.getFirst();
  boolean verified=Boolean.TRUE.equals(db.queryForObject("SELECT email_verified_at IS NOT NULL FROM gift_group_accounts WHERE id=?",Boolean.class,creator));
  db.update("UPDATE gift_groups SET selected_gift_id=?,reserved_gift_id=?,gift_name=?,gift_description=?,gift_link=?,target_paise=? WHERE id=?",gift,verified?gift:null,
   truncate((String)details.get("name"),160),truncate((String)details.get("description"),500),truncate((String)details.get("product_url"),1000),details.get("target_paise"),group);
  return Map.of("reserved",verified,"giftId",gift,"message",verified?"Your group is creating a lovely surprise. This gift is held with love for you.":"Gift details added. Verify your creator email in Account to hold this gift for your group. Until then, it stays available on the wishlist.");
 }
 private static String truncate(String value,int limit){return value==null?null:value.substring(0,Math.min(value.length(),limit));}
 @Transactional
 public void activateForCreator(UUID creator){
  var pending=db.query("SELECT id,selected_gift_id FROM gift_groups WHERE admin_id=? AND selected_gift_id IS NOT NULL AND reserved_gift_id IS NULL ORDER BY id",(rs,n)->new UUID[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class)},creator);
  for(var row:pending){
   // A gift may have been reserved by another verified group while email verification was pending.
   try{select(row[0],creator,row[1]);}catch(ResponseStatusException ex){if(ex.getStatusCode()!=HttpStatus.CONFLICT&&ex.getStatusCode()!=HttpStatus.NOT_FOUND)throw ex;}
  }
 }
 @Transactional
 public Map<String,String> release(UUID id,UUID creator){
  var g=group(id,creator);
  if(g.get("selected_gift_id")==null&&g.get("reserved_gift_id")==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"This group has no wishlist gift to release");
  if(g.get("selected_gift_id")!=null)db.queryForList("SELECT id FROM gifts WHERE id=? FOR UPDATE",g.get("selected_gift_id"));
  db.update("UPDATE gift_groups SET selected_gift_id=NULL,reserved_gift_id=NULL,gift_name=NULL,gift_description=NULL,gift_link=NULL,target_paise=NULL WHERE id=?",id);
  return Map.of("message","Gift released with love. Your group can keep collecting together or choose another wishlist gift. All contributions and history are preserved.");
 }
}
