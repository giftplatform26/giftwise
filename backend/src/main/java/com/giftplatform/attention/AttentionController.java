package com.giftplatform.attention;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AttentionController {
    private final AttentionService attention;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;
    public AttentionController(AttentionService attention, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.attention = attention; this.actor = actor; this.permissions = permissions;
    }

    public record View(List<AttentionService.Item> items, String summary) {}

    @GetMapping("/api/v1/me/attention")
    public ResponseEntity<View> attention() {
        UUID me = actor.requireAuthenticatedUserId();
        permissions.require(me, AttentionService.MODULE);
        List<AttentionService.Item> items = attention.items(me);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new View(items, AttentionService.summary(items)));
    }
}
