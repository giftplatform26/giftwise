package com.giftplatform.localization;

import com.giftplatform.security.ActorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
public class LocalizationController {
    private final LocalizationService service;private final JdbcTemplate db;private final ActorContext actor;
    public LocalizationController(LocalizationService service,JdbcTemplate db,ActorContext actor){this.service=service;this.db=db;this.actor=actor;}
    public record Preference(@NotBlank @Size(max=63) String language) {}
    @GetMapping("/api/public/localization/catalog")
    @Transactional(readOnly=true)
    public ResponseEntity<LocalizationService.Catalog> guest(@RequestParam(required=false) UUID token,@RequestParam(required=false) String language){
        if(language!=null&&language.length()>63)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        UUID[] scope=service.publicEvent(token);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.catalog(scope[0],scope[1],language,false));
    }
    @GetMapping("/api/localization/catalog")
    @Transactional(readOnly=true)
    public ResponseEntity<LocalizationService.Catalog> account(){UUID user=actor.requireAuthenticatedUserId();return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.catalog(user,null,null,actor.isPlatformAdmin()));}
    @GetMapping("/api/auth/language")
    public ResponseEntity<Map<String,Object>> preference(){UUID user=actor.requireAuthenticatedUserId();Map<String,Object> result=new LinkedHashMap<>();result.put("preferredLanguage",service.preference(user));result.put("catalog",service.catalog(user,null,null,actor.isPlatformAdmin()));return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);}
    @PostMapping("/api/auth/language")
    @Transactional
    public Map<String,String> preference(@Valid @RequestBody Preference input){UUID user=actor.requireAuthenticatedUserId();if(!(actor.isPlatformAdmin()?service.settings().enabled():service.available(user,null)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Multi-language support is disabled");
        if(service.languages(true).stream().noneMatch(l->l.code().equals(input.language())))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose an enabled language");
        db.update("INSERT INTO user_language_preferences(user_id,language_code) VALUES(?,?) ON CONFLICT(user_id) DO UPDATE SET language_code=EXCLUDED.language_code,changed_at=now()",user,input.language());return Map.of("preferredLanguage",input.language());
    }
}
