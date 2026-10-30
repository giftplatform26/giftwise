package com.giftplatform.hostpayment.qr;

import com.giftplatform.hostpayment.repository.HostUpiAccountRepository;
import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.hostpayment.entity.VerificationStatus;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class JpaRegisteredUpiResolver implements RegisteredUpiResolver {
    private final HostUpiAccountRepository accounts;
    private final PaymentDataProtector protector;
    public JpaRegisteredUpiResolver(HostUpiAccountRepository accounts, PaymentDataProtector protector) {
        this.accounts=accounts; this.protector=protector;
    }
    @Override public ResolvedUpi resolve(UUID hostId, UUID accountId) {
        var account=accounts.findByIdAndHostId(accountId,hostId)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"UPI account not found"));
        if (!account.isEnabled() || account.isArchived() || account.getVerificationStatus()==VerificationStatus.REVOKED)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"UPI account is unavailable");
        String upi=protector.decrypt(hostId,accountId,"upi_id",account.getEncrypted());
        return new ResolvedUpi(upi,"Gift Host",account.getVerificationStatus()==VerificationStatus.VERIFIED);
    }
}
