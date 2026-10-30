package com.giftplatform.payment.hybrid;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class HybridCheckoutQuoteServiceTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final HybridCheckoutQuoteService service=new HybridCheckoutQuoteService(jdbc);
    @Test void invalidAmountRejected() {
        assertThrows(ResponseStatusException.class,()->service.quote(UUID.randomUUID(),null,0));
        assertThrows(ResponseStatusException.class,()->service.quote(UUID.randomUUID(),null,-1));
        assertThrows(ResponseStatusException.class,()->service.quote(UUID.randomUUID(),null,100_000_001L));
        verifyNoInteractions(jdbc);
    }
    @Test void missingEventRejected() {
        when(jdbc.queryForObject(anyString(),eq(Integer.class),any(),any(),any())).thenReturn(0);
        assertThrows(ResponseStatusException.class,()->service.quote(UUID.randomUUID(),null,500));
    }
    @Test void quotePreservesContributionWithoutSurcharge() {
        when(jdbc.queryForObject(anyString(),eq(Integer.class),any(),any(),any())).thenReturn(1);
        var q=service.quote(UUID.randomUUID(),null,200_000);
        assertEquals(200_000,q.totalPaise());
        assertEquals(200_000,q.contributionPaise());
        assertEquals(0,q.processingFeePaise());
        assertEquals("RAZORPAY_CHECKOUT",q.method());
        verify(jdbc).update(contains("INSERT INTO hybrid_checkout_quotes"),
                any(UUID.class),any(UUID.class),isNull(),eq(200_000L),eq(200_000L));
    }
}
