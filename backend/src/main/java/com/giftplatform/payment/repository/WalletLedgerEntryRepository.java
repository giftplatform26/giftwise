package com.giftplatform.payment.repository;

import com.giftplatform.payment.entity.WalletLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WalletLedgerEntryRepository
        extends JpaRepository<WalletLedgerEntry, UUID> {
}
