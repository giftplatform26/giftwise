package com.giftplatform.monetization;

import com.giftplatform.security.ActorContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MonetizationService {
    private final JdbcTemplate db;
    private final ActorContext actors;
    public MonetizationService(JdbcTemplate db, ActorContext actors) { this.db=db; this.actors=actors; }
    public record Stream(String code, boolean enabled, String feeMode, long fixedPaise,
                         int rateBps, long capPaise, String feeBearer, boolean collectionReady) {}
    public record Settings(long revision, boolean enabled, List<Stream> streams) {}
    public record Update(long revision, boolean enabled, List<Stream> streams, String reason) {}
    public record Quote(long revision, String stream, long contributionPaise, long platformFeePaise,
                        long totalPaise, long hostReceivesPaise, String feeBearer, String currency) {}
    private static final Set<String> CODES=Set.of("CONTRIBUTION","WALLET","AFFILIATE","PREMIUM_TEMPLATE",
            "HOST_PLAN","VENDOR_LISTING","SPONSORED_GIFT","GROUP_GIFT");
    private static final Set<String> READY=CODES;

    @Transactional
    public Settings current() {
        // Lock makes the master switch and all stream rows one consistent configuration.
        var master=db.queryForMap("SELECT revision,enabled FROM platform_monetization_settings WHERE id=1 FOR SHARE");
        var streams=db.query("SELECT * FROM platform_monetization_streams ORDER BY code",(rs,n)->new Stream(
                rs.getString("code"),rs.getBoolean("enabled"),rs.getString("fee_mode"),rs.getLong("fixed_paise"),
                rs.getInt("rate_bps"),rs.getLong("cap_paise"),rs.getString("fee_bearer"),READY.contains(rs.getString("code"))));
        return new Settings(((Number)master.get("revision")).longValue(),(Boolean)master.get("enabled"),streams);
    }
    public void requireAdmin() {
        if(!actors.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform admin only");
    }
    @Transactional
    public Settings update(Update input) {
        requireAdmin();
        UUID who=actors.requireAuthenticatedUserId();
        if(input==null || input.reason()==null || input.reason().isBlank() || input.reason().length()>500 ||
                input.streams()==null || input.streams().size()!=CODES.size()) throw bad("Supply all streams and a change reason (1–500 characters)");
        Set<String> seen=new HashSet<>();
        for(Stream s:input.streams()) {
            if(s==null || s.code()==null || !CODES.contains(s.code()) || !seen.add(s.code()) ||
                    !Set.of("FIXED","PERCENTAGE").contains(s.feeMode()==null?"":s.feeMode()) ||
                    !Set.of("GUEST","HOST").contains(s.feeBearer()==null?"":s.feeBearer()) ||
                    s.fixedPaise()<0 || s.fixedPaise()>100_000_000 || s.rateBps()<0 || s.rateBps()>10000 ||
                    s.capPaise()<0 || s.capPaise()>100_000_000) throw bad("Invalid monetization stream");
            if(Set.of("HOST_PLAN","PREMIUM_TEMPLATE","VENDOR_LISTING","SPONSORED_GIFT").contains(s.code()) && !"FIXED".equals(s.feeMode()))
                throw bad("Purchases require a fixed price; percentage pricing is available for contributions, affiliate commissions and group coordination");
        }
        Long version=db.queryForObject("SELECT revision FROM platform_monetization_settings WHERE id=1 FOR UPDATE",Long.class);
        if(version==null || version!=input.revision()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Settings changed; reload before saving");
        String before=snapshot();
        for(Stream s:input.streams()) db.update("UPDATE platform_monetization_streams SET enabled=?,fee_mode=?,fixed_paise=?,rate_bps=?,cap_paise=?,fee_bearer=? WHERE code=?",
                s.enabled(),s.feeMode(),s.fixedPaise(),s.rateBps(),s.capPaise(),s.feeBearer(),s.code());
        long next=Math.addExact(version,1);
        db.update("UPDATE platform_monetization_settings SET enabled=?,revision=?,changed_by=?,changed_at=now() WHERE id=1",input.enabled(),next,who);
        db.update("INSERT INTO platform_monetization_history(revision,actor_id,reason,before_config,after_config) VALUES(?,?,?,?::jsonb,?::jsonb)",
                next,who,input.reason().strip(),before,snapshot());
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action) VALUES(?,?,?)",UUID.randomUUID(),who,"MONETIZATION_UPDATED");
        return current();
    }
    private String snapshot() {
        return db.queryForObject("SELECT jsonb_build_object('enabled',enabled,'revision',revision,'streams',(SELECT jsonb_agg(to_jsonb(s) ORDER BY code) FROM platform_monetization_streams s))::text FROM platform_monetization_settings WHERE id=1",String.class);
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> history(int limit, long beforeRevision) {
        requireAdmin();
        if(limit<1 || limit>100 || beforeRevision<0) throw bad("Invalid history page");
        return db.queryForList("SELECT revision,actor_id,reason,before_config::text,after_config::text,created_at FROM platform_monetization_history WHERE revision<? ORDER BY revision DESC LIMIT ?",beforeRevision,limit);
    }
    @Transactional
    public Quote quote(String stream,long amount) {
        return calculate(current(),stream,amount);
    }
    @Transactional
    public boolean enabled(String code) {
        Settings settings=current();
        return settings.enabled() && settings.streams().stream().anyMatch(s->s.code().equals(code)&&s.enabled());
    }
    public static Quote calculate(Settings config,String code,long amount) {
        if(amount<=0 || amount>100_000_000L) throw bad("Contribution must be between 1 paise and ₹10,00,000");
        Stream s=config.streams().stream().filter(x->x.code().equals(code)).findFirst().orElseThrow(()->bad("Unknown revenue stream"));
        long fee=0;
        if(config.enabled() && s.enabled() && READY.contains(code)) {
            fee="FIXED".equals(s.feeMode())?s.fixedPaise():BigDecimal.valueOf(amount)
                    .multiply(BigDecimal.valueOf(s.rateBps())).divide(BigDecimal.valueOf(10000),0,RoundingMode.HALF_UP).longValueExact();
            if(s.capPaise()>0) fee=Math.min(fee,s.capPaise());
        }
        boolean guest="GUEST".equals(s.feeBearer());
        if(!guest && fee>=amount) throw bad("Contribution must exceed the platform fee");
        return new Quote(config.revision(),code,amount,fee,guest?Math.addExact(amount,fee):amount,
                guest?amount:amount-fee,s.feeBearer(),"INR");
    }
    private static ResponseStatusException bad(String message) {return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
}
