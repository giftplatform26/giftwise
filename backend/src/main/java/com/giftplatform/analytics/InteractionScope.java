package com.giftplatform.analytics;

import com.giftplatform.security.GiftUserPrincipal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class InteractionScope {
 private final JdbcTemplate db;
 public InteractionScope(JdbcTemplate db){this.db=db;}
 public record Scope(UUID actor,String role,UUID host,UUID event){}
 public Scope resolve(UUID event,UUID token){
  var auth=SecurityContextHolder.getContext().getAuthentication();
  UUID actor=auth!=null&&auth.getPrincipal() instanceof GiftUserPrincipal p?p.getUserId():null;
  boolean admin=actor!=null&&auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
  String role=actor==null?"GUEST":admin?"ADMIN":"HOST";
  if(token!=null){var rows=db.query("SELECT owner_id,id FROM events WHERE public_token=? AND status='PUBLISHED'",(rs,i)->new UUID[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class)},token);if(!rows.isEmpty())return new Scope(actor,role,rows.getFirst()[0],rows.getFirst()[1]);}
  if(event!=null&&actor!=null){var rows=db.query("SELECT owner_id,id FROM events WHERE id=? AND (owner_id=? OR ?)",(rs,i)->new UUID[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class)},event,actor,admin);if(!rows.isEmpty())return new Scope(actor,role,rows.getFirst()[0],rows.getFirst()[1]);}
  return new Scope(actor,role,admin?null:actor,null);
 }
}
