package com.giftplatform.event;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.OccasionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class EventOptimisticLockIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired EntityManagerFactory emf;

    @Test
    void staleWriterCannotOverwriteCommittedEventChange() {
        Event initial = new Event("Original", OccasionType.BIRTHDAY);
initial.setHostDisplayName("Fixture host");
        EntityManager seed = emf.createEntityManager();
        try {
            seed.getTransaction().begin();
            seed.persist(initial);
            seed.getTransaction().commit();
        } finally { seed.close(); }

        EntityManager first = emf.createEntityManager();
        EntityManager stale = emf.createEntityManager();
        try {
            first.getTransaction().begin();
            stale.getTransaction().begin();
            Event firstCopy = first.find(Event.class, initial.getId());
            Event staleCopy = stale.find(Event.class, initial.getId());
            assertNotNull(firstCopy);
            assertNotNull(staleCopy);

            firstCopy.update("Winner", null, OccasionType.BIRTHDAY, null);
            first.getTransaction().commit();

            staleCopy.update("Lost update", null, OccasionType.BIRTHDAY, null);
            assertThrows(RuntimeException.class, () -> {
                stale.flush();
                stale.getTransaction().commit();
            }, "A stale entity version must fail instead of silently overwriting");
            if (stale.getTransaction().isActive()) stale.getTransaction().rollback();
        } finally {
            if (first.getTransaction().isActive()) first.getTransaction().rollback();
            if (stale.getTransaction().isActive()) stale.getTransaction().rollback();
            first.close();
            stale.close();
        }

        EntityManager verify = emf.createEntityManager();
        try {
            assertEquals("Winner", verify.find(Event.class, initial.getId()).getTitle());
        } finally { verify.close(); }
    }
}
