package com.giftplatform.hostpayment.repository;
import com.giftplatform.hostpayment.entity.HostUpiAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface HostUpiAccountRepository extends JpaRepository<HostUpiAccount,UUID> {
 List<HostUpiAccount> findByHostIdOrderByCreatedAtAsc(UUID hostId);
 Optional<HostUpiAccount> findByIdAndHostId(UUID id,UUID hostId);
 boolean existsByHostIdAndFingerprintAndArchivedAtIsNull(UUID hostId,String fingerprint);
 boolean existsByHostIdAndFingerprintAndEventIdAndArchivedAtIsNull(UUID hostId,String fingerprint,UUID eventId);
}
