package com.giftplatform.payment.repository;

import com.giftplatform.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, UUID> {
    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);

    java.util.List<PaymentOrder> findByEventIdOrderByCreatedAtDesc(UUID eventId);

    java.util.List<PaymentOrder> findByEventIdAndGiftId(
            UUID eventId, UUID giftId);

    boolean existsByEventIdAndGiftIdAndStatus(
            UUID eventId, UUID giftId, String status);
}

