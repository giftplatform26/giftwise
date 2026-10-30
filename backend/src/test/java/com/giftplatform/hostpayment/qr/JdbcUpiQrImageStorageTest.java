package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcUpiQrImageStorageTest {
    @Test void uploadedReadsWithBothHostAndAccount() {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        UUID host=UUID.randomUUID(),account=UUID.randomUUID();
        when(jdbc.query(anyString(),any(org.springframework.jdbc.core.RowMapper.class),eq(host),eq(account)))
            .thenReturn(List.of(new byte[]{1,2}));
        assertArrayEquals(new byte[]{1,2},new JdbcUpiQrImageStorage(jdbc).getUploaded(host,account));
    }
    @Test void missingUploadedImageReturns404() {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        when(jdbc.query(anyString(),any(org.springframework.jdbc.core.RowMapper.class),any(UUID.class),any(UUID.class)))
            .thenReturn(List.of());
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,
            ()->new JdbcUpiQrImageStorage(jdbc).getUploaded(UUID.randomUUID(),UUID.randomUUID())).getStatusCode());
    }
}
