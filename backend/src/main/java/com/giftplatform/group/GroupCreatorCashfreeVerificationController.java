package com.giftplatform.group;

import com.giftplatform.hostpayment.verification.cashfree.CashfreeVerificationAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

/** Explicit creator authorization; no public unauthenticated verification. */
@RestController
public class GroupCreatorCashfreeVerificationController {
  private final GroupContributionController groups;
  private final CashfreeVerificationAttemptService attempts;
  public GroupCreatorCashfreeVerificationController(GroupContributionController groups,CashfreeVerificationAttemptService attempts){this.groups=groups;this.attempts=attempts;}
  @PostMapping("/api/public/groups/{token}/payments/{groupId}/destinations/{id}/verify-cashfree")
  public Map<String,Object> verify(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id,HttpServletRequest request){
    UUID creator=groups.requireGroupCreator(token,groupId,request);
    return attempts.requestSandbox(groupId,id,creator);
  }
}
