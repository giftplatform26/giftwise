package com.giftplatform.hostpayment.service;

import com.giftplatform.audit.service.AuditService;
import com.giftplatform.hostpayment.entity.*;
import com.giftplatform.hostpayment.repository.*;
import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.security.ActorContext;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.entity.UserRole;
import com.giftplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Isolated authorization tests; does not contact payment providers or enable the feature. */
class HostPaymentProfileAuthorizationTest {
    private final UUID hostId = UUID.randomUUID();
    private HostUpiAccountRepository upis;
    private HostBankAccountRepository banks;
    private PaymentDataProtector protector;
    private ActorContext actor;
    private UserRepository users;
    private AuditService audit;
    private HostPaymentProfileService service;
    private EventAccountLinkService links;

    @BeforeEach void setup() {
        upis = mock(HostUpiAccountRepository.class);
        banks = mock(HostBankAccountRepository.class);
        protector = mock(PaymentDataProtector.class);
        actor = mock(ActorContext.class);
        users = mock(UserRepository.class);
        audit = mock(AuditService.class);
        service = new HostPaymentProfileService(upis, banks, protector, actor, users, audit);
        links=mock(EventAccountLinkService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"eventLinks",links);
        when(actor.requireAuthenticatedUserId()).thenReturn(hostId);
        User user = mock(User.class);
        when(user.isEnabled()).thenReturn(true);
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(users.findById(hostId)).thenReturn(Optional.of(user));
    }

    @Test void missingUserCannotListDestinations() {
        when(users.findById(hostId)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, service::listUpis);
        verifyNoInteractions(upis);
    }

    @Test void anonymousActorCannotReadDestinations() {
        when(actor.requireAuthenticatedUserId()).thenThrow(new IllegalStateException("Not authenticated"));
        assertThrows(IllegalStateException.class, service::listBanks);
        verifyNoInteractions(banks);
    }

