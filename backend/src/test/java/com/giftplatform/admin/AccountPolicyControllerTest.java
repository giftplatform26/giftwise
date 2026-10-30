package com.giftplatform.admin;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AccountPolicyControllerTest {
 @Test void rejectsNonAdmin(){var db=mock(JdbcTemplate.class);var actor=mock(ActorContext.class);var c=new AccountPolicyController(db,actor);assertThrows(ResponseStatusException.class,()->c.read("PLATFORM",new UUID(0,0)));verifyNoInteractions(db);}
 @Test void rejectsUnknownScopeBeforeQuery(){var db=mock(JdbcTemplate.class);var actor=mock(ActorContext.class);when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());var c=new AccountPolicyController(db,actor);assertThrows(ResponseStatusException.class,()->c.read("INVALID",UUID.randomUUID()));verifyNoInteractions(db);}
 @Test void rejectsMissingMandatoryVerification(){var db=mock(JdbcTemplate.class);var actor=mock(ActorContext.class);when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());var c=new AccountPolicyController(db,actor);assertThrows(ResponseStatusException.class,()->c.update("PLATFORM",new UUID(0,0),new AccountPolicyController.Policy(true,true,false,true,true)));verifyNoInteractions(db);}
}
