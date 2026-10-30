package com.giftplatform.hostpayment.verification;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Pattern;

/** Explicitly invoked, read-only HTTPS provider retrieval. Not registered as a Spring bean.
 * Confirm the provider endpoint and account entitlements before enabling any production caller.
 */
public final class RazorpayXAuthenticatedReader {
 private static final Pattern ID=Pattern.compile("fav_[A-Za-z0-9_-]{1,100}");
 private static final URI ORIGIN=URI.create("https://api.razorpay.com");
 private final HttpClient http;
 private final String keyId;
 private final String keySecret;
 public RazorpayXAuthenticatedReader(String keyId,String keySecret) {
  this(keyId,keySecret,HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
    .followRedirects(HttpClient.Redirect.NEVER).build());
 }
 RazorpayXAuthenticatedReader(String keyId,String keySecret,HttpClient http) {
  if(keyId==null||keyId.isBlank()||keySecret==null||keySecret.isBlank()||http==null)
   throw new IllegalArgumentException("RazorpayX credentials and transport required");
  if(keyId.contains(":")||keyId.contains("\n")||keySecret.contains("\n"))
   throw new IllegalArgumentException("Invalid credential format");
  this.keyId=keyId;this.keySecret=keySecret;this.http=http;
 }
 public String fetch(String requestReference) throws IOException, InterruptedException {
  if(requestReference==null||!ID.matcher(requestReference).matches())
   throw new IllegalArgumentException("Invalid validation reference");
  // Read-only endpoint. Never use a caller-controlled URL or follow redirects.
  URI uri=ORIGIN.resolve("/v1/fund_accounts/validations/"+requestReference);
  String basic=Base64.getEncoder().encodeToString((keyId+":"+keySecret).getBytes(StandardCharsets.UTF_8));
  HttpRequest req=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10))
   .header("Authorization","Basic "+basic).header("Accept","application/json").GET().build();
  HttpResponse<String> response=http.send(req,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  if(response.statusCode()!=200)throw new IOException("RazorpayX validation lookup failed (HTTP "+response.statusCode()+")");
  if(response.body()==null||response.body().isBlank()||response.body().length()>262144)
   throw new IOException("Invalid RazorpayX validation response");
  return response.body();
 }
}
