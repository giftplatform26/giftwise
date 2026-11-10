package com.giftplatform.security.delivery;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import tools.jackson.databind.ObjectMapper;

/** SendOTP transport only. Local, account-bound challenges decide ownership. Never logs credentials or codes. */
public final class Msg91SmsDelivery implements MessageDelivery {
 private final HttpClient client; private final String authKey,templateId; private final int expiryMinutes;
 private java.util.function.Supplier<String> localizedTemplate;
 public Msg91SmsDelivery(String authKey,String templateId,int expiryMinutes,java.util.function.Supplier<String> resolver){this(authKey,templateId,expiryMinutes);this.localizedTemplate=resolver;}
 public Msg91SmsDelivery(String authKey,String templateId,int expiryMinutes){
  this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),authKey,templateId,expiryMinutes);
 }
 Msg91SmsDelivery(HttpClient client,String authKey,String templateId,int expiryMinutes){
  this.client=client;this.authKey=authKey;this.templateId=templateId;
  if(expiryMinutes<1||expiryMinutes>10)throw new IllegalArgumentException("SMS OTP expiry must be 1–10 minutes");
  this.expiryMinutes=expiryMinutes;
 }
 @Override public boolean isAvailable(){return authKey!=null&&!authKey.isBlank()&&!authKey.chars().anyMatch(Character::isISOControl)&&templateId!=null&&!templateId.isBlank();}
 @Override public boolean establishesMobileOwnership(){return isAvailable();}
 @Override public void send(String destination,String code){sendCode(destination,code);}
 @Override public void sendCode(String destination,String code){
  if(!isAvailable())throw new IllegalStateException("MSG91 is not configured");
  if(destination==null||!destination.matches("\\+[1-9][0-9]{7,14}")||code==null||!code.matches("[0-9]{6}"))throw new IllegalArgumentException("Invalid mobile or OTP");
  String query="template_id="+encode(localizedTemplate==null?templateId:localizedTemplate.get())+"&mobile="+encode(destination.substring(1))+"&otp="+code+"&otp_length=6&otp_expiry="+expiryMinutes;
  HttpRequest request=HttpRequest.newBuilder(URI.create("https://control.msg91.com/api/v5/otp?"+query))
    .timeout(Duration.ofSeconds(10)).header("authkey",authKey).header("Content-Type","application/json")
    .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
  try{
   var response=client.send(request,HttpResponse.BodyHandlers.ofString());
   if(response.statusCode()<200||response.statusCode()>=300||!accepted(response.body()))throw new IllegalStateException("MSG91 did not accept the verification request");
  }catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException("Mobile delivery interrupted");}
  catch(java.io.IOException ex){throw new IllegalStateException("Mobile delivery unavailable");}
 }
 static boolean accepted(String json){
  try{var node=new ObjectMapper().readTree(json);return "success".equals(node.path("type").asText())&&!node.path("message").asText().isBlank();}
  catch(RuntimeException ex){return false;}
 }
 private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
}
