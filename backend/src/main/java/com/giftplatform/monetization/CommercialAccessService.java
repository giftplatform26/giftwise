package com.giftplatform.monetization;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommercialAccessService {
    private final JdbcTemplate db;
    private final MonetizationService pricing;
    public CommercialAccessService(JdbcTemplate db,MonetizationService pricing) {this.db=db;this.pricing=pricing;}
    public boolean has(String kind,UUID buyer,String stream,UUID resource) {
        return Boolean.TRUE.equals(db.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM commercial_entitlements WHERE buyer_kind=? AND buyer_id=?
            AND stream=? AND resource_id=? AND starts_at<=now() AND (expires_at IS NULL OR expires_at>now()))
            """,Boolean.class,kind,buyer,stream,resource));
    }
    public boolean premium(UUID host) {return has("HOST",host,"HOST_PLAN",host);}
    public boolean template(UUID host,UUID asset) {
        return premium(host) || has("HOST",host,"PREMIUM_TEMPLATE",asset);
    }
    @Transactional
    public void requireEventCapacity(UUID host,UUID event) {
        if(!pricing.enabled("HOST_PLAN"))return;
        db.queryForObject("SELECT id FROM users WHERE id=? FOR UPDATE",UUID.class,host);
        if(premium(host))return;
        Integer maximum=db.queryForObject("SELECT free_active_events FROM commercial_policy WHERE id=1",Integer.class);
        Integer active=db.queryForObject("SELECT count(*) FROM events WHERE owner_id=? AND status='PUBLISHED' AND id<>?",Integer.class,host,event);
        if(active!=null&&maximum!=null&&active>=maximum)throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"Your free active-event limit is reached. Upgrade your host plan or complete an active event.");
    }
    public void requireGroupFee(UUID group) {
        if(!pricing.enabled("GROUP_GIFT"))return;
        var row=db.queryForMap("SELECT admin_id,collection_published_at FROM gift_groups WHERE id=?",group);
        if(row.get("collection_published_at")!=null)return;
        if(!has("GROUP_ADMIN",(UUID)row.get("admin_id"),"GROUP_GIFT",group))
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"Pay the group coordination fee before publishing");
    }
}
