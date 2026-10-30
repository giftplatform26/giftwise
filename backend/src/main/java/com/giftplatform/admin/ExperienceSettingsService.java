package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class ExperienceSettingsService {
 private final JdbcTemplate db;
 private final ActorContext actors;
 public ExperienceSettingsService(JdbcTemplate db,ActorContext actors){this.db=db;this.actors=actors;}
 public record Settings(String viewMode){}
 public Settings current(){
  String mode=db.queryForObject("SELECT view_mode FROM platform_experience_settings WHERE id=1",String.class);
  return new Settings("FOCUSED".equals(mode)?"FOCUSED":"CLASSIC");
 }
 @Transactional public Settings update(Settings input){
  if(!actors.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  UUID admin=actors.requireAuthenticatedUserId();
  if(input==null||!("CLASSIC".equals(input.viewMode())||"FOCUSED".equals(input.viewMode())))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose Classic or Focused");
  db.update("UPDATE platform_experience_settings SET view_mode=?,changed_by=?,changed_at=now() WHERE id=1",input.viewMode(),admin);
  db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action) VALUES (?,?,?)",UUID.randomUUID(),admin,"EXPERIENCE_"+input.viewMode());
  return current();
 }
}
