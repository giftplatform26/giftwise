package com.giftplatform.payment.control;

import com.giftplatform.security.ActorContext;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentControlService {
    public static final UUID PLATFORM=new UUID(0,0);
    public static final List<String> CHANNELS=List.of("UPI","BANK","RAZORPAY","ROUTING");
    private final JdbcTemplate db;private final ActorContext actors;
    public PaymentControlService(JdbcTemplate db,ActorContext actors){this.db=db;this.actors=actors;}
    public record Control(String channel,Boolean configured,boolean platformEnabled,boolean hostEnabled,boolean eventEnabled,boolean effectiveEnabled) {}
    public record View(long revision,String scope,UUID scopeId,List<Control> controls) {}
    public record Change(long revision,Boolean enabled,String reason) {}
    public void admin(){if(!actors.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform administrator required");}
    public Map<String,Boolean> event(UUID eventId){
        var rows=db.queryForList("SELECT owner_id FROM events WHERE id=?",eventId);
        if(rows.size()!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
        return effective((UUID)rows.getFirst().get("owner_id"),eventId);
    }
    public Map<String,Boolean> host(UUID hostId){return effective(hostId,null);}
    private Map<String,Boolean> effective(UUID host,UUID event){
        var rows=db.queryForList("SELECT scope,channel,enabled FROM payment_channel_controls WHERE (scope='PLATFORM' AND scope_id=?) OR (scope='HOST' AND scope_id=?) OR (scope='EVENT' AND scope_id=?)",PLATFORM,host,event);
        Map<String,Boolean> result=new LinkedHashMap<>();
        for(String code:CHANNELS){boolean value=true,root=false;for(var row:rows)if(code.equals(row.get("channel"))){value&=(Boolean)row.get("enabled");if("PLATFORM".equals(row.get("scope")))root=true;}result.put(code,root&&value);}
        result.put("ROUTING",result.get("ROUTING")&&result.get("RAZORPAY"));
        return Map.copyOf(result);
    }
    public void require(UUID event,String channel){if(!Boolean.TRUE.equals(event(event).get(channel)))throw new ResponseStatusException(HttpStatus.CONFLICT,channel+" payments are disabled for this event");}
    public void requireHost(UUID host,String channel){if(!Boolean.TRUE.equals(host(host).get(channel)))throw new ResponseStatusException(HttpStatus.CONFLICT,channel+" payments are disabled for this host");}
    private UUID validate(String scope,UUID id){
        if("PLATFORM".equals(scope)){if(id!=null&&!PLATFORM.equals(id))throw bad("Invalid platform scope");return PLATFORM;}
        if(id==null)throw bad("Choose a host or event");
        if("HOST".equals(scope)){if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users WHERE id=?)",Boolean.class,id)))throw missing();return id;}
        if("EVENT".equals(scope)){if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM events WHERE id=?)",Boolean.class,id)))throw missing();return id;}
        throw bad("Invalid payment scope");
    }
    @Transactional(readOnly=true) public View read(String scope,UUID id){
        admin();id=validate(scope,id);long revision=db.queryForObject("SELECT revision FROM payment_control_revision WHERE id=1",Long.class);
        UUID host="HOST".equals(scope)?id:"EVENT".equals(scope)?db.queryForObject("SELECT owner_id FROM events WHERE id=?",UUID.class,id):null;
        UUID event="EVENT".equals(scope)?id:null;
        var root=effective(null,null);var inherited=effective(host,null);var resolved=effective(host,event);
        var configured=db.queryForList("SELECT channel,enabled FROM payment_channel_controls WHERE scope=? AND scope_id=?",scope,id);
        List<Control> controls=new ArrayList<>();
        for(String code:CHANNELS){Boolean value=null;for(var row:configured)if(code.equals(row.get("channel")))value=(Boolean)row.get("enabled");controls.add(new Control(code,value,root.get(code),inherited.get(code),resolved.get(code),resolved.get(code)));}
        return new View(revision,scope,id,List.copyOf(controls));
    }
    @Transactional public View change(String scope,UUID id,String channel,Change input){
        admin();id=validate(scope,id);
        if(!CHANNELS.contains(channel)||input==null||input.reason()==null||input.reason().isBlank()||input.reason().length()>500||("PLATFORM".equals(scope)&&input.enabled()==null))throw bad("Choose a channel, setting and change reason");
        if("ROUTING".equals(channel)&&Boolean.TRUE.equals(input.enabled()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Route/VPA activation is disabled. Normal Razorpay checkout remains available through its own controls.");
        Long revision=db.queryForObject("SELECT revision FROM payment_control_revision WHERE id=1 FOR UPDATE",Long.class);
        if(revision==null||revision!=input.revision())throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment controls changed; reload before saving");
        var prior=db.queryForList("SELECT enabled FROM payment_channel_controls WHERE scope=? AND scope_id=? AND channel=?",scope,id,channel);
        Boolean before=prior.isEmpty()?null:(Boolean)prior.getFirst().get("enabled");UUID admin=actors.requireAuthenticatedUserId();long next=revision+1;
        if(input.enabled()==null)db.update("DELETE FROM payment_channel_controls WHERE scope=? AND scope_id=? AND channel=?",scope,id,channel);
        else db.update("INSERT INTO payment_channel_controls(scope,scope_id,channel,enabled,changed_by) VALUES(?,?,?,?,?) ON CONFLICT(scope,scope_id,channel) DO UPDATE SET enabled=EXCLUDED.enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",scope,id,channel,input.enabled(),admin);
        db.update("UPDATE payment_control_revision SET revision=? WHERE id=1",next);
        db.update("INSERT INTO payment_control_history(revision,actor_id,scope,scope_id,channel,reason,before_enabled,after_enabled) VALUES(?,?,?,?,?,?,?,?)",next,admin,scope,id,channel,input.reason().strip(),before,input.enabled());
        return read(scope,id);
    }
    public List<Map<String,Object>> history(int limit,long before){admin();if(limit<1||limit>100||before<0)throw bad("Invalid history page");return db.queryForList("SELECT * FROM payment_control_history WHERE revision<? ORDER BY revision DESC LIMIT ?",before,limit);}
    private static ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private static ResponseStatusException missing(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment scope not found");}
}
