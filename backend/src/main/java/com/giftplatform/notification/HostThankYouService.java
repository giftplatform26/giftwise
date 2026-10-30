package com.giftplatform.notification;

import com.giftplatform.security.ActorContext;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HostThankYouService {
  private final JdbcTemplate jdbc;
  private final ActorContext actors;
  public HostThankYouService(JdbcTemplate jdbc,ActorContext actors){this.jdbc=jdbc;this.actors=actors;}
  public record Settings(String hostName,String emailSubject,String emailBody,String smsBody,
    String cardTemplate,boolean autoSend,boolean emailEnabled,boolean smsEnabled,boolean whatsappEnabled){}
  public record Update(String hostName,String emailSubject,String emailBody,String smsBody,String cardTemplate,
    boolean autoSend,boolean emailEnabled,boolean smsEnabled,boolean whatsappEnabled){}
  private void owner(UUID id){
    UUID actor=actors.requireAuthenticatedUserId();
    Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE id=? AND owner_id=?",Integer.class,id,actor);
    if(n==null||n!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
  }
  @Transactional
  public Settings settings(UUID id){
    owner(id);
    jdbc.update("""
      INSERT INTO event_thank_you_settings(event_id,host_display_name)
      SELECT id,host_display_name FROM events WHERE id=? ON CONFLICT (event_id) DO NOTHING
      """,id);
    return jdbc.query("""
      SELECT COALESCE(e.host_display_name,s.host_display_name),s.email_subject,s.email_body,s.sms_body,
             s.card_template,s.auto_send,s.email_enabled,s.sms_enabled,s.whatsapp_enabled
      FROM events e JOIN event_thank_you_settings s ON s.event_id=e.id WHERE e.id=?
      """,(rs,i)->new Settings(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
       rs.getString(5),rs.getBoolean(6),rs.getBoolean(7),rs.getBoolean(8),rs.getBoolean(9)),id)
      .stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Settings missing"));
  }
  @Transactional
  public Settings update(UUID id,Update r){
    owner(id);
    jdbc.update("""
      INSERT INTO event_thank_you_settings(event_id,host_display_name)
      SELECT id,host_display_name FROM events WHERE id=? ON CONFLICT (event_id) DO NOTHING
      """,id);
    if(r==null||!valid(r.hostName(),200)||!valid(r.emailSubject(),200)||!valid(r.emailBody(),10000)
      ||!valid(r.smsBody(),1000)||!Set.of("CLASSIC","FLORAL","MODERN").contains(r.cardTemplate()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid thank-you configuration");
    jdbc.update("""
      UPDATE event_thank_you_settings SET host_display_name=?,email_subject=?,email_body=?,sms_body=?,
        card_template=?,auto_send=?,email_enabled=?,sms_enabled=?,whatsapp_enabled=?,updated_at=now()
      WHERE event_id=?
      """,r.hostName().strip(),r.emailSubject().strip(),r.emailBody().strip(),r.smsBody().strip(),
      r.cardTemplate(),r.autoSend(),r.emailEnabled(),r.smsEnabled(),r.whatsappEnabled(),id);
    return settings(id);
  }
  @Transactional(readOnly=true)
  public List<Map<String,Object>> history(UUID id){
    owner(id);
    return jdbc.query("""
      SELECT id,notification_kind,recipient_kind,channel,payment_evidence,status,created_at,sent_at
      FROM notification_outbox WHERE event_id=? ORDER BY created_at DESC LIMIT 100
      """,(rs,i)->{
        Map<String,Object> m=new LinkedHashMap<>();
        for(String c:List.of("id","notification_kind","recipient_kind","channel","payment_evidence","status","created_at","sent_at"))
          m.put(c,rs.getObject(c));
        return m;
      },id);
  }
  private static boolean valid(String s,int max){return s!=null&&!s.isBlank()&&s.length()<=max;}
}
