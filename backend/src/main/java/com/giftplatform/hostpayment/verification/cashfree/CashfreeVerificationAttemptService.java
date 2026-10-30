package com.giftplatform.hostpayment.verification.cashfree;

import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Records sandbox checks without promoting destination status or touching contributions. */
@Service
public class CashfreeVerificationAttemptService {
  private final JdbcTemplate db;
  private final CashfreeSecureIdTransport transport;
  public CashfreeVerificationAttemptService(JdbcTemplate db,CashfreeSecureIdTransport transport){this.db=db;this.transport=transport;}
  @Transactional
  public Map<String,Object> requestSandbox(UUID groupId,UUID destinationId,UUID creatorId){
    Integer count=db.queryForObject("SELECT count(*) FROM gift_group_payment_destinations WHERE id=? AND group_id=? AND status<>'REVOKED'",Integer.class,destinationId,groupId);
    if(count==null || count!=1) throw new IllegalArgumentException("Destination not found");
    if(!transport.available()) return Map.of("status","UNAVAILABLE","message","Secure ID sandbox is not enabled");
    UUID id=UUID.randomUUID(); String ref="cf-sandbox-"+id;
    // No remote API call here: correct product-specific request schema and entitlement
    // must be confirmed before transmitting bank account or UPI details.
    db.update("INSERT INTO gift_group_destination_verification_attempts(id,group_id,destination_id,requested_by,provider,environment,request_reference,state,completed_at) VALUES(?,?,?,?,'CASHFREE','SANDBOX',?,'UNAVAILABLE',now())",id,groupId,destinationId,creatorId,ref);
    return Map.of("attemptId",id,"status","UNAVAILABLE","message","Sandbox integration staged; provider API entitlement and request schema not yet confirmed");
  }
}
