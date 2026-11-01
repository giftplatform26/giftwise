package com.giftplatform.group;

import com.giftplatform.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class GroupGiftCompletionControllerTest {
    final JdbcTemplate db=mock(JdbcTemplate.class);
    final GroupContributionController groups=mock(GroupContributionController.class);
    final GroupGiftCompletionController controller=new GroupGiftCompletionController(db,groups,mock(EventRepository.class));
    final UUID token=UUID.randomUUID(),group=UUID.randomUUID();
    final MockHttpServletRequest request=new MockHttpServletRequest();
    void setup(long collected,String state){
        when(db.queryForMap(contains("FOR UPDATE"),eq(group))).thenReturn(new HashMap<>(Map.of("target_paise",10000L,"gift_name","Camera","fulfilment_status",state)));
        when(db.queryForObject(contains("collected_paise"),eq(Long.class),eq(group))).thenReturn(collected);
    }
    @Test void unconfirmedReportsCannotPublishGift(){
        setup(0,"PLANNED");
        assertEquals(409,assertThrows(ResponseStatusException.class,()->controller.publish(token,group,request)).getStatusCode().value());
        verify(db,never()).update(anyString(),eq(group));
    }
    @Test void fullyFundedGiftCanBePublished(){
        setup(10000,"PLANNED");
        assertEquals("100% Contributed ✅",controller.publish(token,group,request).get("displayStatus"));
        verify(groups).requireGroupCreator(token,group,request);
        verify(db).update(contains("gift_published_at=now()"),eq(group));
    }
    @Test void cannotShipBeforePurchaseOrSwitchToTransfer(){
        setup(10000,"PLANNED");
        assertThrows(ResponseStatusException.class,()->controller.fulfil(token,group,new GroupGiftCompletionController.Fulfilment(GroupGiftCompletionController.Status.TRACKING_SHARED,"Courier 123"),request));
        setup(10000,"GIFT_PURCHASED");
        assertThrows(ResponseStatusException.class,()->controller.fulfil(token,group,new GroupGiftCompletionController.Fulfilment(GroupGiftCompletionController.Status.AMOUNT_TRANSFERRED_TO_HOST,null),request));
    }
    @Test void trackingRequiresDetails(){
        setup(10000,"GIFT_PURCHASED");
        assertEquals(400,assertThrows(ResponseStatusException.class,()->controller.fulfil(token,group,new GroupGiftCompletionController.Fulfilment(GroupGiftCompletionController.Status.TRACKING_SHARED," "),request)).getStatusCode().value());
    }
    @Test void strangerCannotReadProgress(){
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED)).when(groups).member(group,request);
        assertThrows(ResponseStatusException.class,()->controller.progress(token,group,request));
        verifyNoInteractions(db);
    }
    @Test void unrelatedCreatorCannotPublishOrAcknowledgeReceipts(){
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)).when(groups).requireGroupCreator(token,group,request);
        assertThrows(ResponseStatusException.class,()->controller.publish(token,group,request));
        assertThrows(ResponseStatusException.class,()->controller.acknowledgeReceipt(token,group,UUID.randomUUID(),request));
        verifyNoInteractions(db);
    }
}
