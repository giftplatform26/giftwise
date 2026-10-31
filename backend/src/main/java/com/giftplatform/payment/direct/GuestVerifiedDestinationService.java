package com.giftplatform.payment.direct;

import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.hostpayment.qr.UpiQrImageCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Read-only, fail-closed disclosure of eligible verified destinations for a published invitation.
 * Only host-confirmed or provider-verified preferred destinations are disclosed. This does not verify a payment.
 */
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
@Service
public class GuestVerifiedDestinationService {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(GuestVerifiedDestinationService.class);
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.payment.control.PaymentControlService controls;
 private final JdbcTemplate jdbc;
 private final PaymentDataProtector protector;
 private final boolean active;
 public GuestVerifiedDestinationService(JdbcTemplate jdbc,PaymentDataProtector protector,
   @Value("${gift.payment.direct-transfers.enabled:false}") boolean transfers,
   @Value("${gift.payment.direct-transfers.reconciliation.enabled:false}") boolean reconciliation) {
  this.jdbc=jdbc;this.protector=protector;this.active=transfers; // Read-only manual destination disclosure, not payment verification.
 }
 public record Destination(UUID id,String method,boolean preferred,String label,
                            String upiId,String qrPngBase64,String beneficiaryName,
                            String accountNumber,String ifsc) {}
 @Transactional(readOnly=true)
 public List<Destination> list(UUID token) {
  if(!active) return List.of();
  if(token==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable");
  List<UUID[]> owners=jdbc.query("SELECT id,owner_id FROM events WHERE public_token=? AND status='PUBLISHED'",
   (rs,i)->new UUID[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class)},token);
  if(owners.size()!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable");
  UUID host=owners.get(0)[1];
  List<Destination> out=new ArrayList<>();
  jdbc.query("""
    SELECT id,TRUE AS preferred,upi_id_encrypted,upi_id_masked FROM host_upi_accounts
    WHERE host_id=? AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.event_id=? AND l.host_id=host_upi_accounts.host_id AND l.destination_id=host_upi_accounts.id AND l.destination_kind='UPI' AND l.enabled AND l.preferred) AND enabled=TRUE AND verification_status NOT IN ('FAILED','REVOKED') AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED')
      AND archived_at IS NULL
      AND EXISTS (SELECT 1 FROM host_upi_qr_images q
                  WHERE q.host_id=host_upi_accounts.host_id
                    AND q.upi_account_id=host_upi_accounts.id
                    AND q.source IN ('UPLOADED','GENERATED')
                    AND q.payload_sha256 IS NOT NULL)
    ORDER BY preferred DESC,created_at ASC,id ASC
    """,rs->{
      UUID id=rs.getObject(1,UUID.class);
      String upi;
      try { upi=protector.decrypt(host,id,"upi_id",rs.getString(3)); }
      catch (IllegalArgumentException ex) {
       log.warn("Omitting unreadable guest UPI destination {}; check payment encryption key or re-enter account",id);
       return;
      }
      byte[] png=UpiQrImageCodec.generatePng(upi,"Gift recipient");
      out.add(new Destination(id,"UPI",rs.getBoolean(2),rs.getString(4),upi,
       Base64.getEncoder().encodeToString(png),null,null,null));
    },host,owners.get(0)[0]);
  jdbc.query("""
    SELECT id,TRUE AS preferred,account_number_encrypted,ifsc_encrypted,account_holder_encrypted,account_number_masked
    FROM host_bank_accounts WHERE host_id=? AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.event_id=? AND l.host_id=host_bank_accounts.host_id AND l.destination_id=host_bank_accounts.id AND l.destination_kind='BANK' AND l.enabled AND l.preferred) AND enabled=TRUE AND verification_status NOT IN ('FAILED','REVOKED') AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED') AND archived_at IS NULL
    ORDER BY preferred DESC,created_at ASC,id ASC
    """,rs->{
      UUID id=rs.getObject(1,UUID.class);
      String holder,number,ifsc;
      try {
       holder=protector.decrypt(host,id,"account_holder",rs.getString(5));
       number=protector.decrypt(host,id,"account_number",rs.getString(3));
       ifsc=protector.decrypt(host,id,"ifsc",rs.getString(4));
      } catch (IllegalArgumentException ex) {
       log.warn("Omitting unreadable guest bank destination {}; check payment encryption key or re-enter account",id);
       return;
      }
      out.add(new Destination(id,"BANK",rs.getBoolean(2),rs.getString(6),null,null,holder,number,ifsc));
    },host,owners.get(0)[0]);
  return controls==null?List.copyOf(out):out.stream().filter(d->Boolean.TRUE.equals(controls.event(owners.get(0)[0]).get(d.method()))).toList();
 }
}
