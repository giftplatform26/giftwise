package com.giftplatform.notification;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Transactional, idempotent notification recording for guest-reported transfers.
 * PENDING means not delivered. No email/SMS/WhatsApp transport is enabled here.
 */
@Service
public class ManualClaimNotificationService {
 @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.localization.LocalizationService localization;
 private final JdbcTemplate jdbc;
 public ManualClaimNotificationService(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public void enqueue(UUID eventId,UUID claimId,String guestName,String guestEmail,String guestPhone,long amountPaise,UUID giftId) {
  jdbc.update("""
    INSERT INTO event_thank_you_settings(event_id,host_display_name)
    SELECT id,host_display_name FROM events WHERE id=? ON CONFLICT (event_id) DO NOTHING
    """,eventId);
  var rows=jdbc.query("""
   SELECT e.title,e.contact_email,s.host_display_name,s.email_subject,s.email_body,s.sms_body,
          s.card_template,s.auto_send,s.email_enabled,s.sms_enabled
   FROM events e JOIN event_thank_you_settings s ON s.event_id=e.id WHERE e.id=?
   """,(rs,i)->new String[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
       rs.getString(5),rs.getString(6),rs.getString(7),
       String.valueOf(rs.getBoolean(8)),String.valueOf(rs.getBoolean(9)),String.valueOf(rs.getBoolean(10))},eventId);
  if(rows.isEmpty()) return;
  String[] x=rows.get(0);
  boolean guestAutoSend=Boolean.parseBoolean(x[7]);
  String amount="₹"+String.format(java.util.Locale.ROOT,"%.2f",amountPaise/100.0);
  String gift=giftId==null?"event contribution":"gift contribution";
  Map<String,String> values=Map.of("guestName",guestName,"hostName",x[2],
    "eventName",x[0],"giftName",gift,"amount",amount);
  String language=localization==null?"en":localization.eventLanguage(eventId);
  String safeWording=localization==null?ThankYouMessageRenderer.paymentWording(false):localization.render("notification.transfer.unverified",Map.of(),language);
  String guestSubject=x[3],guestBody=ThankYouMessageRenderer.render(x[4],values);
  if(localization!=null){
   if(x[3].equals("Thank you for celebrating with us"))guestSubject=localization.render("notification.thanks.subject",Map.of(),language);
   if(x[4].equals("Dear {{guestName}}, thank you for your thoughtful contribution to {{eventName}}. With love, {{hostName}}"))guestBody=localization.render("notification.thanks.body",values,language);
  }
  if(guestAutoSend&&guestEmail!=null&&!guestEmail.isBlank()&&Boolean.parseBoolean(x[8])){
    add(eventId,claimId,"GUEST","EMAIL",guestSubject,
      guestBody+"\n\n"+safeWording,x[6]);
  }
  // Host notification is in-app; contact email can be enabled by a later delivery adapter.
  add(eventId,claimId,"HOST","IN_APP",localization==null?"Guest transfer reported":localization.render("notification.host.transfer.subject",Map.of(),language),
    localization==null?guestName+" reported "+amount+" for "+x[0]+" ("+gift+"). Transfer is not verified.":localization.render("notification.host.transfer.body",values,language),null);
 }
 private void add(UUID eventId,UUID claimId,String who,String channel,String subject,String body,String card){
  String language=localization==null?"en":localization.eventLanguage(eventId);
  jdbc.update("""
   INSERT INTO notification_outbox
     (id,event_id,notification_kind,recipient_kind,channel,source_kind,source_id,payment_evidence,subject,message,card_template,language_code)
   VALUES(?,?,'CONTRIBUTION',?,?,'MANUAL_CLAIM',?,'GUEST_REPORTED',?,?,?,?)
   ON CONFLICT (event_id,notification_kind,recipient_kind,channel,source_kind,source_id) DO NOTHING
   """,UUID.randomUUID(),eventId,who,channel,claimId,subject,body,card,language);
 }
}
