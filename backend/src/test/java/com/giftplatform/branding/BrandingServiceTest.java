package com.giftplatform.branding;
import com.giftplatform.security.ActorContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
class BrandingServiceTest {
 private BrandingService.Update input(String color,String email,String social){return new BrandingService.Update("GiftWise",color,"© 2026 GiftWise",email,social,0);}
 @Test void rejectsInvalidColorEmailAndUnsafeLinks(){
  assertThatThrownBy(()->BrandingService.validate(input("red","",""))).hasMessageContaining("color");
  assertThatThrownBy(()->BrandingService.validate(input("#5b21b6","bad",""))).hasMessageContaining("email");
  for(String link:new String[]{"javascript:alert(1)","http://example.com","https://user@example.com","https:///broken"})assertThatThrownBy(()->BrandingService.validate(input("#5b21b6","",link))).hasMessageContaining("HTTPS");
  assertThatCode(()->BrandingService.validate(input("#5b21b6","help@example.com","https://example.com/community"))).doesNotThrowAnyException();
 }
 @Test void nonAdminCannotReadOrMutateAdminSettings(){var actor=mock(ActorContext.class);var db=mock(JdbcTemplate.class);var service=new BrandingService(db,actor);
  assertThatThrownBy(service::admin).isInstanceOf(ResponseStatusException.class);
  assertThatThrownBy(()->service.save(input("#5b21b6","",""))).isInstanceOf(ResponseStatusException.class);
  assertThatThrownBy(()->service.upload("logo",new byte[0],0)).isInstanceOf(ResponseStatusException.class);verifyNoInteractions(db);
 }
 @Test void staleUpdateIsRejected(){var actor=mock(ActorContext.class);when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());var service=new BrandingService(mock(JdbcTemplate.class),actor);assertThatThrownBy(()->service.save(input("#5b21b6","",""))).isInstanceOf(ResponseStatusException.class).hasMessageContaining("another session");}
}
