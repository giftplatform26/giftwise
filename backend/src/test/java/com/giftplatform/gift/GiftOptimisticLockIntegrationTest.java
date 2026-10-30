package com.giftplatform.gift;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.gift.entity.Gift;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class GiftOptimisticLockIntegrationTest {
 @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url",postgres::getJdbcUrl);
  r.add("spring.datasource.username",postgres::getUsername);
  r.add("spring.datasource.password",postgres::getPassword);
 }
 @Autowired EntityManagerFactory emf;
 @Test void staleGiftWriterCannotOverwriteCommittedUpdate() {
  Event event=new Event("Birthday",OccasionType.BIRTHDAY);
  Gift gift=new Gift(event,"Mixer",new BigDecimal("100.00"));
  EntityManager seed=emf.createEntityManager();
  try {seed.getTransaction().begin();seed.persist(event);seed.persist(gift);seed.getTransaction().commit();}
  finally {seed.close();}
  EntityManager first=emf.createEntityManager(),stale=emf.createEntityManager();
  try {
   first.getTransaction().begin();stale.getTransaction().begin();
   Gift winner=first.find(Gift.class,gift.getId()),loser=stale.find(Gift.class,gift.getId());
   assertNotNull(winner);assertNotNull(loser);
   winner.setDetails("Winner",null,null,new BigDecimal("110.00"));
   first.getTransaction().commit();
   loser.setDetails("Lost update",null,null,new BigDecimal("120.00"));
   assertThrows(RuntimeException.class,()->{stale.flush();stale.getTransaction().commit();});
   if(stale.getTransaction().isActive())stale.getTransaction().rollback();
  } finally {
   if(first.getTransaction().isActive())first.getTransaction().rollback();
   if(stale.getTransaction().isActive())stale.getTransaction().rollback();
   first.close();stale.close();
  }
  EntityManager verify=emf.createEntityManager();
  try {assertEquals("Winner",verify.find(Gift.class,gift.getId()).getName());}
  finally {verify.close();}
 }
}
