package com.giftplatform.payment.report;

import com.giftplatform.security.ActorContext;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HostContributionReportService {
  private final JdbcTemplate jdbc;
  private final ActorContext actors;
  public HostContributionReportService(JdbcTemplate jdbc, ActorContext actors) {
    this.jdbc=jdbc; this.actors=actors;
  }
  public record Entry(UUID id, String source, String guestName, UUID giftId, String giftName,
    String contributionType, String method, long amountPaise, long giftPaise,
    long walletPaise, String verificationStatus, OffsetDateTime createdAt) {}
  public record GiftOption(UUID id, String name, long targetPaise) {}
  public record Report(UUID eventId, long pendingPaise, long verifiedPaise,
    long verifiedGiftPaise, long verifiedWalletPaise, List<Entry> entries,
    List<GiftOption> gifts, UUID selectedGiftId, long selectedGiftTargetPaise,
    long matchingCount, long reportedGiftPaise, long reportedWalletPaise,
    long selectedGiftVerifiedPaise, long selectedGiftReportedPaise) {}

  public static boolean matches(Entry e, String type, String status, UUID giftId) {
    if(giftId!=null && !giftId.equals(e.giftId())) return false;
    if(!"ALL".equals(status) && !status.equals(e.verificationStatus())) return false;
    return switch(type) {
      case "ALL" -> true;
      case "GIFT" -> e.giftPaise()>0 || ("VERIFICATION_PENDING".equals(e.verificationStatus()) && e.giftId()!=null);
      case "WALLET" -> e.walletPaise()>0 || ("VERIFICATION_PENDING".equals(e.verificationStatus()) && e.giftId()==null);
      default -> false;
    };
  }
  public static boolean withinDates(OffsetDateTime createdAt, LocalDate from, LocalDate to) {
    if(createdAt==null) return from==null && to==null;
    // Date filters use UTC consistently; the UI explicitly labels them UTC.
    LocalDate day=createdAt.atZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDate();
    return (from==null || !day.isBefore(from)) && (to==null || !day.isAfter(to));
  }
  // CSV is for people: convert integer paise to exact decimal rupees without floating point.
  public static String csvInr(long paise) {
    return java.math.BigDecimal.valueOf(paise, 2).setScale(2).toPlainString();
  }
  public static String csvCell(String value) {
    String v=value==null?"":value;
    // Spreadsheet formula injection: neutralize formula-like fields, including leading whitespace.
    if(v.stripLeading().matches("^[=+@\\-].*")) v="'"+v;
    return "\""+v.replace("\"","\"\"")+"\"";
  }
  @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public String exportCsv(UUID eventId, String type, String status, UUID giftId,
                          LocalDate from, LocalDate to) {
    return exportCsv(eventId,type,status,giftId,from,to,null);
  }
  @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public String exportCsv(UUID eventId, String type, String status, UUID giftId,
                          LocalDate from, LocalDate to, String search) {
    StringBuilder out=new StringBuilder("Date (UTC),Guest,Gift,Method,Payment amount (INR),Gift portion (INR),Wallet portion (INR),Status,Source,Record ID\n");
    int offset=0;
    while(true) {
      Report r=search==null||search.isBlank()?report(eventId,100,offset,type,status,giftId,from,to):report(eventId,100,offset,type,status,giftId,from,to,search);
      if(r.matchingCount()>10000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Export limited to 10000 records; narrow filters");
      for(Entry e:r.entries()) {
        String[] fields={e.createdAt()==null?"":e.createdAt().toInstant().toString(),e.guestName(),
          e.giftName()==null?"Celebration wallet":e.giftName(),e.method(),
          csvInr(e.amountPaise()),csvInr(e.giftPaise()),csvInr(e.walletPaise()),
          e.verificationStatus(),e.source(),e.id().toString()};
        out.append(java.util.Arrays.stream(fields).map(HostContributionReportService::csvCell)
          .collect(java.util.stream.Collectors.joining(","))).append('\n');
      }
      offset+=r.entries().size();
      if(offset>=r.matchingCount() || r.entries().isEmpty()) break;
    }
    return out.toString();
  }
  @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Report report(UUID eventId, int limit, int offset, String type, String status) {
    return report(eventId,limit,offset,type,status,null,null,null);
  }
  @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Report report(UUID eventId, int limit, int offset, String type, String status, UUID giftId) {
    return report(eventId,limit,offset,type,status,giftId,null,null);
  }
  @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Report report(UUID eventId, int limit, int offset, String type, String status,
                       UUID giftId, LocalDate from, LocalDate to) {
    return report(eventId,limit,offset,type,status,giftId,from,to,null);
  }
  @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Report report(UUID eventId, int limit, int offset, String type, String status,
                       UUID giftId, LocalDate from, LocalDate to, String search) {
    if(search!=null && search.length()>120)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Search must be at most 120 characters");
    if(eventId==null || limit<1 || limit>100 || offset<0 || offset>100000 ||
      !Set.of("ALL","GIFT","WALLET").contains(type) ||
      !Set.of("ALL","VERIFICATION_PENDING","VERIFIED","FAILED","CANCELLED").contains(status))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid report filters");
    if(from!=null && to!=null && from.isAfter(to))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid date range");
    UUID actor=actors.requireAuthenticatedUserId();
    Integer owns=jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,actor);
    if(owns==null || owns!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
    List<GiftOption> gifts=jdbc.query("""
      SELECT id,name,target_amount FROM gifts WHERE event_id=? ORDER BY name,id
      """,(rs,i)->new GiftOption(rs.getObject(1,UUID.class),rs.getString(2),
      rs.getBigDecimal(3).movePointRight(2).longValueExact()),eventId);
    if(giftId!=null && gifts.stream().noneMatch(g->g.id().equals(giftId)))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gift not found for event");
    // Keep the established payment verification rules in one relation shared by all queries.
    String rows = "WITH report_rows AS (" + REPORT_ROWS + ") ";
    StringBuilder filter = new StringBuilder(" WHERE 1=1");
    List<Object> args = new ArrayList<>(List.of(eventId,eventId,eventId));
    if(search!=null && !search.isBlank()) {
      filter.append(" AND strpos(lower(concat_ws(' ',guest_name,gift_name,method)),lower(?))>0");
      args.add(search.trim());
    }
    if(giftId!=null){filter.append(" AND gift_id=?");args.add(giftId);}
    if(!"ALL".equals(status)){filter.append(" AND verification_status=?");args.add(status);}
    if("GIFT".equals(type))filter.append(" AND (gift_paise>0 OR (verification_status='VERIFICATION_PENDING' AND gift_id IS NOT NULL))");
    if("WALLET".equals(type))filter.append(" AND (wallet_paise>0 OR (verification_status='VERIFICATION_PENDING' AND gift_id IS NULL))");
    if(from!=null){filter.append(" AND created_at>=?");args.add(from.atStartOfDay().atOffset(java.time.ZoneOffset.UTC));}
    if(to!=null){filter.append(" AND created_at<?");args.add(to.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC));}
    String gp="WALLET".equals(type)?"0":"gift_paise";
    String wp="GIFT".equals(type)?"0":"wallet_paise";
    String included="ALL".equals(type)?"amount_paise":"GIFT".equals(type)?gp:wp;
    String pendingIncluded="ALL".equals(type)?included:
      "CASE WHEN source='RAZORPAY' AND ("+included+")=0 AND gift_id IS "+("GIFT".equals(type)?"NOT NULL":"NULL")+" THEN amount_paise ELSE "+included+" END";
    String aggregate="SELECT count(*) AS matching_count, "+
      "COALESCE(SUM("+pendingIncluded+") FILTER (WHERE verification_status='VERIFICATION_PENDING'),0) AS pending, "+
      "COALESCE(SUM("+included+") FILTER (WHERE verification_status='VERIFIED'),0) AS verified, "+
      "COALESCE(SUM("+gp+") FILTER (WHERE verification_status='VERIFIED'),0) AS gift, "+
      "COALESCE(SUM("+wp+") FILTER (WHERE verification_status='VERIFIED'),0) AS wallet, "+
      "COALESCE(SUM("+gp+") FILTER (WHERE verification_status='VERIFICATION_PENDING' AND source IN ('MANUAL','GROUP')),0) AS reported_gift, "+
      "COALESCE(SUM("+wp+") FILTER (WHERE verification_status='VERIFICATION_PENDING' AND source IN ('MANUAL','GROUP')),0) AS reported_wallet FROM report_rows";
    long[] totals=jdbc.queryForObject(rows+aggregate+filter,(rs,i)->new long[]{rs.getLong("matching_count"),rs.getLong("pending"),rs.getLong("verified"),rs.getLong("gift"),rs.getLong("wallet"),rs.getLong("reported_gift"),rs.getLong("reported_wallet")},args.toArray());
    List<Object> pageArgs=new ArrayList<>(args);pageArgs.add(limit);pageArgs.add(offset);
    List<Entry> entries=jdbc.query(rows+"SELECT * FROM report_rows"+filter+" ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?",
      (rs,i)->new Entry(rs.getObject("id",UUID.class),rs.getString("source"),rs.getString("guest_name"),
        rs.getObject("gift_id",UUID.class),rs.getString("gift_name"),rs.getString("contribution_type"),rs.getString("method"),
        rs.getLong("amount_paise"),rs.getLong("gift_paise"),rs.getLong("wallet_paise"),rs.getString("verification_status"),
        rs.getObject("created_at",OffsetDateTime.class)),pageArgs.toArray());
    long target=giftId==null?0:gifts.stream().filter(g->g.id().equals(giftId)).findFirst().orElseThrow().targetPaise();
    // Selected gift progress remains independent of report filters and pagination.
    long[] selected=giftId==null?new long[]{0,0}:jdbc.queryForObject(rows+
      "SELECT COALESCE(SUM(gift_paise) FILTER (WHERE verification_status='VERIFIED'),0), "+
      "COALESCE(SUM(gift_paise) FILTER (WHERE verification_status='VERIFICATION_PENDING' AND source IN ('MANUAL','GROUP')),0) FROM report_rows WHERE gift_id=?",
      (rs,i)->new long[]{rs.getLong(1),rs.getLong(2)},eventId,eventId,eventId,giftId);
    return new Report(eventId,totals[1],totals[2],totals[3],totals[4],entries,gifts,giftId,target,
      totals[0],totals[5],totals[6],selected[0],selected[1]);
  }
  private static final String REPORT_ROWS = """
        SELECT c.id,'RAZORPAY'::text AS source,c.guest_name,c.gift_id,g.name AS gift_name,
               CASE WHEN c.gift_id IS NULL THEN 'WALLET' ELSE 'GIFT' END AS contribution_type,
               'RAZORPAY'::text AS method,c.amount_paise,
               CASE WHEN po.status='CAPTURED' AND EXISTS (
                 SELECT 1 FROM payment_transactions pt WHERE pt.payment_order_id=po.id
                   AND pt.status='CAPTURED' AND pt.verified_at IS NOT NULL)
                 THEN COALESCE((SELECT SUM(a.amount_paise) FROM contribution_allocations a
                   WHERE a.contribution_id=c.id AND a.allocation_type='GIFT'),0)
                 ELSE 0 END AS gift_paise,
               CASE WHEN po.status='CAPTURED' AND EXISTS (
                 SELECT 1 FROM payment_transactions pt WHERE pt.payment_order_id=po.id
                   AND pt.status='CAPTURED' AND pt.verified_at IS NOT NULL)
                 THEN COALESCE((SELECT SUM(a.amount_paise) FROM contribution_allocations a
                   WHERE a.contribution_id=c.id AND a.allocation_type='EVENT_WALLET'),0)
                 ELSE 0 END AS wallet_paise,
               CASE WHEN po.status='CAPTURED' AND EXISTS (
                 SELECT 1 FROM payment_transactions pt WHERE pt.payment_order_id=po.id
                   AND pt.status='CAPTURED' AND pt.verified_at IS NOT NULL
                   AND EXISTS (SELECT 1 FROM contribution_allocation_postings p WHERE p.contribution_id=c.id))
                 THEN 'VERIFIED'
                 WHEN po.status='FAILED' THEN 'FAILED'
                 WHEN po.status='CANCELLED' THEN 'CANCELLED'
                 ELSE 'VERIFICATION_PENDING' END AS verification_status,
               c.created_at
        FROM contributions c JOIN payment_orders po ON po.id=c.payment_order_id
        LEFT JOIN gifts g ON g.id=c.gift_id AND g.event_id=c.event_id
        WHERE c.event_id=? AND po.provider='RAZORPAY'
        UNION ALL
        SELECT cl.id,'MANUAL'::text,cl.guest_name,cl.gift_id,g.name,
               CASE WHEN cl.gift_id IS NULL THEN 'WALLET' ELSE 'GIFT' END,
               cl.method,cl.amount_paise,COALESCE(a.gift_paise,0),
               COALESCE(a.wallet_paise,cl.amount_paise),
               CASE WHEN cl.status='REJECTED' THEN 'FAILED'
                    WHEN cl.status='EXPIRED' THEN 'CANCELLED'
                    ELSE 'VERIFICATION_PENDING' END,
               cl.created_at
        FROM guest_direct_transfer_claims cl
        LEFT JOIN guest_manual_display_allocations a ON a.claim_id=cl.id
        LEFT JOIN gifts g ON g.id=cl.gift_id AND g.event_id=cl.event_id
        WHERE cl.event_id=?
        UNION ALL
        SELECT r.id,'GROUP'::text,concat(g.name,' · ',r.guest_name),r.gift_id_snapshot,gift.name,
               CASE WHEN r.gift_id_snapshot IS NULL THEN 'WALLET' ELSE 'GIFT' END,
               r.method,r.amount_paise,
               CASE WHEN r.gift_id_snapshot IS NOT NULL THEN r.amount_paise ELSE 0 END,
               CASE WHEN r.gift_id_snapshot IS NULL THEN r.amount_paise ELSE 0 END,
               CASE WHEN r.state='REJECTED' THEN 'FAILED' ELSE 'VERIFICATION_PENDING' END,
               r.created_at
        FROM group_collection_transactions r JOIN gift_groups g ON g.id=r.group_id
        LEFT JOIN gifts gift ON gift.id=r.gift_id_snapshot AND gift.event_id=g.event_id
        WHERE g.event_id=? AND g.collection_published_at IS NOT NULL
      """;
}
