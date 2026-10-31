package com.giftplatform.security.verification;

import com.giftplatform.security.ActorContext;
import com.giftplatform.security.delivery.MessageDelivery;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class VerificationSettingsControllerTest {
 final JdbcTemplate db=mock(JdbcTemplate.class);
 final ActorContext actor=mock(ActorContext.class);
 final VerificationSettings settings=mock(VerificationSettings.class);
 final MessageDelivery sms=mock(MessageDelivery.class),email=mock(MessageDelivery.class);
 final VerificationSettingsController controller=new VerificationSettingsController(settings,db,actor,sms,email,"");
 @Test void defaultsHideBothChannels(){var defaults=new VerificationSettings(db,false,false).current();assertThat(defaults.mobileOtpEnabled()).isFalse();assertThat(defaults.emailVerificationEnabled()).isFalse();}
 @Test void hostCannotChangeGlobalVerification(){assertThatThrownBy(()->controller.update(new VerificationSettings.Settings(false,false))).isInstanceOf(ResponseStatusException.class).hasMessageContaining("403");verifyNoInteractions(db);}
 @Test void missingProviderPreventsEnablement(){when(actor.isPlatformAdmin()).thenReturn(true);when(settings.current()).thenReturn(new VerificationSettings.Settings(false,false));assertThatThrownBy(()->controller.update(new VerificationSettings.Settings(true,false))).isInstanceOf(ResponseStatusException.class).hasMessageContaining("Configure MSG91");verifyNoInteractions(db);}
 @Test void mobileSetupFailureIsVisibleToTheBrowser() throws Exception {
  when(actor.isPlatformAdmin()).thenReturn(true);when(settings.current()).thenReturn(new VerificationSettings.Settings(false,false));
  MockMvcBuilders.standaloneSetup(controller).build().perform(put("/api/admin/verification-settings")
    .contentType(MediaType.APPLICATION_JSON).content("{\"mobileOtpEnabled\":true,\"emailVerificationEnabled\":false}"))
    .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("Configure MSG91 credentials, template and OTP signing key before enabling mobile verification"));
  verifyNoInteractions(db);
 }
 @Test void emailSetupFailureIsVisibleToTheBrowser() throws Exception {
  when(actor.isPlatformAdmin()).thenReturn(true);when(settings.current()).thenReturn(new VerificationSettings.Settings(false,false));
  MockMvcBuilders.standaloneSetup(controller).build().perform(put("/api/admin/verification-settings")
    .contentType(MediaType.APPLICATION_JSON).content("{\"mobileOtpEnabled\":false,\"emailVerificationEnabled\":true}"))
    .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("Configure the email provider, sender and OTP signing key before enabling email verification"));
  verifyNoInteractions(db);
 }
 @Test void configuredChannelsCanBeEnabledAndAudited(){
  UUID admin=UUID.randomUUID();when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(admin);
  when(settings.current()).thenReturn(new VerificationSettings.Settings(false,false),new VerificationSettings.Settings(true,true));
  when(sms.isAvailable()).thenReturn(true);when(sms.establishesMobileOwnership()).thenReturn(true);when(email.isAvailable()).thenReturn(true);
  var configured=new VerificationSettingsController(settings,db,actor,sms,email,Base64.getEncoder().encodeToString(new byte[32]));
  var result=configured.update(new VerificationSettings.Settings(true,true));
  assertThat(result.get("settings")).isEqualTo(new VerificationSettings.Settings(true,true));
  verify(db).update(contains("INSERT INTO platform_verification_settings"),eq(true),eq(true),eq(admin));
  verify(db).update(contains("platform_admin_action_audit"),any(UUID.class),eq(admin),eq("VERIFICATION_MOBILE_true_EMAIL_true"));
 }
 @Test void outageBypassIsAllowedEvenWhenOtherChannelHasLostConfiguration(){
  UUID admin=UUID.randomUUID();when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(admin);when(settings.current()).thenReturn(new VerificationSettings.Settings(true,true));
  controller.update(new VerificationSettings.Settings(true,false));
  verify(db).update(contains("INSERT INTO platform_verification_settings"),eq(true),eq(false),eq(admin));
  verify(db).update(contains("platform_admin_action_audit"),any(UUID.class),eq(admin),anyString());
 }
 @Test void disabledChannelsRejectDirectCalls(){var disabled=new VerificationSettings(db,false,false);assertThatThrownBy(disabled::requireMobile).isInstanceOf(ResponseStatusException.class);assertThatThrownBy(disabled::requireEmail).isInstanceOf(ResponseStatusException.class);}
}