    @Test void crossHostUpiDisableIsNotAllowed() {
        UUID foreignId = UUID.randomUUID();
        when(upis.findByIdAndHostId(foreignId, hostId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.disableUpi(foreignId));
        verify(upis).findByIdAndHostId(foreignId, hostId);
        verify(upis, never()).findById(any());
        verifyNoInteractions(audit);
    }

    @Test void crossHostBankDisableIsNotAllowed() {
        UUID foreignId = UUID.randomUUID();
        when(banks.findByIdAndHostId(foreignId, hostId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.disableBank(foreignId));
        verify(banks).findByIdAndHostId(foreignId, hostId);
        verify(banks, never()).findById(any());
        verifyNoInteractions(audit);
    }

    @Test void duplicateUpiCannotBeInserted() {
        when(protector.fingerprint(eq("UPI"), anyString())).thenReturn("duplicate-fingerprint");
        when(upis.existsByHostIdAndFingerprintAndEventIdAndArchivedAtIsNull(hostId, "duplicate-fingerprint", null)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.addUpi("alice@examplebank"));
        verify(upis, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void pendingUpiCannotBePreferred() {
        UUID id = UUID.randomUUID();
        HostUpiAccount pending = new HostUpiAccount(id, hostId, "encrypted", "fingerprint", "a***@bank");
        when(users.findForPhoneEnrollment(hostId)).thenReturn(Optional.of(mock(User.class)));
        when(upis.findByIdAndHostId(id, hostId)).thenReturn(Optional.of(pending));
        when(upis.findByHostIdOrderByCreatedAtAsc(hostId)).thenReturn(List.of(pending));
        assertThrows(IllegalStateException.class, () -> service.preferUpi(id));
        assertFalse(pending.isPreferred());
        verifyNoInteractions(audit);
    }

    @Test void pendingBankCannotBePreferred() {
        UUID id = UUID.randomUUID();
        HostBankAccount pending = new HostBankAccount(id, hostId, "encrypted", "fingerprint", "****1234", "encrypted", "ABCD*******", "encrypted");
        when(users.findForPhoneEnrollment(hostId)).thenReturn(Optional.of(mock(User.class)));
        when(banks.findByIdAndHostId(id, hostId)).thenReturn(Optional.of(pending));
        when(banks.findByHostIdOrderByCreatedAtAsc(hostId)).thenReturn(List.of(pending));
        assertThrows(IllegalStateException.class, () -> service.preferBank(id));
        assertFalse(pending.isPreferred());
        verifyNoInteractions(audit);
    }

    @Test void accountCannotBeAssignedToAnotherHostsEvent() {
        var events = mock(com.giftplatform.event.repository.EventRepository.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"events",events);
        UUID eventId=UUID.randomUUID();
        var event=new com.giftplatform.event.entity.Event("Private",com.giftplatform.event.entity.OccasionType.BIRTHDAY);
        event.assignOwner(UUID.randomUUID());
        when(events.findById(eventId)).thenReturn(Optional.of(event));
        assertThrows(NoSuchElementException.class,()->service.addUpi("alice@bank",null,eventId));
        verifyNoInteractions(upis,banks,protector,audit);
    }

    @Test void preferredAccountChangesStayWithinTheirEvent() {
        UUID eventId=UUID.randomUUID();
        var shared=new HostBankAccount(UUID.randomUUID(),hostId,"enc","shared","****1234","enc","MASK","enc");
        shared.confirmByHost(); shared.prefer();
        var selected=new HostBankAccount(UUID.randomUUID(),hostId,"enc","event","****5678","enc","MASK","enc");
        selected.assignEvent(eventId); selected.confirmByHost();
        when(users.findForPhoneEnrollment(hostId)).thenReturn(Optional.of(mock(User.class)));
        when(banks.findByIdAndHostId(selected.getId(),hostId)).thenReturn(Optional.of(selected));
        when(banks.findByHostIdOrderByCreatedAtAsc(hostId)).thenReturn(List.of(shared,selected));
        when(links.banks(eventId)).thenReturn(List.of(new HostPaymentProfileService.BankView(selected.getId(),"masked","ifsc",VerificationStatus.PENDING,true,true,true,null,hostId,eventId)));
        assertEquals(eventId,service.preferBank(selected.getId()).eventId());
        verify(links).update(eventId,"BANK",selected.getId(),"preferred");
        assertTrue(shared.isPreferred());
    }

    @Test void receivingAccountListsSeparateHostAndEventMappings() {
        var events=mock(com.giftplatform.event.repository.EventRepository.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"events",events);
        var event=new com.giftplatform.event.entity.Event("Event",com.giftplatform.event.entity.OccasionType.BIRTHDAY);
        event.assignOwner(hostId);
        when(events.findById(event.getId())).thenReturn(Optional.of(event));
        var sharedUpi=new HostUpiAccount(UUID.randomUUID(),hostId,"enc","one","a***@bank");
        var eventUpi=new HostUpiAccount(UUID.randomUUID(),hostId,"enc","two","b***@bank");
        eventUpi.assignEvent(event.getId());
        var otherUpi=new HostUpiAccount(UUID.randomUUID(),hostId,"enc","three","c***@bank");
        otherUpi.assignEvent(UUID.randomUUID());
        var archivedUpi=new HostUpiAccount(UUID.randomUUID(),hostId,"enc","four","d***@bank");
        archivedUpi.assignEvent(event.getId()); archivedUpi.archive();
        when(upis.findByHostIdOrderByCreatedAtAsc(hostId)).thenReturn(List.of(sharedUpi,eventUpi,otherUpi,archivedUpi));
        assertEquals(List.of(sharedUpi.getId()),service.listUpis().stream().map(HostPaymentProfileService.UpiView::id).toList());
        service.listUpis(event.getId());verify(links).upis(event.getId());
        var sharedBank=new HostBankAccount(UUID.randomUUID(),hostId,"enc","one","****1234","enc","MASK","enc");
        var eventBank=new HostBankAccount(UUID.randomUUID(),hostId,"enc","two","****5678","enc","MASK","enc");
        eventBank.assignEvent(event.getId());
        var otherBank=new HostBankAccount(UUID.randomUUID(),hostId,"enc","three","****9012","enc","MASK","enc");
        otherBank.assignEvent(UUID.randomUUID());
        when(banks.findByHostIdOrderByCreatedAtAsc(hostId)).thenReturn(List.of(sharedBank,eventBank,otherBank));
        assertEquals(List.of(sharedBank.getId()),service.listBanks().stream().map(HostPaymentProfileService.BankView::id).toList());
        service.listBanks(event.getId());verify(links).banks(event.getId());
    }

    @Test void cannotListAccountsForAnotherHostsEvent() {
        var events=mock(com.giftplatform.event.repository.EventRepository.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"events",events);
        var event=new com.giftplatform.event.entity.Event("Private",com.giftplatform.event.entity.OccasionType.BIRTHDAY);
        event.assignOwner(UUID.randomUUID());
        when(events.findById(event.getId())).thenReturn(Optional.of(event));
        assertThrows(NoSuchElementException.class,()->service.listUpis(event.getId()));
        assertThrows(NoSuchElementException.class,()->service.listBanks(event.getId()));
        verifyNoInteractions(upis,banks);
    }
}
