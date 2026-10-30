package com.giftplatform.event.repository;

import com.giftplatform.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<Event> findByPublicToken(UUID publicToken);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Event e where e.id = :id")
    Optional<Event> findForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);
}