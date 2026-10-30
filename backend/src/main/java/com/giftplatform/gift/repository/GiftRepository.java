package com.giftplatform.gift.repository;

import com.giftplatform.gift.entity.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GiftRepository extends JpaRepository<Gift, UUID> {
    List<Gift> findByEventIdOrderByCreatedAtAsc(UUID eventId);
    List<Gift> findByEventIdAndEventOwnerId(UUID eventId, UUID ownerId);
}
