package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Host-level features (platform switch AND host privilege), for account-wide navigation such as the gift ledger. */
@RestController
public class HostFeaturesController {
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;
    public HostFeaturesController(ActorContext actor, EffectiveModulePermissionService permissions) { this.actor = actor; this.permissions = permissions; }

    @GetMapping("/api/v1/me/features")
    public ResponseEntity<Map<String, Boolean>> features() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(permissions.modules(actor.requireAuthenticatedUserId()));
    }
}
