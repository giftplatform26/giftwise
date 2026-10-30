package com.giftplatform.hostpayment.verification.cashfree;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Sandbox-only transport. URLs are fixed by deployment configuration, never by request input. */
@Component
public class CashfreeSecureIdTransport {
  private final boolean enabled;
  private final String clientId, clientSecret;
  private final HttpClient client;
  public CashfreeSecureIdTransport(
      @Value("${gift.cashfree.secureid.sandbox.enabled:false}") boolean enabled,
      @Value("${gift.cashfree.secureid.client-id:}") String clientId,
      @Value("${gift.cashfree.secureid.client-secret:}") String clientSecret) {
    this.enabled=enabled; this.clientId=clientId; this.clientSecret=clientSecret;
    this.client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3))
        .followRedirects(HttpClient.Redirect.NEVER).build();
  }
  public boolean available() { return enabled && !clientId.isBlank() && !clientSecret.isBlank(); }
  /** Caller supplies only one of the two allowlisted, operator-reviewed sandbox API paths. */
  public int submitSandbox(String path, String json) {
    if (!available()) throw new IllegalStateException("Cashfree Secure ID sandbox not configured");
    if (!path.equals("/verification/bank-account/sync") && !path.equals("/verification/upi-penny-drop"))
      throw new IllegalArgumentException("Unsupported Secure ID route");
    if (json==null || json.length()>4096) throw new IllegalArgumentException("Invalid request size");
    try {
      var request=HttpRequest.newBuilder(URI.create("https://sandbox.cashfree.com"+path))
          .timeout(Duration.ofSeconds(8))
          .header("Content-Type","application/json")
          .header("x-client-id",clientId).header("x-client-secret",clientSecret)
          .POST(HttpRequest.BodyPublishers.ofString(json)).build();
      // Intentionally discard response body. Never trust unvalidated sandbox status as production proof.
      return client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Sandbox request interrupted"); }
      catch (java.io.IOException e) { throw new IllegalStateException("Sandbox verification unavailable"); }
  }
}
