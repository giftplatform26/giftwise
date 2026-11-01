package com.giftplatform.group;

import com.giftplatform.event.entity.DeliveryAddress;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Individual guests use a private browser session, without group registration. */
@RestController
@RequestMapping("/api/public/groups/{token}/guest-gifts/individual-entry")
public class IndividualGuestGiftController {
    @org.springframework.beans.factory.annotation.Autowired(required=false)
    private com.giftplatform.payment.direct.GuestDirectTransferService transfers;
    private final JdbcTemplate db;
    private final GroupContributionController groups;
    private final EventRepository events;
    public IndividualGuestGiftController(JdbcTemplate db,GroupContributionController groups,EventRepository events){
        this.db=db;this.groups=groups;this.events=events;
    }
    private UUID owner(UUID event,HttpServletRequest request,boolean create){
        var session=request.getSession(create);
        if(session==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        synchronized(session){
            String key="gift.individual.owner."+event;
            if(session.getAttribute(key) instanceof UUID id)return id;
            if(!create)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            UUID id=UUID.randomUUID();session.setAttribute(key,id);return id;
        }
    }
    public enum Method { LINK_AND_CONTRIBUTION, DIRECT_DELIVERY }
    public record Entry(UUID sourceGiftId,@NotBlank @Size(max=160) String name,
        @Size(max=500) String description,@Size(max=1000) @Pattern(regexp="https://[^\\s]+") String productUrl,
        @NotBlank @Size(max=120) String guestName,@NotBlank @Email @Size(max=254) String email,
        @NotBlank @Pattern(regexp="\\+?[0-9][0-9 -]{7,23}") String mobile,
        @NotNull Method givingMethod,@Positive @Max(100000000000L) Long contributionPaise) {}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String,UUID> save(@PathVariable UUID token,@Valid @RequestBody Entry input,HttpServletRequest request){
        if(input.givingMethod()==Method.LINK_AND_CONTRIBUTION && (input.contributionPaise()==null||input.contributionPaise()<100||input.contributionPaise()>100000000))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a contribution between ₹1 and ₹10,00,000");
        if((input.productUrl()==null||input.productUrl().isBlank())&&(input.description()==null||input.description().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Add a product link or gift details");
        if(input.givingMethod()==Method.DIRECT_DELIVERY&&(input.productUrl()==null||input.productUrl().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Add the product link for your purchase");
        UUID event=groups.event(token),owner=owner(event,request,true);
        // An internal attribution record has random, undisclosed credentials. No group or user-facing account is created.
        db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,?,?) ON CONFLICT(id) DO NOTHING",
            owner,event,"guest_"+owner.toString().replace("-",""),new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()));
        Integer count=db.queryForObject("SELECT count(*) FROM guest_gifts WHERE event_id=? AND selected_by=?",Integer.class,event,owner);
        if(count!=null&&count>=20)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many gifts added in this session");
        UUID id=input.sourceGiftId()==null?UUID.randomUUID():input.sourceGiftId();
        if(input.sourceGiftId()==null){
            db.update("""
                INSERT INTO guest_gifts(id,event_id,added_by,selected_by,contribution_type,name,description,product_url,
                  guest_name,guest_email,guest_mobile,contribution_paise,giving_method)
                VALUES(?,?,?,?,'INDIVIDUAL',?,?,?,?,?,?,?,?)
                """,id,event,owner,owner,input.name().strip(),input.description(),input.productUrl(),input.guestName().strip(),
                input.email(),input.mobile(),input.givingMethod()==Method.DIRECT_DELIVERY?null:input.contributionPaise(),input.givingMethod().name());
        }else{
            int changed=db.update("""
                UPDATE guest_gifts SET selected_by=?,contribution_type='INDIVIDUAL',name=?,description=?,product_url=?,
                  guest_name=?,guest_email=?,guest_mobile=?,contribution_paise=?,giving_method=?
                WHERE id=? AND event_id=? AND selected_by IS NULL
                """,owner,input.name().strip(),input.description(),input.productUrl(),input.guestName().strip(),input.email(),input.mobile(),
                input.givingMethod()==Method.DIRECT_DELIVERY?null:input.contributionPaise(),input.givingMethod().name(),id,event);
            if(changed!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"This gift has already been selected. Choose another gift.");
        }
        return Map.of("id",id);
    }
    @GetMapping
    public List<Map<String,Object>> mine(@PathVariable UUID token,HttpServletRequest request){
        UUID event=groups.event(token);
        var session=request.getSession(false);
        if(session==null||!(session.getAttribute("gift.individual.owner."+event) instanceof UUID owner))return List.of();
        return db.queryForList("SELECT id,name,product_url,giving_method,delivery_status FROM guest_gifts WHERE event_id=? AND selected_by=? AND contribution_type='INDIVIDUAL' ORDER BY created_at DESC,id",event,owner);
    }
    private UUID requireGift(UUID token,UUID id,HttpServletRequest request){
        UUID event=groups.event(token),owner=owner(event,request,false);
        if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM guest_gifts WHERE id=? AND event_id=? AND selected_by=? AND contribution_type='INDIVIDUAL')",Boolean.class,id,event,owner)))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return event;
    }
    @GetMapping("/{id}/host-contribution")
    public Map<String,Object> payment(@PathVariable UUID token,@PathVariable UUID id,HttpServletRequest request){
        UUID event=requireGift(token,id,request);
        return db.queryForMap("""
            SELECT g.contribution_paise AS amount_paise,g.giving_method,
              c.id AS claim_id,c.status AS payment_status,c.method AS payment_method
            FROM guest_gifts g LEFT JOIN guest_gift_host_contributions link ON link.guest_gift_id=g.id
            LEFT JOIN guest_direct_transfer_claims c ON c.id=link.claim_id WHERE g.id=? AND g.event_id=?
            """,id,event);
    }
    public record HostContribution(@NotNull UUID destinationId,@NotBlank @Pattern(regexp="UPI|BANK") String method,
        @NotBlank @Pattern(regexp="[A-Za-z0-9-]{8,40}") String transactionReference) {}
    @PostMapping("/{id}/host-contribution")
    @Transactional
    public com.giftplatform.payment.direct.GuestDirectTransferService.ClaimResult contribute(
        @PathVariable UUID token,@PathVariable UUID id,@Valid @RequestBody HostContribution input,HttpServletRequest request){
        UUID event=requireGift(token,id,request);
        var gift=db.queryForMap("SELECT selected_by,giving_method,contribution_paise,guest_name,guest_email,guest_mobile FROM guest_gifts WHERE id=? AND event_id=? FOR UPDATE",id,event);
        if(!"LINK_AND_CONTRIBUTION".equals(gift.get("giving_method"))||gift.get("contribution_paise")==null)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"This gift uses direct delivery");
        if(transfers==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Host contributions are unavailable");
        // Amount and contact come from the session-owned gift, never from the payment request.
        // A stable key makes retries report the same transfer without creating another contribution.
        var result=transfers.claim(token,new com.giftplatform.payment.direct.GuestDirectTransferService.ClaimRequest(
            input.destinationId(),input.method(),null,((Number)gift.get("contribution_paise")).longValue(),
            (String)gift.get("guest_name"),(String)gift.get("guest_email"),(String)gift.get("guest_mobile"),
            input.transactionReference(),"individual-gift-"+id+"-"+gift.get("selected_by")));
        db.update("INSERT INTO guest_gift_host_contributions(guest_gift_id,claim_id) VALUES(?,?) ON CONFLICT(guest_gift_id) DO NOTHING",id,result.claimId());
        return result;
    }
    @GetMapping("/{id}/delivery-address")
    public DeliveryAddress delivery(@PathVariable UUID token,@PathVariable UUID id,HttpServletRequest request){
        UUID event=requireGift(token,id,request);
        return events.findById(event).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)).getDeliveryAddress();
    }
    @PostMapping("/{id}/delivery")
    public Map<String,String> tracking(@PathVariable UUID token,@PathVariable UUID id,@Valid @RequestBody GuestGiftController.Delivery input,HttpServletRequest request){
        UUID event=requireGift(token,id,request);
        if(input.status()==GuestGiftController.DeliveryStatus.TRACKING_SHARED&&(input.trackingDetails()==null||input.trackingDetails().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tracking details required");
        db.update("UPDATE guest_gifts SET delivery_status=?,tracking_details=? WHERE id=? AND event_id=?",input.status().name(),input.trackingDetails(),id,event);
        return Map.of("status",input.status().name());
    }
}
