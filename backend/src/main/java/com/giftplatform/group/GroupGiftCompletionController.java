package com.giftplatform.group;

import com.giftplatform.event.entity.DeliveryAddress;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/public/groups/{token}")
public class GroupGiftCompletionController {
    private final JdbcTemplate db;
    private final GroupContributionController groups;
    private final EventRepository events;
    public GroupGiftCompletionController(JdbcTemplate db,GroupContributionController groups,EventRepository events){
        this.db=db;this.groups=groups;this.events=events;
    }
    @GetMapping("/gift-board")
    public List<Map<String,Object>> board(@PathVariable UUID token){
        var rows=db.queryForList("""
            SELECT id,name AS group_name,'GROUP' AS contribution_type,
              CASE WHEN gift_published_at IS NULL THEN NULL ELSE gift_name END AS gift_name,
              CASE WHEN gift_published_at IS NULL THEN NULL ELSE gift_description END AS gift_description,
              CASE WHEN gift_published_at IS NULL THEN NULL ELSE gift_link END AS gift_link,
              CASE WHEN gift_published_at IS NULL THEN 'Something is Cooking 🍳'
                ELSE '100% Contributed ✅' END AS display_status
            FROM gift_groups WHERE event_id=? AND independent_gift ORDER BY created_at,id
            """,groups.event(token));
        for(var row:rows){
            boolean available=true;
            try{groups.publicGroup(token,(UUID)row.get("id"));}catch(ResponseStatusException ex){available=false;}
            row.put("contributions_available",available);
        }
        return rows;
    }
    @GetMapping("/gift-progress/{groupId}")
    public Map<String,Object> progress(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){
        groups.publicGroup(token,groupId);
        // A member session is scoped to this group, never just the invitation.
        groups.member(groupId,request);
        return db.queryForMap("SELECT g.target_paise,p.collected_paise,p.reported_paise,p.completion_percentage FROM gift_groups g JOIN group_gift_progress p ON p.id=g.id WHERE g.id=?",groupId);
    }
    private Map<String,Object> locked(UUID token,UUID groupId,HttpServletRequest request){
        groups.requireGroupCreator(token,groupId,request);
        return db.queryForMap("SELECT id,target_paise,gift_name,gift_published_at,fulfilment_status FROM gift_groups WHERE id=? FOR UPDATE",groupId);
    }
    private void funded(UUID groupId,Map<String,Object> gift){
        Number target=(Number)gift.get("target_paise");
        Long collected=db.queryForObject("SELECT collected_paise FROM group_gift_progress WHERE id=?",Long.class,groupId);
        if(target==null||collected==null||collected<target.longValue())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"The target must be fully funded by verified or creator acknowledged receipts");
    }
    @PostMapping("/gift-publish/{groupId}")
    @Transactional
    public Map<String,String> publish(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){
        var gift=locked(token,groupId,request);funded(groupId,gift);
        if(gift.get("gift_name")==null||gift.get("gift_name").toString().isBlank())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Gift name required before publishing");
        if(gift.get("gift_published_at")!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Gift already published");
        db.update("UPDATE gift_groups SET gift_published_at=now() WHERE id=?",groupId);
        return Map.of("status","PUBLISHED","displayStatus","100% Contributed ✅");
    }
    @PostMapping("/gift-receipts/{groupId}/{reportId}")
    @Transactional
    public Map<String,String> acknowledgeReceipt(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID reportId,HttpServletRequest request){
        locked(token,groupId,request);
        int changed=db.update("UPDATE gift_group_guest_reports SET creator_received_at=COALESCE(creator_received_at,now()) WHERE id=? AND group_id=? AND state='REPORTED_UNVERIFIED'",reportId,groupId);
        changed+=db.update("UPDATE gift_group_payment_reports SET creator_received_at=COALESCE(creator_received_at,now()) WHERE id=? AND group_id=? AND state='REPORTED_UNVERIFIED'",reportId,groupId);
        if(changed!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Contribution report not found");
        return Map.of("status","CREATOR_ACKNOWLEDGED","notice","Receipt acknowledged by creator; payment is not independently verified");
    }
    public enum Status { GIFT_PURCHASED, TRACKING_SHARED, AMOUNT_TRANSFERRED_TO_HOST, COMPLETED }
    public record Fulfilment(@NotNull Status status,@Size(max=1000) String trackingDetails) {}
    @PostMapping("/gift-fulfilment/{groupId}")
    @Transactional
    public Map<String,String> fulfil(@PathVariable UUID token,@PathVariable UUID groupId,@Valid @RequestBody Fulfilment input,HttpServletRequest request){
        var gift=locked(token,groupId,request);funded(groupId,gift);
        String previous=(String)gift.get("fulfilment_status");
        boolean allowed=switch(input.status()){
            case GIFT_PURCHASED, AMOUNT_TRANSFERRED_TO_HOST -> "PLANNED".equals(previous);
            case TRACKING_SHARED -> "GIFT_PURCHASED".equals(previous)||"TRACKING_SHARED".equals(previous);
            case COMPLETED -> Set.of("GIFT_PURCHASED","TRACKING_SHARED","AMOUNT_TRANSFERRED_TO_HOST").contains(previous);
        };
        if(!allowed)throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid fulfilment transition");
        if(input.status()==Status.TRACKING_SHARED&&(input.trackingDetails()==null||input.trackingDetails().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tracking details required");
        db.update("UPDATE gift_groups SET fulfilment_status=?,tracking_details=COALESCE(?,tracking_details) WHERE id=?",input.status().name(),input.trackingDetails(),groupId);
        return Map.of("status",input.status().name());
    }
    @GetMapping("/gift-delivery-address/{groupId}")
    public DeliveryAddress delivery(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){
        groups.requireGroupCreator(token,groupId,request);
        return events.findById(groups.event(token)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)).getDeliveryAddress();
    }
}
