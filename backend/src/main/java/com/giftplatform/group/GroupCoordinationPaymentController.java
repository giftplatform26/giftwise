package com.giftplatform.group;

import com.giftplatform.monetization.CommercialPurchaseService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/public/groups/{token}/coordination/{groupId}")
public class GroupCoordinationPaymentController {
    private final GroupContributionController groups;private final CommercialPurchaseService purchases;
    public GroupCoordinationPaymentController(GroupContributionController groups,CommercialPurchaseService purchases){this.groups=groups;this.purchases=purchases;}
    private CommercialPurchaseService.Buyer buyer(UUID token,UUID group,HttpServletRequest request){return new CommercialPurchaseService.Buyer("GROUP_ADMIN",groups.requireGroupCreator(token,group,request));}
    @GetMapping public CommercialPurchaseService.Offer offer(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){return purchases.offer(buyer(token,groupId,request),"GROUP_GIFT",groupId);}
    @PostMapping("/checkout") public CommercialPurchaseService.Checkout checkout(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody CommercialPurchaseService.Request input,HttpServletRequest request){
        var buyer=buyer(token,groupId,request);
        if(input==null||!"GROUP_GIFT".equals(input.stream())||!groupId.equals(input.resourceId()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return purchases.create(buyer,input);
    }
    @PostMapping("/confirm") public CommercialPurchaseService.Checkout confirm(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody CommercialPurchaseService.Confirmation input,HttpServletRequest request){return purchases.confirm(buyer(token,groupId,request),input);}
}
