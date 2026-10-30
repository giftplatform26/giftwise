
package com.giftplatform.otp.repository;

import com.giftplatform.otp.entity.PhoneOtpChallenge;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PhoneOtpChallengeRepository
        extends JpaRepository<PhoneOtpChallenge, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM PhoneOtpChallenge c
        WHERE c.id = :id
          AND c.userId = :userId
        """)
    Optional<PhoneOtpChallenge> findForVerification(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM PhoneOtpChallenge c
        WHERE c.userId = :userId
          AND c.consumedAt IS NULL
          AND c.deliveryStatus IN :statuses
        ORDER BY c.createdAt DESC
        """)
    java.util.List<PhoneOtpChallenge> findActiveForUpdate(
            @Param("userId") UUID userId,
            @Param("statuses")
            java.util.Collection<
                    com.giftplatform.otp.entity.OtpDeliveryStatus
                    > statuses
    );

    Optional<PhoneOtpChallenge>
    findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT c
    FROM PhoneOtpChallenge c
    WHERE c.userId = :userId
    ORDER BY c.createdAt DESC, c.id DESC
    """)
    List<PhoneOtpChallenge> findLatestForVerification(
            @Param("userId") UUID userId,
            Pageable pageable
    );

}
