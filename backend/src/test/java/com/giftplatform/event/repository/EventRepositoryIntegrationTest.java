
package com.giftplatform.event.repository;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.OccasionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



import com.giftplatform.event.entity.EventStatus;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Testcontainers
class EventRepositoryIntegrationTest {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;


    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("giftplatform_test")
                    .withUsername("testuser")
                    .withPassword("testpassword");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveAndRetrieveEvent() {

        Event event = new Event(
                "Birthday Celebration",
                OccasionType.BIRTHDAY
        );

        Event savedEvent = eventRepository.saveAndFlush(event);

        assertThat(savedEvent.getId()).isNotNull();

        Event retrievedEvent = eventRepository
                .findById(savedEvent.getId())
                .orElseThrow();

        assertThat(retrievedEvent.getTitle())
                .isEqualTo("Birthday Celebration");

        assertThat(retrievedEvent.getOccasionType())
                .isEqualTo(OccasionType.BIRTHDAY);

        assertThat(retrievedEvent.getStatus())
                .isEqualTo(com.giftplatform.event.entity.EventStatus.DRAFT);
    }

    @Test
    void shouldRejectEventWithNullTitle() {

        Event event = new Event(
                null,
                OccasionType.BIRTHDAY
        );

        assertThatThrownBy(() ->
                eventRepository.saveAndFlush(event)
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }


    @Test
    void shouldAcceptEventTitleWith200Characters() {

        String title = "A".repeat(200);

        Event event = new Event(
                title,
                OccasionType.BIRTHDAY
        );

        Event savedEvent = eventRepository.saveAndFlush(event);

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getTitle()).hasSize(200);

        Event retrievedEvent = eventRepository
                .findById(savedEvent.getId())
                .orElseThrow();

        assertThat(retrievedEvent.getTitle())
                .isEqualTo(title);
    }


    @Test
    void shouldRejectEventTitleWith201Characters() {

        String title = "A".repeat(201);

        Event event = new Event(
                title,
                OccasionType.BIRTHDAY
        );

        assertThatThrownBy(() ->
                eventRepository.saveAndFlush(event)
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }


    @Test
    void shouldAllowNullDescriptionAndEventDate() {

        Event event = new Event(
                "Housewarming Ceremony",
                OccasionType.HOUSEWARMING
        );

        Event savedEvent = eventRepository.saveAndFlush(event);

        assertThat(savedEvent.getId()).isNotNull();

        // Reload from the database
        Event retrievedEvent = eventRepository
                .findById(savedEvent.getId())
                .orElseThrow();

        assertThat(retrievedEvent.getDescription()).isNull();
        assertThat(retrievedEvent.getEventDate()).isNull();

        assertThat(retrievedEvent.getOccasionType())
                .isEqualTo(OccasionType.HOUSEWARMING);
    }


    @Test
    void shouldGenerateUniqueEventIds() {
        Set<UUID> ids = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            Event event = new Event(
                    "Birthday " + i,
                    OccasionType.BIRTHDAY
            );

            assertThat(event.getId()).isNotNull();
            assertThat(ids.add(event.getId())).isTrue();
        }
    }

    @Test
    void shouldDefaultStatusToDraft() {
        Event event = new Event(
                "Wedding",
                OccasionType.WEDDING
        );

        assertThat(event.getStatus())
                .isEqualTo(EventStatus.DRAFT);

        Event saved = eventRepository.saveAndFlush(event);

        assertThat(saved.getStatus())
                .isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void shouldPersistAllOccasionTypes() {
        for (OccasionType type : OccasionType.values()) {
            Event event = new Event(
                    "Event " + type.name(),
                    type
            );

            Event saved = eventRepository.saveAndFlush(event);

            assertThat(
                    eventRepository.findById(saved.getId())
                            .orElseThrow()
                            .getOccasionType()
            ).isEqualTo(type);
        }
    }

    @Test
    void shouldRejectNullOccasionType() {
        Event event = new Event("Test", null);

        assertThatThrownBy(() ->
                eventRepository.saveAndFlush(event)
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }


    @Test
    void shouldReturnEmptyForUnknownEventId() {
        UUID unknownId = UUID.randomUUID();

        assertThat(eventRepository.findById(unknownId))
                .isEmpty();
    }

    @Test
    void shouldDeleteExistingEvent() {
        Event event = new Event(
                "Delete Test",
                OccasionType.BIRTHDAY
        );

        Event saved = eventRepository.saveAndFlush(event);

        UUID eventId = saved.getId();

        eventRepository.deleteById(eventId);
        eventRepository.flush();

        assertThat(eventRepository.findById(eventId))
                .isEmpty();
    }

    @Test
    void shouldGenerateDatabaseTimestamps() {
        Event event = new Event(
                "Timestamp Test",
                OccasionType.BIRTHDAY
        );

        Event saved = eventRepository.saveAndFlush(event);

        var timestamps = jdbcTemplate.queryForMap(
                """
                SELECT created_at, updated_at
                FROM events
                WHERE id = ?
                """,
                saved.getId()
        );

        assertThat(timestamps.get("created_at")).isNotNull();
        assertThat(timestamps.get("updated_at")).isNotNull();
    }

}
