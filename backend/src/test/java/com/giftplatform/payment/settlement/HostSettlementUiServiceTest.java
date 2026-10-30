package com.giftplatform.payment.settlement;

import com.giftplatform.audit.service.AuditService;
import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HostSettlementUiServiceTest {
    ActorContext actor=mock(ActorContext.class);
    JdbcTemplate jdbc=mock(JdbcTemplate.class);
    SettlementBalanceService balances=mock(SettlementBalanceService.class);
    HostPayoutRequestService requests=mock(HostPayoutRequestService.class);
    PayoutSafetyFinalizationService safety=mock(PayoutSafetyFinalizationService.class);
    AuditService audit=mock(AuditService.class);
    HostSettlementUiService service=new HostSettlementUiService(actor,jdbc,balances,requests,safety,audit);
    UUID host=UUID.randomUUID(), event=UUID.randomUUID(), bank=UUID.randomUUID(), request=UUID.randomUUID();
    @BeforeEach void owner(){when(actor.requireAuthenticatedUserId()).thenReturn(host);}
    @Test void unauthorizedEventIsNotFoundAndBalanceNotRead(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(0);
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,()->service.wallet(event)).getStatusCode());
        verifyNoInteractions(balances);
    }
    @Test void pendingMoneyIsNotPresentedAsWithdrawable(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(1);
        when(balances.read(event)).thenReturn(new SettlementBalanceService.Balance(0,0,0));
        var w=service.wallet(event);
        assertEquals(0,w.withdrawablePaise()); assertFalse(w.livePayoutsEnabled());
    }
    @Test void blockedRequestUsesAuthenticatedActorAndAudits(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(1);
        when(requests.request(host,event,bank,600,"k")).thenReturn(new HostPayoutRequestService.Result(request,"BLOCKED","LIVE_PAYOUTS_DISABLED"));
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(request))).thenReturn(0);
        assertEquals("BLOCKED",service.request(event,bank,600,"k").state());
        verify(requests).request(host,event,bank,600,"k");
        verify(audit).recordChange(eq(host),any(),eq("HOST_PAYOUT_REQUEST"),eq(request),any(),isNull(),anyMap(),isNull());
    }
    @Test void cancellationCannotCrossEvents(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(1);
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(request),eq(host),eq(event))).thenReturn(0);
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,()->service.cancel(event,request)).getStatusCode());
        verifyNoInteractions(safety);
    }
    @Test void replayedCancellationDoesNotAuditAgain(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(1);
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(request),eq(host),eq(event))).thenReturn(1);
        when(safety.cancel(host,request)).thenReturn(new PayoutSafetyFinalizationService.Cancellation(request,true,0));
        assertTrue(service.cancel(event,request).alreadyCancelled());
        verifyNoInteractions(audit);
    }
    @Test void requestIsNotCalledForOtherHostEvent(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(event),eq(host))).thenReturn(0);
        assertThrows(ResponseStatusException.class,()->service.request(event,bank,600,"k"));
        verifyNoInteractions(requests,audit);
    }
}
