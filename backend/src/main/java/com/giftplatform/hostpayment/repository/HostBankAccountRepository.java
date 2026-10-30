package com.giftplatform.hostpayment.repository;
import com.giftplatform.hostpayment.entity.HostBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface HostBankAccountRepository extends JpaRepository<HostBankAccount,UUID> {
 List<HostBankAccount> findByHostIdOrderByCreatedAtAsc(UUID hostId);
 Optional<HostBankAccount> findByIdAndHostId(UUID id,UUID hostId);
 boolean existsByHostIdAndFingerprintAndArchivedAtIsNull(UUID hostId,String fingerprint);
 boolean existsByHostIdAndFingerprintAndEventIdAndArchivedAtIsNull(UUID hostId,String fingerprint,UUID eventId);
}
