package com.giftplatform.payment.control;

import com.giftplatform.security.ActorContext;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Administrator-managed platform revenue share, separate from provider processing charges.
 * Configuration only while Route/VPA transfer execution is disabled.
 */
@Service
public class RoutingCommissionService {
    private final JdbcTemplate db;
    private final PaymentControlService controls;
    private final ActorContext actors;
    public RoutingCommissionService(JdbcTemplate db,PaymentControlService controls,ActorContext actors){this.db=db;this.controls=controls;this.actors=actors;}
    public record Settings(long revision,int rateBps,boolean routingActive,String appliesTo) {}
    public record Update(Long revision,Integer rateBps,String reason) {}
    public Settings current(){
        controls.admin();
        return db.queryForObject("SELECT revision,rate_bps FROM routing_commission_settings WHERE id=1",(rs,n)->new Settings(rs.getLong(1),rs.getInt(2),false,"ROUTED_HOST_PAYMENTS_ONLY"));
    }
    @Transactional public Settings update(Update input){
        controls.admin();
        if(input==null||input.revision()==null||input.rateBps()==null||input.rateBps()<0||input.rateBps()>10000||input.reason()==null||input.reason().isBlank()||input.reason().length()>500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supply a revision, percentage from 0% to 100%, and a change reason");
        var old=db.queryForMap("SELECT revision,rate_bps FROM routing_commission_settings WHERE id=1 FOR UPDATE");
        long revision=((Number)old.get("revision")).longValue();
        if(revision!=input.revision())throw new ResponseStatusException(HttpStatus.CONFLICT,"Routing commission changed; reload before saving");
        long next=Math.addExact(revision,1);UUID admin=actors.requireAuthenticatedUserId();
        db.update("UPDATE routing_commission_settings SET revision=?,rate_bps=?,changed_by=?,changed_at=now() WHERE id=1",next,input.rateBps(),admin);
        db.update("INSERT INTO routing_commission_history(revision,actor_id,before_rate_bps,after_rate_bps,reason) VALUES(?,?,?,?,?)",next,admin,old.get("rate_bps"),input.rateBps(),input.reason().strip());
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action) VALUES(?,?,?)",UUID.randomUUID(),admin,"ROUTING_COMMISSION_UPDATED");
        return current();
    }
    public List<Map<String,Object>> history(int limit,long beforeRevision){
        controls.admin();
        if(limit<1||limit>100||beforeRevision<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid history page");
        return db.queryForList("SELECT * FROM routing_commission_history WHERE revision<? ORDER BY revision DESC LIMIT ?",beforeRevision,limit);
    }
}
