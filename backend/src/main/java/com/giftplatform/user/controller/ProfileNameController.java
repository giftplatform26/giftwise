package com.giftplatform.user.controller;

import com.giftplatform.security.GiftUserPrincipal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/profile-name")
public class ProfileNameController {
 @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.audit.service.AuditService audit;
 private final JdbcTemplate db;
 public ProfileNameController(JdbcTemplate db){this.db=db;}
 public record NameInput(String fullName){}
 private String name(String value){
  if(value==null||value.strip().length()<2||value.strip().length()>120||value.chars().anyMatch(Character::isISOControl))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter your full name (2–120 characters)");
  return value.strip();
 }
 @GetMapping public Map<String,Object> get(Authentication auth){
  GiftUserPrincipal user=(GiftUserPrincipal)auth.getPrincipal();
  String value=db.queryForObject("SELECT full_name FROM users WHERE id=?",String.class,user.getUserId());
  return java.util.Collections.singletonMap("fullName",value);
 }
 @PostMapping @org.springframework.transaction.annotation.Transactional public Map<String,String> update(Authentication auth,@RequestBody NameInput input){
  GiftUserPrincipal user=(GiftUserPrincipal)auth.getPrincipal();
  String value=name(input==null?null:input.fullName());
  db.update("UPDATE users SET full_name=? WHERE id=?",value,user.getUserId());
  if(audit!=null)audit.recordChange(user.getUserId(),com.giftplatform.audit.entity.AuditActorType.CUSTOMER,"USER",user.getUserId(),com.giftplatform.audit.entity.AuditAction.UPDATE,Map.of(),Map.of("profileUpdated",true),null);
  return Map.of("fullName",value);
 }
}
