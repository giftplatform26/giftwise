package com.giftplatform.payment;

import com.giftplatform.payment.security.RazorpaySignatureVerifier;
import com.giftplatform.payment.service.RazorpayCaptureService;
import com.giftplatform.payment.service.RazorpayWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RazorpayWebhookServiceTest {
    private final RazorpaySignatureVerifier signatures = mock(RazorpaySignatureVerifier.class);
    private final RazorpayCaptureService capture = mock(RazorpayCaptureService.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TransactionTemplate tx = mock(TransactionTemplate.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final RazorpayWebhookService service = new RazorpayWebhookService(
            signatures, capture, jdbc, tx, mapper, "webhook-test-secret");

    @Test void rejectsMissingEventId() {
        assertThrows(ResponseStatusException.class,
                () -> service.receive("{}".getBytes(), "sig", null));
        verifyNoInteractions(capture, jdbc);
    }
    @Test void rejectsInvalidSignatureBeforeDatabaseAccess() {
        assertThrows(ResponseStatusException.class,
                () -> service.receive("{}".getBytes(), "bad", "evt_123"));
        verifyNoInteractions(capture, jdbc);
    }
    @Test void rejectsOversizedBodyBeforeDatabaseAccess() {
        assertThrows(ResponseStatusException.class,
                () -> service.receive(new byte[262145], "sig", "evt_123"));
        verifyNoInteractions(capture, jdbc);
    }
    @Test void rejectsMalformedCapturedPaymentBeforeDatabaseAccess() {
        String raw = "{\"event\":\"payment.captured\",\"payload\":{}}";
        when(signatures.verifyWebhook(raw, "sig", "webhook-test-secret")).thenReturn(true);
        assertThrows(ResponseStatusException.class,
                () -> service.receive(raw.getBytes(), "sig", "evt_123"));
        verifyNoInteractions(capture, jdbc);
    }
    @Test void rejectsMalformedJsonBeforeDatabaseAccess() {
        String raw = "not-json";
        when(signatures.verifyWebhook(raw, "sig", "webhook-test-secret")).thenReturn(true);
        assertThrows(ResponseStatusException.class,
                () -> service.receive(raw.getBytes(), "sig", "evt_123"));
        verifyNoInteractions(capture, jdbc);
    }
}
