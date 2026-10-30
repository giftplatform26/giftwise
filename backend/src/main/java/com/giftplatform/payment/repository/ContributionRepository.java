package com.giftplatform.payment.repository;

import com.giftplatform.payment.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ContributionRepository
        extends JpaRepository<Contribution, UUID> {
}
