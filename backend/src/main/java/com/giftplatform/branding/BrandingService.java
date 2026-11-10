package com.giftplatform.branding;

import com.giftplatform.security.ActorContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.net.URI;

@Service
public class BrandingService {
 private final JdbcTemplate db;private final ActorContext actor;
 public BrandingService(JdbcTemplate db,ActorContext actor){this.db=db;this.actor=actor;}
 public record Settings(String applicationName,String brandColor,String footerText,String supportEmail,String socialLinks,String logoUrl,String faviconUrl,long revision){}
 public record Update(String applicationName,String brandColor,String footerText,String supportEmail,String socialLinks,long revision){}
 public Settings get(){return db.queryForObject("SELECT * FROM platform_branding WHERE id=1",(rs,n)->new Settings(rs.getString("application_name"),rs.getString("brand_color"),rs.getString("footer_text"),rs.getString("support_email"),rs.getString("social_links"),assetUrl(rs.getObject("logo_id")),assetUrl(rs.getObject("favicon_id")),rs.getLong("revision")));}
 private String assetUrl(Object id){return id==null?"":"/api/public/branding/assets/"+id;}
 public UUID admin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform administrator access required.");return actor.requireAuthenticatedUserId();}
 private void audit(UUID id,String action){db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action) VALUES(?,?,?)",UUID.randomUUID(),id,action);}
 static String text(String value,int max,String label,boolean required){if(value==null||value.strip().length()>max||value.chars().anyMatch(Character::isISOControl)||(required&&value.isBlank()))throw new IllegalArgumentException("Enter a valid "+label+" (maximum "+max+" characters).");return value.strip();}
 public static void validate(Update u){
  if(u==null)throw new IllegalArgumentException("Enter branding settings.");
  text(u.applicationName(),80,"application name",true);text(u.footerText(),240,"footer text",true);
  String email=text(u.supportEmail(),254,"support email",false);
  if(!email.isEmpty()&&!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("Please enter a valid support email address.");
  if(u.brandColor()==null||!u.brandColor().matches("#[a-fA-F0-9]{6}"))throw new IllegalArgumentException("Choose a valid brand color.");
  if(u.socialLinks()==null||u.socialLinks().length()>2000)throw new IllegalArgumentException("Social links must be at most 2000 characters.");
  for(String link:u.socialLinks().split("\\R")){if(link.isBlank())continue;try{URI uri=URI.create(link.strip());if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new IllegalArgumentException();}catch(IllegalArgumentException ex){throw new IllegalArgumentException("Each social link must be a valid HTTPS URL, one per line.");}}
 }
 @Transactional public Settings save(Update input){UUID id=admin();validate(input);
  int n=db.update("UPDATE platform_branding SET application_name=?,brand_color=?,footer_text=?,support_email=?,social_links=?,revision=revision+1,updated_by=?,updated_at=now() WHERE id=1 AND revision=?",input.applicationName().strip(),input.brandColor(),input.footerText().strip(),input.supportEmail().strip(),input.socialLinks().strip(),id,input.revision());
  if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Branding changed in another session. Reload and try again.");audit(id,"BRANDING_UPDATED");return get();
 }
 @Transactional public Settings upload(String kind,byte[] data,long revision){UUID admin=admin();String column=column(kind);byte[] image=SafeBrandImage.png(data);UUID id=UUID.randomUUID();
  db.update("INSERT INTO platform_brand_assets(id,image_bytes,created_by) VALUES(?,?,?)",id,image,admin);
  if(db.update("UPDATE platform_branding SET "+column+"=?,revision=revision+1,updated_by=?,updated_at=now() WHERE id=1 AND revision=?",id,admin,revision)!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Branding changed in another session. Reload and try again.");audit(admin,"BRANDING_"+kind.toUpperCase(Locale.ROOT)+"_UPDATED");return get();
 }
 @Transactional public Settings remove(String kind,long revision){UUID admin=admin();String column=column(kind);if(db.update("UPDATE platform_branding SET "+column+"=NULL,revision=revision+1,updated_by=?,updated_at=now() WHERE id=1 AND revision=?",admin,revision)!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Branding changed in another session. Reload and try again.");audit(admin,"BRANDING_"+kind.toUpperCase(Locale.ROOT)+"_REMOVED");return get();}
 private String column(String kind){return switch(kind){case "logo"->"logo_id";case "favicon"->"favicon_id";default->throw new IllegalArgumentException("Choose logo or favicon.");};}
 public byte[] image(UUID id){var rows=db.query("SELECT image_bytes FROM platform_brand_assets WHERE id=?",(rs,n)->rs.getBytes(1),id);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Brand image not found.");return rows.get(0);}
}
