package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class GrowthSettingsService {
 public static final Set<String> CODES=Set.of("OCCASION_STARTERS","SMART_DASHBOARD","PUBLIC_DEMO","SHARE_PREVIEWS","GUEST_DISCOVERY","PUBLIC_KEEPSAKES","INTERACTION_ANALYTICS");
 private final JdbcTemplate db;
 private final ActorContext actor;
 public GrowthSettingsService(JdbcTemplate db,ActorContext actor){this.db=db;this.actor=actor;}
 public record Settings(Map<String,Boolean> features,long version){}
 @Transactional
 public Settings current(){
  // A shared lock keeps flags and revision consistent with concurrent admin updates.
  Long version=db.queryForObject("SELECT version FROM platform_growth_settings WHERE id=1 FOR SHARE",Long.class);
  Map<String,Boolean> flags=new TreeMap<>();
  db.query("SELECT code,enabled FROM platform_growth_features",(org.springframework.jdbc.core.RowCallbackHandler)rs->flags.put(rs.getString(1),rs.getBoolean(2)));
  return new Settings(Map.copyOf(flags),version);
 }
 public boolean enabled(String code){return Boolean.TRUE.equals(db.queryForObject("SELECT enabled FROM platform_growth_features WHERE code=?",Boolean.class,code));}
 @Transactional
 public Settings update(Settings input){
  if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform administrator access required");
  if(input==null||input.features()==null||!input.features().keySet().equals(CODES)||input.features().values().stream().anyMatch(Objects::isNull)||input.version()<0)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supply all supported feature switches and the current version");
  UUID who=actor.requireAuthenticatedUserId();
  if(db.update("UPDATE platform_growth_settings SET version=version+1,changed_by=?,changed_at=now() WHERE id=1 AND version=?",who,input.version())!=1)
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Settings changed. Reload before saving");
  input.features().forEach((code,value)->db.update("UPDATE platform_growth_features SET enabled=? WHERE code=?",value,code));
  db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action) VALUES(?,?,?)",UUID.randomUUID(),who,"GROWTH_SETTINGS_UPDATED");
  return current();
 }
}
