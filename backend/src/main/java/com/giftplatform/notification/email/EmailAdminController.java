package com.giftplatform.notification.email;
import com.giftplatform.security.ActorContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController
@RequestMapping("/api/admin/emails")
public class EmailAdminController {
 private final JdbcTemplate db;private final ActorContext actor;private final BrandedEmailService service;
 public EmailAdminController(JdbcTemplate db,ActorContext actor,BrandedEmailService service){this.db=db;this.actor=actor;this.service=service;}
 @GetMapping public Map<String,Object> status(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);return Map.of("configured",service.available(),"delivery",db.queryForList("SELECT template_key,status,count(*) AS count FROM transactional_email_outbox GROUP BY template_key,status ORDER BY template_key,status"));}
}
