package com.giftplatform.payment.verification;

import com.giftplatform.security.ActorContext;
import com.giftplatform.payment.service.RazorpayCaptureService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class HostTransactionVerificationControllerTest {
    private final JdbcTemplate db=mock(JdbcTemplate.class);
    private final ActorContext actor=mock(ActorContext.class);
    private final RazorpayCaptureService capture=mock(RazorpayCaptureService.class);
    private final HostTransactionVerificationController api=new HostTransactionVerificationController(db,actor,capture);
    private final UUID host=UUID.randomUUID(),event=UUID.randomUUID(),order=UUID.randomUUID();
    @Test void anotherHostsEventCannotTriggerAProviderLookup(){
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        when(db.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(0);
        assertThrows(ResponseStatusException.class,()->api.verify(event,order));
        verifyNoInteractions(capture);
    }
    @Test void verificationUsesOnlyProviderIdentitiesFromTheOwnedStoredTransaction(){
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        when(db.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(1);
        doReturn(List.<String[]>of(new String[]{"order_stored","pay_stored"})).when(db).query(anyString(),any(RowMapper.class),eq(event),eq(order));
        api.verify(event,order);
        verify(capture).recheckRecordedPayment(order,"order_stored","pay_stored");
    }
    @Test void missingRecordedPaymentCannotBePromoted(){
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        when(db.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(1);
        doReturn(List.of()).when(db).query(anyString(),any(RowMapper.class),eq(event),eq(order));
        assertThrows(ResponseStatusException.class,()->api.verify(event,order));
        verifyNoInteractions(capture);
    }
}
