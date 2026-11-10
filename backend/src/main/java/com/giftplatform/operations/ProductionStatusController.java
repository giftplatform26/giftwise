package com.giftplatform.operations;
import com.giftplatform.security.ActorContext;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController
@RequestMapping("/api/admin/console/production-status")
public class ProductionStatusController {
 private final ActorContext actor;private final Environment environment;
 public ProductionStatusController(ActorContext actor,Environment environment){this.actor=actor;this.environment=environment;}
 @GetMapping public ResponseEntity<Map<String,Object>> status(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);actor.requireAuthenticatedUserId();return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("configurationIssues",ProductionConfigurationPolicy.violations(environment),"productionProfileActive",Arrays.stream(environment.getActiveProfiles()).anyMatch(p->p.equals("production")||p.equals("prod")),"livePaymentsReady",false,"livePayoutsReady",false,"refundsReady",false,"backupRestoreVerified",false,"notice","Configuration checks cannot verify provider enrollment, real delivery, backup recovery, penetration testing or legal approval. See the production gap register."));}
}
