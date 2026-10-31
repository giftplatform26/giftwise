package com.giftplatform.payment.direct;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GuestDirectTransferService {
 private final JdbcTemplate jdbc;
 private final com.giftplatform.notification.ManualClaimNotificationService notifications;
 private final boolean enabled;
 @org.springframework.beans.factory.annotation.Autowired
 public GuestDirectTransferService(JdbcTemplate jdbc, com.giftplatform.notification.ManualClaimNotificationService notifications,
  @Value("${gift.payment.direct-transfers.enabled:false}") boolean enabled,
  @Value("${gift.payment.direct-transfers.reconciliation.enabled:false}") boolean reconciliationEnabled) {
  this.notifications=notifications;
  this.jdbc=jdbc; this.enabled=enabled; // Manual reports only; never provider-confirmed payment capture.
 }
 public record Destination(UUID id,String method,String masked,boolean available) {}
 public record ClaimRequest(UUID destinationId,String method,UUID giftId,long amountPaise,
                            String guestName,String guestEmail,String guestPhone,String transactionReference,String idempotencyKey) {}
 public record ClaimResult(UUID claimId,String status,String message) {}
 public GuestDirectTransferService(JdbcTemplate jdbc,
  boolean enabled, boolean reconciliationEnabled) {
  
  this.jdbc=jdbc; this.enabled=enabled; this.notifications=null;
 }
 private void gate() {
  if(!enabled) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
   "Manual transfer reporting is disabled");
 }
 private UUID event(UUID token) {
  if(token==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable");
  List<UUID> rows=jdbc.query("SELECT id FROM events WHERE public_token=? AND status='PUBLISHED'",
   (rs,i)->rs.getObject(1,UUID.class),token);
  if(rows.size()!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable");
  return rows.get(0);
 }
 @Transactional(readOnly=true)
 public List<Destination> destinations(UUID token) {
  gate(); UUID eventId=event(token);
  // No decrypted bank account or UPI identifier is exposed by this read-only endpoint.
  return jdbc.query("""
    SELECT id,'UPI' AS method,upi_id_masked AS masked FROM host_upi_accounts
    WHERE host_id=(SELECT owner_id FROM events WHERE id=?) AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.event_id=? AND l.host_id=host_upi_accounts.host_id AND l.destination_id=host_upi_accounts.id AND l.destination_kind='UPI' AND l.enabled AND l.preferred)
      AND enabled=TRUE AND archived_at IS NULL AND verification_status NOT IN ('FAILED','REVOKED') AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED')
      AND EXISTS (SELECT 1 FROM host_upi_qr_images q WHERE q.host_id=host_upi_accounts.host_id AND q.upi_account_id=host_upi_accounts.id AND q.source IN ('UPLOADED','GENERATED') AND q.payload_sha256 IS NOT NULL)
    UNION ALL
    SELECT id,'BANK',account_number_masked FROM host_bank_accounts
    WHERE host_id=(SELECT owner_id FROM events WHERE id=?) AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.event_id=? AND l.host_id=host_bank_accounts.host_id AND l.destination_id=host_bank_accounts.id AND l.destination_kind='BANK' AND l.enabled AND l.preferred)
      AND enabled=TRUE AND archived_at IS NULL AND verification_status NOT IN ('FAILED','REVOKED') AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED')
    """,(rs,i)->new Destination(rs.getObject(1,UUID.class),rs.getString(2),
      rs.getString(3),true),eventId,eventId,eventId,eventId);
 }
 @Transactional
 public ClaimResult claim(UUID token,ClaimRequest r) {
  gate(); UUID eventId=event(token);
  if(r==null || r.destinationId()==null || r.method()==null ||
    !java.util.Set.of("UPI","BANK").contains(r.method()) ||
    r.amountPaise()<100 || r.amountPaise()>100000000 ||
    r.guestName()==null || r.guestName().isBlank() || r.guestName().length()>200 ||
    r.transactionReference()==null || !r.transactionReference().matches("[A-Za-z0-9\\-]{8,40}") ||
    (r.guestEmail()!=null && (r.guestEmail().length()>320 || !r.guestEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) ||
    (r.guestPhone()!=null && (r.guestPhone().length()>30 || !r.guestPhone().matches("[+0-9 ()-]{5,30}"))) ||
    r.idempotencyKey()==null || !r.idempotencyKey().matches("[A-Za-z0-9-]{8,120}"))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid transfer confirmation");
  if(r.giftId()!=null) {
   Integer count=jdbc.queryForObject("""
    SELECT COUNT(*) FROM gifts WHERE id=? AND event_id=? AND status='ACTIVE' AND visibility='PUBLIC'
    """,Integer.class,r.giftId(),eventId);
   if(count==null || count!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gift unavailable");
  }
  String table="UPI".equals(r.method())?"host_upi_accounts":"host_bank_accounts";
  Integer eligible=jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE id=? AND " +
    "host_id=(SELECT owner_id FROM events WHERE id=?) AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.event_id=? AND l.host_id="+table+".host_id AND l.destination_id="+table+".id AND l.destination_kind='"+r.method()+"' AND l.enabled AND l.preferred) AND enabled=TRUE " +
    "AND archived_at IS NULL AND verification_status NOT IN ('FAILED','REVOKED') AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED')" +
    ("UPI".equals(r.method()) ? " AND EXISTS (SELECT 1 FROM host_upi_qr_images q WHERE q.host_id="+table+".host_id AND q.upi_account_id="+table+".id AND q.source IN ('UPLOADED','GENERATED') AND q.payload_sha256 IS NOT NULL)" : ""),Integer.class,r.destinationId(),eventId,eventId);
  if(eligible==null || eligible!=1)
   throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Eligible destination unavailable");
  String reference=sha256(r.method()+":"+r.transactionReference().strip().toUpperCase(java.util.Locale.ROOT));
  // Serialize idempotent retries; duplicate payment references cannot be claimed twice.
  jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,
   "guest-direct:"+eventId+":"+r.idempotencyKey());
  List<ClaimResult> prior=jdbc.query("""
   SELECT id,status FROM guest_direct_transfer_claims WHERE event_id=? AND client_key=?
   """,(rs,i)->new ClaimResult(rs.getObject(1,UUID.class),rs.getString(2),
     "Guest report received; payment is not verified"),eventId,r.idempotencyKey());
  if(!prior.isEmpty()) {
   Integer matching=jdbc.queryForObject("""
    SELECT COUNT(*) FROM guest_direct_transfer_claims WHERE id=? AND destination_id=?
      AND method=? AND amount_paise=? AND reference_hash=?
      AND guest_name=? AND gift_id IS NOT DISTINCT FROM ?
      AND guest_email IS NOT DISTINCT FROM ? AND guest_phone IS NOT DISTINCT FROM ?
    """,Integer.class,prior.get(0).claimId(),r.destinationId(),r.method(),
     r.amountPaise(),reference,r.guestName().strip(),r.giftId(),clean(r.guestEmail()),clean(r.guestPhone()));
   if(matching==null || matching!=1) throw new ResponseStatusException(HttpStatus.CONFLICT,"Idempotency conflict");
   return prior.get(0);
  }
  // Lock the event across gift selection, amount calculation and claim insertion.
  jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,
    "manual-display-event:"+eventId);
  // Match verified payment allocation's gift-row lock before reading gift progress.
  if(r.giftId()!=null) {
   var locked=jdbc.query("""
     SELECT id FROM gifts WHERE id=? AND event_id=? AND status='ACTIVE' AND visibility='PUBLIC' FOR UPDATE
     """,(rs,i)->rs.getObject(1,UUID.class),r.giftId(),eventId);
   if(locked.size()!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gift unavailable");
   if(Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM gift_groups WHERE reserved_gift_id=?)",Boolean.class,r.giftId())))throw new ResponseStatusException(HttpStatus.CONFLICT,"A surprise is waiting for this gift");
  }
  UUID id=UUID.randomUUID();
  try {
   jdbc.update("""
    INSERT INTO guest_direct_transfer_claims
      (id,event_id,gift_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key,guest_email,guest_phone)
    VALUES(?,?,?,?,?,?,?,?,?,?,?)
    """,id,eventId,r.giftId(),r.destinationId(),r.method(),r.amountPaise(),
    r.guestName().strip(),reference,r.idempotencyKey(),clean(r.guestEmail()),clean(r.guestPhone()));
  } catch(org.springframework.dao.DataIntegrityViolationException e) {
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Reference already submitted");
  }
  // Display-only allocation: not a verified contribution or financial posting.
  long giftPart=0L;
  if(r.giftId()!=null) {
   Long target=jdbc.queryForObject("SELECT ROUND(target_amount*100)::bigint FROM gifts WHERE id=? AND event_id=?",Long.class,r.giftId(),eventId);
   if(target==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gift unavailable");
   Long captured=jdbc.queryForObject("""
     SELECT COALESCE(SUM(ca.amount_paise),0) FROM contribution_allocations ca
     JOIN contributions c ON c.id=ca.contribution_id
     JOIN payment_orders po ON po.id=c.payment_order_id
     WHERE ca.event_id=? AND ca.gift_id=? AND ca.allocation_type='GIFT' AND po.status='CAPTURED'
     """,Long.class,eventId,r.giftId());
   Long manual=jdbc.queryForObject("""
     SELECT COALESCE(SUM(a.gift_paise),0) FROM guest_manual_display_allocations a
     JOIN guest_direct_transfer_claims cl ON cl.id=a.claim_id
     WHERE a.event_id=? AND a.gift_id=? AND cl.status='AWAITING_VERIFICATION'
     """,Long.class,eventId,r.giftId());
   long remaining=Math.max(0L,target-Math.addExact(captured==null?0:captured,manual==null?0:manual));
   giftPart=Math.min(remaining,r.amountPaise());
  }
  jdbc.update("""
    INSERT INTO guest_manual_display_allocations(claim_id,event_id,gift_id,gift_paise,wallet_paise)
    VALUES(?,?,?,?,?)
    """,id,eventId,r.giftId(),giftPart,r.amountPaise()-giftPart);
  if(notifications!=null) notifications.enqueue(eventId,id,r.guestName().strip(),clean(r.guestEmail()),clean(r.guestPhone()),r.amountPaise(),r.giftId());
  return new ClaimResult(id,"AWAITING_VERIFICATION",
   "Guest report received; payment is not verified");
 }
 private static String clean(String value) { return value==null || value.isBlank()?null:value.strip(); }
 private static String sha256(String value) {
  try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
     .digest(value.getBytes(StandardCharsets.UTF_8))); }
  catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
}
