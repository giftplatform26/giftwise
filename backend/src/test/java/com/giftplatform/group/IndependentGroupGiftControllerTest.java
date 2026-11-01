package com.giftplatform.group;

import com.giftplatform.gift.retailer.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class IndependentGroupGiftControllerTest {
 final JdbcTemplate db=mock(JdbcTemplate.class);
 final GroupContributionController groups=mock(GroupContributionController.class);
 final RetailerPageClient client=mock(RetailerPageClient.class);
 final RetailerMetadataService metadata=mock(RetailerMetadataService.class);
 final IndependentGroupGiftController controller=new IndependentGroupGiftController(db,groups,client,metadata);
 final UUID token=UUID.randomUUID(),event=UUID.randomUUID(),creator=UUID.randomUUID();
 final MockHttpServletRequest request=new MockHttpServletRequest();
 void login(){when(groups.event(token)).thenReturn(event);when(groups.sessionAdmin(event,request)).thenReturn(creator);}
 @Test void productLookupRequiresCreatorSession(){
  when(groups.event(token)).thenReturn(event);when(groups.sessionAdmin(event,request)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
  assertThrows(ResponseStatusException.class,()->controller.lookup(token,new IndependentGroupGiftController.Link("https://shop.com/item"),request));
  verifyNoInteractions(client,metadata);
 }
 @Test void productMetadataIsReturnedWithoutCreatingAWishlistGift() throws Exception {
  login();String url="https://shop.com/item";when(client.fetch(url)).thenReturn("html");
  when(metadata.parse("html",url)).thenReturn(Map.of("name","Camera","description","For your adventures","targetAmount",new BigDecimal("5000"),"currency","INR"));
  var result=controller.lookup(token,new IndependentGroupGiftController.Link(url),request);
  assertEquals("Camera",result.get("name"));assertEquals(new BigDecimal("5000"),result.get("targetAmount"));verifyNoInteractions(db);
 }
 @Test void unsafeProductLinksAreRejectedBeforeFetching(){
  login();assertThrows(ResponseStatusException.class,()->controller.lookup(token,new IndependentGroupGiftController.Link("https://127.0.0.1/private"),request));
  verifyNoInteractions(client,metadata);
 }
}
