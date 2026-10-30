package com.giftplatform.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_ledger_entries")
public class WalletLedgerEntry {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "contribution_id")
    private UUID contributionId;

    @Column(name = "entry_group_id")
    private UUID entryGroupId;

    @Column(name = "account_code")
    private String accountCode;

    @Column(name = "amount_paise")
    private Long amountPaise;

    @Column(name = "currency")
    private String currency;

    @Column(name = "entry_type")
    private String entryType;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected WalletLedgerEntry() {}

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getContributionId() { return contributionId; }
    public UUID getEntryGroupId() { return entryGroupId; }
    public String getAccountCode() { return accountCode; }
    public Long getAmountPaise() { return amountPaise; }
    public String getCurrency() { return currency; }
    public String getEntryType() { return entryType; }
    public Instant getCreatedAt() { return createdAt; }
}
