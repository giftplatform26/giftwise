package com.giftplatform.qr.design;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InvitationDesignRepository extends JpaRepository<InvitationDesign,UUID> {
 List<InvitationDesign> findByEventIdOrderByIdAsc(UUID eventId);
}
