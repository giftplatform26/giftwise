package com.giftplatform.group;

import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Invitation-scoped surprises are deliberately separate from the host wishlist. */
@RestController
public class GuestGiftController {
    private final JdbcTemplate db;
    private final GroupContributionController accounts;
    private final EventRepository events;
    private final ActorContext actors;
    public GuestGiftController(JdbcTemplate db,GroupContributionController accounts,EventRepository events,ActorContext actors){
        this.db=db;this.accounts=accounts;this.events=events;this.actors=actors;
    }
    public record NewGift(@NotBlank @Size(max=160) String name,@Size(max=500) String description,
                          @Size(max=1000) @Pattern(regexp="https://[^\\s]+") String productUrl){}
    public record Selection(@NotNull Type contributionType,UUID groupId){}
    public enum Type { INDIVIDUAL, GROUP }
    @GetMapping("/api/public/groups/{token}/guest-gifts")
    public List<Map<String,Object>> list(@PathVariable UUID token){
        return db.queryForList("""
            SELECT gift.id,
            CASE WHEN gift.contribution_type='GROUP' AND g.gift_published_at IS NULL THEN NULL ELSE gift.name END AS name,
            CASE WHEN gift.contribution_type='GROUP' AND g.gift_published_at IS NULL THEN NULL ELSE gift.description END AS description,
            CASE WHEN gift.contribution_type='GROUP' AND g.gift_published_at IS NULL THEN NULL ELSE gift.product_url END AS product_url,
            gift.contribution_type,
            CASE WHEN gift.contribution_type='GROUP' THEN
              CASE WHEN g.gift_published_at IS NULL THEN 'Something is Cooking 🍳' ELSE '100% Contributed ✅' END
              ELSE 'Presented with love ♡' END AS display_status,
            selected_by IS NOT NULL AS disabled,
            CASE WHEN contribution_type='GROUP' THEN 'A gift of love from a group ♡'
                 WHEN contribution_type='INDIVIDUAL' THEN 'Presented with love ♡' ELSE 'Available' END AS selection_label
            FROM guest_gifts gift LEFT JOIN gift_groups g ON g.id=gift.group_id WHERE gift.event_id=? ORDER BY gift.created_at,gift.id
            """,accounts.event(token));
    }
    @PostMapping("/api/public/groups/{token}/guest-gifts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,UUID> add(@PathVariable UUID token,@Valid @RequestBody NewGift input,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request),id=UUID.randomUUID();
        db.update("INSERT INTO guest_gifts(id,event_id,added_by,name,description,product_url) VALUES(?,?,?,?,?,?)",id,event,creator,input.name().trim(),input.description(),input.productUrl());
        return Map.of("id",id);
    }
    @PostMapping("/api/public/groups/{token}/guest-gifts/{id}/select")
    @Transactional
    public Map<String,String> select(@PathVariable UUID token,@PathVariable UUID id,@Valid @RequestBody Selection input,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request);
        if(input.contributionType()==Type.GROUP){
            if(input.groupId()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Group required");
            accounts.requireGroupCreator(token,input.groupId(),request);
        } else if(input.groupId()!=null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Individual gifts cannot have a group");
        int changed=db.update("UPDATE guest_gifts SET selected_by=?,contribution_type=?,group_id=? WHERE id=? AND event_id=? AND selected_by IS NULL",creator,input.contributionType().name(),input.groupId(),id,event);
        if(changed!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Gift unavailable or already selected");
        return Map.of("status","SELECTED");
    }
    @GetMapping("/api/public/groups/{token}/guest-gifts/mine")
    public List<Map<String,Object>> mine(@PathVariable UUID token,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request);
        return db.queryForList("""
            SELECT gift.id,name,description,product_url,contribution_type,group_id,published_at,
              CASE WHEN selected_by=viewer.id THEN guest_name END AS guest_name,
              CASE WHEN selected_by=viewer.id THEN guest_email END AS guest_email,
              CASE WHEN selected_by=viewer.id THEN guest_mobile END AS guest_mobile,
              CASE WHEN selected_by=viewer.id THEN contribution_paise END AS contribution_paise,
              CASE WHEN selected_by=viewer.id THEN delivery_status END AS delivery_status,
              CASE WHEN selected_by=viewer.id THEN tracking_details END AS tracking_details,
              selected_by=viewer.id AS managed_by_me
            FROM guest_gifts gift CROSS JOIN (VALUES (?::uuid)) viewer(id)
            WHERE event_id=? AND (added_by=viewer.id OR selected_by=viewer.id) ORDER BY created_at,gift.id
            """,creator,event);
    }
    @PostMapping("/api/public/groups/{token}/guest-gifts/{id}/publish")
    @Transactional
    public Map<String,String> publish(@PathVariable UUID token,@PathVariable UUID id,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request);
        int changed=db.update("UPDATE guest_gifts SET published_at=now() WHERE id=? AND event_id=? AND selected_by=? AND published_at IS NULL AND contribution_type='INDIVIDUAL'",id,event,creator);
        if(changed!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Select this gift before publishing, or it is already published");
        return Map.of("status","PUBLISHED");
    }
    @GetMapping("/api/public/groups/{token}/guest-gifts/{id}/delivery-address")
    public com.giftplatform.event.entity.DeliveryAddress delivery(@PathVariable UUID token,@PathVariable UUID id,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request);
        if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM guest_gifts WHERE id=? AND event_id=? AND selected_by=?)",Boolean.class,id,event,creator)))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return events.findById(event).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)).getDeliveryAddress();
    }
    public record IndividualGift(@NotBlank @Size(max=160) String name,
        @Size(max=500) String description,@Size(max=1000) @Pattern(regexp="https://[^\\s]+") String productUrl,
        @NotBlank @Size(max=120) String guestName,@NotBlank @Email @Size(max=254) String email,
        @NotBlank @Pattern(regexp="\\+?[0-9][0-9 -]{7,23}") String mobile,
        @Positive @Max(100000000000L) Long contributionPaise) {}
    @PostMapping("/api/public/groups/{token}/guest-gifts/individual")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,UUID> individual(@PathVariable UUID token,@Valid @RequestBody IndividualGift input,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request),id=UUID.randomUUID();
        db.update("""
          INSERT INTO guest_gifts(id,event_id,added_by,selected_by,contribution_type,name,description,product_url,
            guest_name,guest_email,guest_mobile,contribution_paise)
          VALUES(?,?,?,?,'INDIVIDUAL',?,?,?,?,?,?,?)
          """,id,event,creator,creator,input.name().trim(),input.description(),input.productUrl(),
          input.guestName().trim(),input.email(),input.mobile(),input.contributionPaise());
        return Map.of("id",id);
    }
    public enum DeliveryStatus { PLANNED, GIFT_PURCHASED, TRACKING_SHARED, COMPLETED }
    public record Delivery(@NotNull DeliveryStatus status,@Size(max=1000) String trackingDetails) {}
    @PostMapping("/api/public/groups/{token}/guest-gifts/{id}/delivery")
    public Map<String,String> updateDelivery(@PathVariable UUID token,@PathVariable UUID id,@Valid @RequestBody Delivery input,HttpServletRequest request){
        UUID event=accounts.event(token),creator=accounts.sessionAdmin(event,request);
        if(input.status()==DeliveryStatus.TRACKING_SHARED && (input.trackingDetails()==null||input.trackingDetails().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tracking details required");
        int changed=db.update("UPDATE guest_gifts SET delivery_status=?,tracking_details=? WHERE id=? AND event_id=? AND selected_by=? AND contribution_type='INDIVIDUAL'",
            input.status().name(),input.trackingDetails(),id,event,creator);
        if(changed!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("status",input.status().name());
    }
    @GetMapping("/api/v1/events/{eventId}/contributions/guest-gifts")
    public List<Map<String,Object>> hostReport(@PathVariable UUID eventId){
        UUID owner=actors.requireAuthenticatedUserId();
        if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM events WHERE id=? AND owner_id=?)",Boolean.class,eventId,owner)))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return db.queryForList("""
            SELECT gift.id,gift.name,gift.description,gift.product_url,gift.contribution_type,gift.group_id,
                   gift.published_at,COALESCE(gift.guest_name,a.full_name) AS contributor_name,g.name AS group_name,
                   gift.guest_email,gift.guest_mobile,gift.contribution_paise,gift.giving_method,
                   payment.id AS host_payment_claim_id,payment.status AS host_payment_status,payment.method AS host_payment_method,
                   CASE WHEN gift.contribution_type='GROUP' THEN g.fulfilment_status ELSE gift.delivery_status END AS delivery_status,
                   CASE WHEN gift.contribution_type='GROUP' THEN g.tracking_details ELSE gift.tracking_details END AS tracking_details,
                   CASE WHEN gift.contribution_type='GROUP' THEN
                     CASE WHEN g.gift_published_at IS NULL THEN 'Something is Cooking' ELSE '100% Contributed' END
                     ELSE 'Planned Gift' END AS display_status
            FROM guest_gifts gift JOIN gift_group_accounts a ON a.id=gift.selected_by
            LEFT JOIN gift_groups g ON g.id=gift.group_id
            LEFT JOIN guest_gift_host_contributions link ON link.guest_gift_id=gift.id
            LEFT JOIN guest_direct_transfer_claims payment ON payment.id=link.claim_id
            WHERE gift.event_id=? AND gift.selected_by IS NOT NULL
            ORDER BY COALESCE(gift.published_at,g.collection_published_at) DESC,gift.id
            """,eventId);
    }
}
