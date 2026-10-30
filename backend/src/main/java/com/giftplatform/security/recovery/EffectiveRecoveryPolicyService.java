
package com.giftplatform.security.recovery;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Platform minimums cannot be weakened by host/event overrides. */
@Service
public class EffectiveRecoveryPolicyService {
    private final JdbcTemplate db;
    public record Policy(boolean requireEmail, boolean requireMobile, boolean verifyEmail,
                         boolean verifyMobile, boolean allowGroupPin) {
        public Policy merge(Policy child) {
            return new Policy(requireEmail||child.requireEmail,requireMobile||child.requireMobile,
                verifyEmail||child.verifyEmail,verifyMobile||child.verifyMobile,
                allowGroupPin&&child.allowGroupPin);
        }
    }
    public EffectiveRecoveryPolicyService(JdbcTemplate db){this.db=db;}
    public Policy effective(UUID hostId, UUID eventId) {
        Policy result=read("PLATFORM",new UUID(0,0)).orElseThrow(() -> new IllegalStateException("Platform account policy missing"));
        if(eventId!=null) {
            List<UUID> owners=db.query("SELECT owner_id FROM events WHERE id=?",(rs,n)->(UUID)rs.getObject(1),eventId);
            if(owners.size()!=1) throw new IllegalArgumentException("Unknown event policy target");
            if(hostId!=null&&!hostId.equals(owners.get(0))) throw new IllegalArgumentException("Event does not belong to host");
            hostId=owners.get(0);
        }
        if(hostId!=null)result=result.merge(read("HOST",hostId).orElse(result));
        if(eventId!=null)result=result.merge(read("EVENT",eventId).orElse(result));
        return result;
    }
    private Optional<Policy> read(String scope,UUID id) {
        List<Policy> rows=db.query("SELECT require_email,require_mobile,verify_email,verify_mobile,allow_group_pin FROM account_registration_policies WHERE scope=? AND scope_id=?",
            (rs,n)->new Policy(rs.getBoolean(1),rs.getBoolean(2),rs.getBoolean(3),rs.getBoolean(4),rs.getBoolean(5)),scope,id);
        return rows.stream().findFirst();
    }
}
