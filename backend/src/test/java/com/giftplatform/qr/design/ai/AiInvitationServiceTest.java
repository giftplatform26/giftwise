package com.giftplatform.qr.design.ai;

import com.giftplatform.event.entity.*;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class AiInvitationServiceTest {
    private final EventRepository events=mock(EventRepository.class);private final ActorContext actor=mock(ActorContext.class);
    private final AiInvitationClient client=mock(AiInvitationClient.class);private final JdbcTemplate db=mock(JdbcTemplate.class);private final TransactionTemplate tx=mock(TransactionTemplate.class);
    private final AiInvitationService service=new AiInvitationService(events,actor,client,db,tx);
    private final UUID host=UUID.randomUUID();
    private Event event(UUID owner){var e=new Event("Garden party",OccasionType.BIRTHDAY);e.assignOwner(owner);e.setHostDisplayName("Jane");e.setEventContact("PRIVATE STREET","CITY","STATE","123456","India","Private Person","PRIVATE PHONE","private@example.test");when(events.findById(e.getId())).thenReturn(Optional.of(e));when(actor.requireAuthenticatedUserId()).thenReturn(host);return e;}
    private AiInvitationService.Preferences input(){return new AiInvitationService.Preferences("FLORAL","WARM","English","","Public Garden","6 PM","Soft pastel colours");}
    @Test void anotherHostsEventCannotCallAI(){var e=event(UUID.randomUUID());assertEquals(404,assertThrows(ResponseStatusException.class,()->service.generate(e.getId(),input())).getStatusCode().value());verifyNoInteractions(client,db,tx);}
    @Test void missingKeyDoesNotConsumeARequest(){var e=event(host);assertEquals(503,assertThrows(ResponseStatusException.class,()->service.generate(e.getId(),input())).getStatusCode().value());verifyNoInteractions(db,tx);}
    @Test void sendsOnlyPublicInvitationFieldsAndNeverSavesOrPublishesEvent(){
        var e=event(host);when(client.available()).thenReturn(true);
        doAnswer(inv->{Consumer<TransactionStatus> action=inv.getArgument(0);action.accept(mock(TransactionStatus.class));return null;}).when(tx).executeWithoutResult(any());
        when(client.generate(anyMap())).thenReturn(new AiInvitationClient.Blueprint("Garden","Join us","A lovely day","See you there","SAGE","AIRY","SERIF"));
        var result=service.generate(e.getId(),input());assertEquals("Public Garden",result.publicVenue());
        ArgumentCaptor<Map<String,Object>> data=ArgumentCaptor.forClass(Map.class);verify(client).generate(data.capture());
        String serialized=data.getValue().toString();assertFalse(serialized.contains("PRIVATE"));assertFalse(serialized.contains("private@example"));assertFalse(data.getValue().containsKey("publicToken"));assertEquals("Jane",data.getValue().get("hostNames"));assertEquals(EventStatus.DRAFT,e.getStatus());verify(events,never()).saveAndFlush(any());
    }

    @Test void contextPrefillsEventFactsAndVenueWithoutExposingContacts() {
        var e=event(host);e.update("Garden party","Come celebrate with us",OccasionType.BIRTHDAY,java.time.LocalDate.of(2027,2,14));
        var context=service.context(e.getId());
        assertEquals("Garden party",context.title());assertEquals("Jane",context.hostName());
        assertEquals("PLAYFUL",context.visualStyle());assertTrue(context.venue().contains("PRIVATE STREET"));
        assertEquals("Come celebrate with us",context.description());
        String serialized=new tools.jackson.databind.ObjectMapper().writeValueAsString(context);
        assertFalse(serialized.contains("PRIVATE PHONE"));assertFalse(serialized.contains("private@example"));assertFalse(serialized.contains("Private Person"));
        verifyNoInteractions(client,db,tx);
    }
    @Test void contextRejectsOtherHostsEvent() {
        var e=event(UUID.randomUUID());assertEquals(404,assertThrows(ResponseStatusException.class,()->service.context(e.getId())).getStatusCode().value());
        verifyNoInteractions(client,db,tx);
    }
}
