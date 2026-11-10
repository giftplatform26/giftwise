package com.giftplatform.asset;

import com.giftplatform.security.ActorContext;
import com.giftplatform.monetization.CommercialAccessService;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.audit.entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class HostInvitationUploadService {
 private final JdbcTemplate db;private final ActorContext actor;private final CommercialAccessService commerce;private final GalleryImageProcessor processor;private final AuditService audit;
 public HostInvitationUploadService(JdbcTemplate db,ActorContext actor,CommercialAccessService commerce,GalleryImageProcessor processor,AuditService audit){this.db=db;this.actor=actor;this.commerce=commerce;this.processor=processor;this.audit=audit;}
 public record Access(UUID hostId,boolean enabled,boolean requiresPayment,boolean paid,boolean accessible){}
 public record Upload(UUID id,String category,String title,int width,int height){}
 public Access access(UUID host){
  var rows=db.queryForList("SELECT enabled,requires_payment FROM host_invitation_upload_controls WHERE host_id=?",host);
  boolean enabled=!rows.isEmpty()&&Boolean.TRUE.equals(rows.getFirst().get("enabled"));boolean requires=rows.isEmpty()||Boolean.TRUE.equals(rows.getFirst().get("requires_payment"));boolean paid=commerce.premium(host);
  return new Access(host,enabled,requires,paid,enabled&&(!requires||paid));
 }
 public Access mine(){return access(actor.requireAuthenticatedUserId());}
 private UUID requireAccess(){UUID host=actor.requireAuthenticatedUserId();Access a=access(host);if(!a.enabled())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Your administrator has not enabled custom invitation uploads");if(!a.accessible())throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"An active premium host plan is required for custom uploads");return host;}
 public List<Upload> list(){UUID host=requireAccess();return db.query("SELECT id,category,title,width,height FROM host_invitation_uploads WHERE host_id=? ORDER BY created_at DESC",(rs,i)->new Upload(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5)),host);}
 @Transactional public Upload upload(String category,String title,byte[] bytes){
  UUID host=requireAccess();if(!Set.of("BACKGROUND","DECORATION").contains(category==null?"":category)||title==null||title.isBlank()||title.length()>120)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a background or sticker and a title");
  var image=processor.process(bytes,category);UUID id=UUID.randomUUID();
  db.update("INSERT INTO host_invitation_uploads(id,host_id,category,title,image_png,width,height) VALUES(?,?,?,?,?,?,?)",id,host,category,title.strip(),image.png(),image.width(),image.height());
  return new Upload(id,category,title.strip(),image.width(),image.height());
 }
 public byte[] image(UUID id){UUID host=requireAccess();return db.query("SELECT image_png FROM host_invitation_uploads WHERE id=? AND host_id=?",(rs,i)->rs.getBytes(1),id,host).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 public Access adminAccess(UUID host){requireAdmin();return access(host);}
 @Transactional public Access configure(UUID host,boolean enabled,boolean requiresPayment){
  UUID admin=requireAdmin();if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM users WHERE id=? AND role='CUSTOMER'",Integer.class,host)))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Host not found");
  Access before=access(host);db.update("INSERT INTO host_invitation_upload_controls(host_id,enabled,requires_payment,changed_by) VALUES(?,?,?,?) ON CONFLICT(host_id) DO UPDATE SET enabled=EXCLUDED.enabled,requires_payment=EXCLUDED.requires_payment,changed_by=EXCLUDED.changed_by,changed_at=now()",host,enabled,requiresPayment,admin);
  audit.recordChange(admin,AuditActorType.ADMIN,"HOST_INVITATION_UPLOAD_ACCESS",host,AuditAction.UPDATE,Map.of("enabled",before.enabled(),"requiresPayment",before.requiresPayment()),Map.of("enabled",enabled,"requiresPayment",requiresPayment),null);return access(host);
 }
 private UUID requireAdmin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);return actor.requireAuthenticatedUserId();}
}
