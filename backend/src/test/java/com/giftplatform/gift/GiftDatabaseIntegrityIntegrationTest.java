package com.giftplatform.gift;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class GiftDatabaseIntegrityIntegrationTest {
 @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url",postgres::getJdbcUrl);
  r.add("spring.datasource.username",postgres::getUsername);
  r.add("spring.datasource.password",postgres::getPassword);
 }
 @Autowired JdbcTemplate jdbc;
 @Autowired PlatformTransactionManager tx;
 @Autowired AuditService audit;
 UUID seedEvent() {
  UUID user=UUID.randomUUID(), event=UUID.randomUUID();
  jdbc.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",user,user+"@example.test","test-hash","CUSTOMER");
  jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id) VALUES (?,?,?,?,?)",event,"Test","BIRTHDAY","DRAFT",user);
  return event;
 }
 UUID gift(UUID event,BigDecimal amount) {
  UUID id=UUID.randomUUID();
  jdbc.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES (?,?,?,?)",id,event,"Mixer",amount);
  return id;
 }
 int giftCount(UUID id) {return jdbc.queryForObject("SELECT count(*) FROM gifts WHERE id=?",Integer.class,id);}
 @Test void migrationV11Applied() {assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE version='11' AND success=true",Integer.class));}
 @Test void giftPersistsWithDefaultStatus() {UUID id=gift(seedEvent(),new BigDecimal("10000.00"));assertEquals("ACTIVE",jdbc.queryForObject("SELECT status FROM gifts WHERE id=?",String.class,id));}
 @Test void giftRejectsZeroTarget() {UUID event=seedEvent();assertThrows(Exception.class,()->gift(event,BigDecimal.ZERO));}
 @Test void giftRejectsNegativeTarget() {UUID event=seedEvent();assertThrows(Exception.class,()->gift(event,new BigDecimal("-1.00")));}
 @Test void giftRejectsInvalidFulfillmentOverride() {UUID id=gift(seedEvent(),new BigDecimal("1.00"));assertThrows(Exception.class,()->jdbc.update("UPDATE gifts SET fulfillment_override='INVALID' WHERE id=?",id));}
 @Test void giftRejectsMissingEvent() {assertThrows(Exception.class,()->gift(UUID.randomUUID(),new BigDecimal("1.00")));}
 @Test void giftHasVersionColumn() {UUID id=gift(seedEvent(),new BigDecimal("1.00"));assertEquals(0L,jdbc.queryForObject("SELECT version FROM gifts WHERE id=?",Long.class,id));}
 @Test void giftAndAuditRollbackTogether() {
  UUID event=seedEvent(), id=UUID.randomUUID();
  UUID owner=jdbc.queryForObject("SELECT owner_id FROM events WHERE id=?",UUID.class,event);
  new TransactionTemplate(tx).execute(status->{
   jdbc.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES (?,?,?,?)",id,event,"Rollback",new BigDecimal("100.00"));
   audit.recordChange(owner,AuditActorType.CUSTOMER,"GIFT",id,AuditAction.CREATE,null,Map.of("name","Rollback"),null);
   status.setRollbackOnly();return null;
  });
  assertEquals(0,giftCount(id));
  assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM audit_logs WHERE entity_type='GIFT' AND entity_id=?",Integer.class,id));
 }
 @Test void giftAndAuditCommitTogether() {
  UUID event=seedEvent(),id=UUID.randomUUID();
  UUID owner=jdbc.queryForObject("SELECT owner_id FROM events WHERE id=?",UUID.class,event);
  new TransactionTemplate(tx).execute(status->{
   jdbc.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES (?,?,?,?)",id,event,"Committed",new BigDecimal("100.00"));
   audit.recordChange(owner,AuditActorType.CUSTOMER,"GIFT",id,AuditAction.CREATE,null,Map.of("name","Committed"),null);
   return null;
  });
  assertEquals(1,giftCount(id));
  assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM audit_logs WHERE entity_type='GIFT' AND entity_id=?",Integer.class,id));
 }
}
