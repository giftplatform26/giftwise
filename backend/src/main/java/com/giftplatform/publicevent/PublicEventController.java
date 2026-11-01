package com.giftplatform.publicevent;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.gift.entity.GiftStatus;
import com.giftplatform.gift.entity.GiftVisibility;
import com.giftplatform.gift.entity.GiftPriority;
import java.util.Comparator;
import com.giftplatform.gift.repository.GiftRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.giftplatform.admin.EffectiveModulePermissionService;
import java.util.UUID;

@Controller
@RequestMapping("/api/public/events")
public class PublicEventController {
    private final EventRepository events;
    private final GiftRepository gifts;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.jdbc.core.JdbcTemplate db;

    private final EffectiveModulePermissionService permissions;

    public PublicEventController(EventRepository events, GiftRepository gifts, EffectiveModulePermissionService permissions) {
        this.events = events;
        this.gifts = gifts;
        this.permissions = permissions;
    }

    @GetMapping("/{token}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<PublicEventView> view(@PathVariable UUID token) {
        Event event = events.findByPublicToken(token)
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        java.util.Set<UUID> reserved=new java.util.HashSet<>(db.query("SELECT reserved_gift_id FROM gift_groups WHERE event_id=? AND reserved_gift_id IS NOT NULL",(rs,n)->rs.getObject(1,UUID.class),event.getId()));
        List<PublicGiftView> visibleGifts = gifts.findByEventIdOrderByCreatedAtAsc(event.getId())
                .stream()
                .filter(g -> g.getVisibility() == GiftVisibility.PUBLIC && g.getStatus() == GiftStatus.ACTIVE)
                .sorted(Comparator.comparing((com.giftplatform.gift.entity.Gift g) -> g.getPriority().ordinal())
                        .thenComparing(com.giftplatform.gift.entity.Gift::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(g -> g.getId().toString()))
                .map(g -> new PublicGiftView(g.getId(), g.getName(), g.getDescription(), g.getTargetAmount(), g.getPriority(),g.getImageKey()!=null&&g.getImageKey().startsWith("gift-photo:")?"/backend/api/public/events/"+token+"/gifts/"+g.getId()+"/photo":null,!reserved.contains(g.getId()),reserved.contains(g.getId())?"A little circle of love is bringing this gift to life. A lovely surprise is on its way ♡":null,g.getProductUrl()==null?null:"/backend/api/public/commerce/affiliate/"+token+"/"+g.getId()))
                .toList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new PublicEventView(event.getTitle(), event.getDescription(),
                        event.getOccasionType().name(), event.getEventDate(), visibleGifts, permissions.modules(event.getOwnerId()), event.getHostDisplayName(), event.getQrTheme()==null?null:event.getQrTheme().name()));
    }

    @GetMapping("/{token}/features")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String,Boolean>> features(@PathVariable UUID token) {
        Event event=events.findByPublicToken(token).filter(e->e.getStatus()==EventStatus.PUBLISHED)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(permissions.modules(event.getOwnerId()));
    }

    public record PublicGiftView(UUID id, String name, String description, BigDecimal targetAmount, GiftPriority priority,String imageUrl,boolean contributionEnabled,String contributionDisabledReason,String productUrl) {}
    public record PublicEventView(String title, String description, String occasionType,
                                  LocalDate eventDate, List<PublicGiftView> gifts, Map<String,Boolean> features, String hostName, String invitationTheme) {}
}
