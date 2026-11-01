package com.giftplatform.group;

import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GuestGiftControllerTest {
    final JdbcTemplate db=mock(JdbcTemplate.class);
    final GroupContributionController accounts=mock(GroupContributionController.class);
    final EventRepository events=mock(EventRepository.class);
    final ActorContext actors=mock(ActorContext.class);
    final GuestGiftController controller=new GuestGiftController(db,accounts,events,actors);
    final UUID token=UUID.randomUUID(),event=UUID.randomUUID(),creator=UUID.randomUUID(),gift=UUID.randomUUID();
    final MockHttpServletRequest request=new MockHttpServletRequest();
    void login(){when(accounts.event(token)).thenReturn(event);when(accounts.sessionAdmin(event,request)).thenReturn(creator);}
    @Test void losingAReservationReturnsConflict(){
        login();
        var ex=assertThrows(ResponseStatusException.class,()->controller.select(token,gift,new GuestGiftController.Selection(GuestGiftController.Type.INDIVIDUAL,null),request));
        assertEquals(409,ex.getStatusCode().value());
    }
    @Test void groupSelectionRequiresGroupOwnership(){
        login();UUID group=UUID.randomUUID();
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)).when(accounts).requireGroupCreator(token,group,request);
        assertThrows(ResponseStatusException.class,()->controller.select(token,gift,new GuestGiftController.Selection(GuestGiftController.Type.GROUP,group),request));
        verifyNoInteractions(db);
    }
    @Test void deliveryAddressCannotBeReadByAnotherGuest(){
        login();
        assertThrows(ResponseStatusException.class,()->controller.delivery(token,gift,request));
        verifyNoInteractions(events);
    }
    @Test void hostReportRequiresOwnership(){
        when(actors.requireAuthenticatedUserId()).thenReturn(creator);
        assertThrows(ResponseStatusException.class,()->controller.hostReport(event));
    }
    @Test void unauthenticatedGuestCannotAddGift(){
        when(accounts.event(token)).thenReturn(event);
        when(accounts.sessionAdmin(event,request)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        assertThrows(ResponseStatusException.class,()->controller.add(token,new GuestGiftController.NewGift("Coffee maker",null,null),request));
        verifyNoInteractions(db);
    }
}
