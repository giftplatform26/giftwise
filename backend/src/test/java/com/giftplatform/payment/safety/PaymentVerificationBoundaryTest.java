package com.giftplatform.payment.safety;

import com.giftplatform.hostpayment.entity.HostBankAccount;
import com.giftplatform.hostpayment.entity.HostUpiAccount;
import com.giftplatform.hostpayment.entity.VerificationStatus;
import com.giftplatform.payment.direct.GuestDirectTransferService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentVerificationBoundaryTest {
    private final UUID host = UUID.randomUUID();

    @Test
    void hostConfirmationDoesNotVerifyUpi() {
        HostUpiAccount account = new HostUpiAccount(UUID.randomUUID(), host,
                "encrypted-upi", "fingerprint", "ab***@bank");
        account.confirmByHost();
        assertNotNull(account.getHostConfirmedAt());
        assertEquals(VerificationStatus.PENDING, account.getVerificationStatus());
        account.prefer();
        assertTrue(account.isPreferred(), "Host confirmation may allow manual destination preference");
        assertEquals(VerificationStatus.PENDING, account.getVerificationStatus(),
                "Host confirmation must never imply independent provider verification");
    }

    @Test
    void hostConfirmationDoesNotVerifyBank() {
        HostBankAccount account = new HostBankAccount(UUID.randomUUID(), host,
                "encrypted-account", "fingerprint", "****1234",
                "encrypted-ifsc", "ABCD*******", "encrypted-holder");
        account.confirmByHost();
        assertNotNull(account.getHostConfirmedAt());
        assertEquals(VerificationStatus.PENDING, account.getVerificationStatus());
        account.prefer();
        assertTrue(account.isPreferred(), "Host confirmation may allow manual destination preference");
        assertEquals(VerificationStatus.PENDING, account.getVerificationStatus(),
                "Host confirmation must never imply independent provider verification");
    }

    @Test
    void disabledDirectTransferCannotReadDestinations() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        GuestDirectTransferService service = new GuestDirectTransferService(jdbc, false, true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.destinations(UUID.randomUUID()));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        verifyNoInteractions(jdbc);
    }

    @Test
    void disabledReconciliationAllowsManualReportingButDoesNotPostMoney() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        GuestDirectTransferService service = new GuestDirectTransferService(jdbc, true, false);
        GuestDirectTransferService.ClaimRequest claim =
                new GuestDirectTransferService.ClaimRequest(UUID.randomUUID(), "UPI", null,
                        1000, "Guest", null, null, "TESTREF12345", "idempotency-12345");
        // The mocked database has no published event. A 404 proves that the manual
        // route passed the feature gate without creating a payment or ledger entry.
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.claim(UUID.randomUUID(), claim));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(jdbc, atLeastOnce()).query(anyString(),
                any(org.springframework.jdbc.core.RowMapper.class), any());
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }
}
