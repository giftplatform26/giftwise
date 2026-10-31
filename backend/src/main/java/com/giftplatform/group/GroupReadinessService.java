package com.giftplatform.group;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GroupReadinessService {
 private final JdbcTemplate db;
 public GroupReadinessService(JdbcTemplate db){this.db=db;}
 public record Readiness(boolean enabled,List<String> reasons){}
 public Readiness invitation(UUID groupId){
  List<String> reasons=new ArrayList<>();
  if(!Boolean.TRUE.equals(db.queryForObject("SELECT a.email_verified_at IS NOT NULL FROM gift_groups g JOIN gift_group_accounts a ON a.id=g.admin_id WHERE g.id=?",Boolean.class,groupId))) reasons.add("Verify your email in Account");
  if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM gift_group_payment_destinations WHERE group_id=? AND enabled AND status IN ('REGISTERED','VERIFIED'))",Boolean.class,groupId))) reasons.add("Add and enable a receiving account");
  return new Readiness(reasons.isEmpty(),List.copyOf(reasons));
 }
 public boolean memberEmailRequired(UUID eventId){
  return Boolean.TRUE.equals(db.queryForObject("SELECT COALESCE((SELECT p.admin_enabled OR p.host_enabled FROM group_member_email_policies p WHERE p.host_id=e.owner_id),FALSE) FROM events e WHERE e.id=?",Boolean.class,eventId));
 }
}
