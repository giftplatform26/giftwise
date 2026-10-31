package com.giftplatform.security.delivery;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.net.http.*;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class Msg91SmsDeliveryTest {
 @Test void http200ProviderErrorsAreNotSuccessfulDelivery(){assertThat(Msg91SmsDelivery.accepted("{\"type\":\"error\",\"message\":\"Invalid authkey\"}")).isFalse();assertThat(Msg91SmsDelivery.accepted("not-json")).isFalse();assertThat(Msg91SmsDelivery.accepted("{\"type\":\"success\"}")).isFalse();assertThat(Msg91SmsDelivery.accepted("{\"type\":\"success\",\"message\":\"request-id\"}")).isTrue();}
 @Test void noCredentialsMeansUnavailable(){var delivery=new Msg91SmsDelivery("","",5);assertThat(delivery.isAvailable()).isFalse();assertThat(delivery.establishesMobileOwnership()).isFalse();}
 @SuppressWarnings("unchecked") @Test void sendsAccountGeneratedCodeWithSecretInHeaderOnly() throws Exception {
  HttpClient client=mock(HttpClient.class);HttpResponse<String> response=mock(HttpResponse.class);
  when(response.statusCode()).thenReturn(200);when(response.body()).thenReturn("{\"type\":\"success\",\"message\":\"request-id\"}");
  when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(response);
  new Msg91SmsDelivery(client,"test-auth-key","template-id",5).sendCode("+919876543210","123456");
  var request=ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
  assertThat(request.getValue().uri().getQuery()).contains("mobile=919876543210","otp=123456","otp_expiry=5").doesNotContain("test-auth-key");
  assertThat(request.getValue().headers().firstValue("authkey")).contains("test-auth-key");
 }
 @SuppressWarnings("unchecked") @Test void transportFailureNeverEchoesProviderSecrets() throws Exception {
  HttpClient client=mock(HttpClient.class);when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("secret url details"));
  assertThatThrownBy(()->new Msg91SmsDelivery(client,"secret-key","template",5).sendCode("+919876543210","123456")).hasMessage("Mobile delivery unavailable").hasNoCause();
 }
}
