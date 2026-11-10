package com.giftplatform.publicevent;

import com.giftplatform.admin.GrowthSettingsService;
import com.giftplatform.security.ActorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/** Link-preview services receive event details only after the host opts in. */
@RestController
public class SharingPreviewController {
 private final JdbcTemplate db;private final ActorContext actor;private final GrowthSettingsService settings;
 public SharingPreviewController(JdbcTemplate db,ActorContext actor,GrowthSettingsService settings){this.db=db;this.actor=actor;this.settings=settings;}
 public record Consent(@NotNull Boolean enabled){}
 public record Preference(boolean enabled,boolean platformEnabled){}
 public record Preview(String title,String occasion,String date){}
 private void owned(UUID id){if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM events WHERE id=? AND owner_id=?)",Boolean.class,id,actor.requireAuthenticatedUserId())))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");}
 @GetMapping("/api/v1/events/{id}/share-preview")
 public ResponseEntity<Preference> preference(@PathVariable UUID id){owned(id);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Preference(Boolean.TRUE.equals(db.queryForObject("SELECT share_preview_enabled FROM events WHERE id=?",Boolean.class,id)),settings.enabled("SHARE_PREVIEWS")));}
 @PutMapping("/api/v1/events/{id}/share-preview")
 @Transactional
 public Preference consent(@PathVariable UUID id,@Valid @RequestBody Consent input){owned(id);boolean available=settings.enabled("SHARE_PREVIEWS");if(input.enabled()&&!available)throw new ResponseStatusException(HttpStatus.CONFLICT,"Sharing previews are disabled by the platform administrator");db.update("UPDATE events SET share_preview_enabled=? WHERE id=?",input.enabled(),id);db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id) VALUES(?,?,?,?)",UUID.randomUUID(),actor.requireAuthenticatedUserId(),input.enabled()?"SHARE_PREVIEW_ENABLED":"SHARE_PREVIEW_DISABLED",id);return new Preference(input.enabled(),available);}
 private Preview preview(UUID token){
  if(!settings.enabled("SHARE_PREVIEWS"))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Preview unavailable");
  return db.query("SELECT title,occasion_type,event_date FROM events WHERE public_token=? AND status='PUBLISHED' AND share_preview_enabled",(rs,n)->new Preview(rs.getString(1),rs.getString(2),rs.getString(3)),token).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Preview unavailable"));
 }
 @GetMapping("/api/public/events/{token}/share-preview")
 public ResponseEntity<Preview> metadata(@PathVariable UUID token){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag","noindex, nofollow, noarchive").body(preview(token));}
 @GetMapping(value="/api/public/events/{token}/share-preview.png",produces="image/png")
 public ResponseEntity<byte[]> image(@PathVariable UUID token) throws IOException {
  var p=preview(token);var canvas=new BufferedImage(1200,630,BufferedImage.TYPE_INT_RGB);var g=canvas.createGraphics();
  try{
   g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
   g.setColor(new Color(250,245,240));g.fillRect(0,0,1200,630);g.setColor(new Color(107,56,80));
   g.setFont(new Font("SansSerif",Font.BOLD,24));g.drawString("YOU ARE INVITED",80,100);g.setFont(new Font("Serif",Font.BOLD,56));
   String text=p.title().replaceAll("[\\p{Cntrl}]"," ");int y=215;
   for(int line=0;line<3&&!text.isEmpty();line++){
    int end=text.length();while(end>1&&g.getFontMetrics().stringWidth(text.substring(0,end))>1040)end--;
    if(end<text.length()){int space=text.lastIndexOf(' ',end);if(space>0)end=space;}
    String part=text.substring(0,end);text=text.substring(end).stripLeading();if(line==2&&!text.isEmpty())part=part.substring(0,Math.max(0,part.length()-1))+"…";
    g.drawString(part,80,y);y+=75;
   }
   g.setFont(new Font("SansSerif",Font.PLAIN,26));g.drawString(p.occasion().replace('_',' ')+(p.date()==null?"":"  ·  "+p.date()),80,505);
   g.setFont(new Font("SansSerif",Font.PLAIN,20));g.drawString("Open the invitation to celebrate together",80,565);
  }finally{g.dispose();}
  var bytes=new ByteArrayOutputStream();ImageIO.write(canvas,"png",bytes);
  return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).cacheControl(CacheControl.noStore()).header("X-Robots-Tag","noindex, nofollow, noarchive").header("X-Content-Type-Options","nosniff").body(bytes.toByteArray());
 }
}
