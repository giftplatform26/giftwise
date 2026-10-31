package com.giftplatform.monetization;

import com.giftplatform.security.ActorContext;
import java.net.URI;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommercialSetupService {
    private final JdbcTemplate db;private final ActorContext actor;private final MonetizationService pricing;
    public CommercialSetupService(JdbcTemplate db,ActorContext actor,MonetizationService pricing){this.db=db;this.actor=actor;this.pricing=pricing;}
    public record Policy(long revision,int freeActiveEvents,int hostPlanDays,int vendorDays,int sponsorDays,String reason) {}
    public record Placement(String stream,String title,String description,String url,String category,String occasion) {}
    public record Moderation(String state,String reason) {}
    public record Partner(UUID id,long revision,String name,String productDomain,boolean enabled,String parameterName,String parameterValue,String clickParameter,String redirectTemplate,String reason) {}
    public record Commission(UUID id,long revision,UUID partnerId,UUID clickId,String externalReference,long amountPaise,String state,String reason) {}
    public Map<String,Object> policy(){return db.queryForMap("SELECT * FROM commercial_policy WHERE id=1");}
    @Transactional public Map<String,Object> policy(Policy input){
        pricing.requireAdmin();
        if(input==null||input.freeActiveEvents()<0||input.freeActiveEvents()>1000||!days(input.hostPlanDays())||!days(input.vendorDays())||!days(input.sponsorDays()))throw bad("Invalid commercial policy");
        reason(input.reason());
        var old=db.queryForMap("SELECT * FROM commercial_policy WHERE id=1 FOR UPDATE");
        if(((Number)old.get("revision")).longValue()!=input.revision())throw conflict();
        String before=snapshot("commercial_policy",1);
        db.update("UPDATE commercial_policy SET revision=revision+1,free_active_events=?,host_plan_days=?,vendor_days=?,sponsor_days=? WHERE id=1",input.freeActiveEvents(),input.hostPlanDays(),input.vendorDays(),input.sponsorDays());
        audit("POLICY","1",input.reason(),before,snapshot("commercial_policy",1));return policy();
    }
    private static boolean days(int n){return n>=1&&n<=3660;}
    public List<Map<String,Object>> ownPlacements(){return db.queryForList("SELECT * FROM commercial_placements WHERE owner_id=? ORDER BY created_at DESC LIMIT 200",actor.requireAuthenticatedUserId());}
    @Transactional public Map<String,Object> createPlacement(Placement input){
        UUID owner=actor.requireAuthenticatedUserId();
        if(input==null||!Set.of("VENDOR_LISTING","SPONSORED_GIFT").contains(input.stream()==null?"":input.stream()))throw bad("Invalid placement type");
        String title=text(input.title(),160),description=text(input.description(),1000),category=text(input.category(),80),occasion=text(input.occasion(),50);
        https(input.url());UUID id=UUID.randomUUID();
        db.update("INSERT INTO commercial_placements(id,owner_id,stream,title,description,url,category,occasion) VALUES(?,?,?,?,?,?,?,?)",id,owner,input.stream(),title,description,input.url(),category,occasion);
        return db.queryForMap("SELECT * FROM commercial_placements WHERE id=?",id);
    }
    @Transactional public Map<String,Object> moderate(UUID id,Moderation input){
        pricing.requireAdmin();if(input==null||!Set.of("APPROVED","REJECTED").contains(input.state()==null?"":input.state()))throw bad("Select approval or rejection");reason(input.reason());
        var rows=db.queryForList("SELECT id FROM commercial_placements WHERE id=? FOR UPDATE",id);if(rows.isEmpty())throw notFound();
        String before=snapshot("commercial_placements",id);
        db.update("UPDATE commercial_placements SET moderation=? WHERE id=?",input.state(),id);
        audit("PLACEMENT",id.toString(),input.reason(),before,snapshot("commercial_placements",id));return db.queryForMap("SELECT * FROM commercial_placements WHERE id=?",id);
    }
    public List<Map<String,Object>> placements(){pricing.requireAdmin();return db.queryForList("SELECT * FROM commercial_placements ORDER BY created_at DESC LIMIT 200");}
    public List<Map<String,Object>> partners(){pricing.requireAdmin();return db.queryForList("SELECT * FROM affiliate_partners ORDER BY name");}
    @Transactional public Map<String,Object> partner(Partner input){
        pricing.requireAdmin();if(input==null)throw bad("Partner setup required");reason(input.reason());
        String name=text(input.name(),100),domain=text(input.productDomain(),253).toLowerCase(Locale.ROOT);
        if(!domain.matches("[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?\\.[a-z]{2,63}")||domain.contains(".."))throw bad("Use the retailer product domain without a scheme or path");
        if(input.parameterName()==null||!input.parameterName().matches("[A-Za-z][A-Za-z0-9_]{0,59}"))throw bad("Invalid affiliate parameter name");
        text(input.parameterValue(),500);
        if(input.clickParameter()!=null&&!input.clickParameter().isBlank()&&(!input.clickParameter().matches("[A-Za-z][A-Za-z0-9_]{0,59}")||input.clickParameter().equals(input.parameterName())))throw bad("Invalid or duplicate click parameter");
        if(input.redirectTemplate()!=null&&!input.redirectTemplate().isBlank()){
            if(!input.redirectTemplate().contains("{url}"))throw bad("Redirect template must include {url}");
            https(input.redirectTemplate().replace("{url}","https%3A%2F%2Fexample.com").replace("{click}",UUID.randomUUID().toString()));
            var redirect=URI.create(input.redirectTemplate().replace("{url}","x").replace("{click}","x"));
            if(input.redirectTemplate().indexOf('{')<input.redirectTemplate().indexOf('/',8))throw bad("Redirect placeholders must be in the path or query");
        }
        UUID id=input.id()==null?UUID.randomUUID():input.id();String before=null;
        if(input.id()!=null){var rows=db.queryForList("SELECT revision FROM affiliate_partners WHERE id=? FOR UPDATE",id);if(rows.size()!=1)throw notFound();if(((Number)rows.getFirst().get("revision")).longValue()!=input.revision())throw conflict();before=snapshot("affiliate_partners",id);}
        db.update("""
            INSERT INTO affiliate_partners(id,name,product_domain,enabled,parameter_name,parameter_value,click_parameter,redirect_template)
            VALUES(?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET name=EXCLUDED.name,product_domain=EXCLUDED.product_domain,
            enabled=EXCLUDED.enabled,parameter_name=EXCLUDED.parameter_name,parameter_value=EXCLUDED.parameter_value,
            click_parameter=EXCLUDED.click_parameter,redirect_template=EXCLUDED.redirect_template,revision=affiliate_partners.revision+1
            """,id,name,domain,input.enabled(),input.parameterName(),input.parameterValue(),clean(input.clickParameter()),clean(input.redirectTemplate()));
        audit("AFFILIATE_PARTNER",id.toString(),input.reason(),before,snapshot("affiliate_partners",id));return db.queryForMap("SELECT * FROM affiliate_partners WHERE id=?",id);
    }
    @Transactional public Map<String,Object> commission(Commission input){
        pricing.requireAdmin();if(input==null||input.partnerId()==null||input.amountPaise()<=0||input.amountPaise()>100_000_000L||!Set.of("CONFIRMED","PAID","REVERSED").contains(input.state()==null?"":input.state()))throw bad("Invalid commission");
        reason(input.reason());text(input.externalReference(),160);
        if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM affiliate_partners WHERE id=?)",Boolean.class,input.partnerId())))throw notFound();
        if(input.clickId()!=null&&!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM affiliate_clicks WHERE id=? AND partner_id=?)",Boolean.class,input.clickId(),input.partnerId())))throw bad("Click does not belong to this partner");
        UUID id=input.id()==null?UUID.randomUUID():input.id();String before=null;
        if(input.id()!=null){var row=db.queryForMap("SELECT * FROM affiliate_commissions WHERE id=? FOR UPDATE",id);
            if(((Number)row.get("revision")).longValue()!=input.revision())throw conflict();
            if(!Objects.equals(row.get("partner_id"),input.partnerId())||!Objects.equals(row.get("click_id"),input.clickId())||!Objects.equals(row.get("external_reference"),input.externalReference())||((Number)row.get("amount_paise")).longValue()!=input.amountPaise())throw bad("Statement identity and amount cannot change; reverse the entry and record a correction");
            if("REVERSED".equals(row.get("state"))||("PAID".equals(row.get("state"))&&"CONFIRMED".equals(input.state())))throw bad("Invalid commission state transition");
            before=snapshot("affiliate_commissions",id);
            db.update("UPDATE affiliate_commissions SET state=?,revision=revision+1,changed_at=now() WHERE id=?",input.state(),id);
        }else db.update("INSERT INTO affiliate_commissions(id,partner_id,click_id,external_reference,amount_paise,state) VALUES(?,?,?,?,?,?)",id,input.partnerId(),input.clickId(),input.externalReference(),input.amountPaise(),input.state());
        audit("AFFILIATE_COMMISSION",id.toString(),input.reason(),before,snapshot("affiliate_commissions",id));return db.queryForMap("SELECT * FROM affiliate_commissions WHERE id=?",id);
    }
    public List<Map<String,Object>> commissions(){pricing.requireAdmin();return db.queryForList("SELECT * FROM affiliate_commissions ORDER BY changed_at DESC LIMIT 200");}
    public List<Map<String,Object>> history(){pricing.requireAdmin();return db.queryForList("SELECT id,actor_id,resource_type,resource_id,reason,before_config::text,after_config::text,created_at FROM commercial_setup_history ORDER BY created_at DESC LIMIT 200");}
    public Map<String,Object> revenue(){pricing.requireAdmin();return db.queryForMap("""
        SELECT (SELECT COALESCE(SUM(amount_paise),0) FROM wallet_ledger_entries WHERE account_code='PLATFORM_REVENUE') AS contribution_fees_paise,
        (SELECT COALESCE(SUM(amount_paise),0) FROM commercial_revenue) AS verified_purchases_paise,
        (SELECT COALESCE(SUM(amount_paise),0) FROM affiliate_commissions WHERE state='PAID') AS partner_reported_paid_paise,
        (SELECT count(*) FROM affiliate_clicks) AS affiliate_clicks,
        (SELECT count(*) FROM commercial_placement_clicks) AS placement_clicks
        """);}
    private String snapshot(String table,Object id){return db.queryForObject("SELECT to_jsonb(t)::text FROM "+table+" t WHERE id=?",String.class,id);}
    private void audit(String type,String id,String reason,String before,String after){db.update("INSERT INTO commercial_setup_history(id,actor_id,resource_type,resource_id,reason,before_config,after_config) VALUES(?,?,?,?,?,?::jsonb,?::jsonb)",UUID.randomUUID(),actor.requireAuthenticatedUserId(),type,id,reason.strip(),before,after);}
    private static void reason(String s){text(s,500);}
    private static String text(String s,int max){if(s==null||s.isBlank()||s.length()>max||s.chars().anyMatch(Character::isISOControl))throw bad("Required field is missing or too long");return s.strip();}
    private static String clean(String s){return s==null||s.isBlank()?null:s.strip();}
    public static URI https(String s){try{if(s==null||s.length()>2048)throw new IllegalArgumentException();URI uri=URI.create(s);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||!uri.getHost().contains(".")||uri.getUserInfo()!=null||uri.getPort()!=-1)throw new IllegalArgumentException();return uri;}catch(IllegalArgumentException e){throw bad("Use a valid HTTPS URL without embedded credentials or a custom port");}}
    private static ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private static ResponseStatusException conflict(){return new ResponseStatusException(HttpStatus.CONFLICT,"Setup changed; reload before saving");}
    private static ResponseStatusException notFound(){return new ResponseStatusException(HttpStatus.NOT_FOUND);}
}
