package com.giftplatform.monetization;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PremiumTemplateAccessTest {
 @Test void disabledCheckoutDoesNotUnlockUnpurchasedPremiumArtwork(){
  var pricing=mock(MonetizationService.class);var access=spy(new CommercialAccessService(mock(JdbcTemplate.class),pricing));
  UUID host=UUID.randomUUID(),asset=UUID.randomUUID();
  doReturn(false).when(access).premium(host);doReturn(false).when(access).has("HOST",host,"PREMIUM_TEMPLATE",asset);
  assertFalse(access.template(host,asset));verifyNoInteractions(pricing);
  doReturn(true).when(access).has("HOST",host,"PREMIUM_TEMPLATE",asset);assertTrue(access.template(host,asset));
 }
}
