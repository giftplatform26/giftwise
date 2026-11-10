package com.giftplatform.notification.email;

import com.giftplatform.branding.BrandingService;
import com.giftplatform.notification.ThankYouMessageRenderer;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class BrandedEmailService {
 private final BrandingService branding;private final ObjectMapper json;
 private final String origin,provider,key,from;
 private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
 public BrandedEmailService(BrandingService branding,ObjectMapper json,@Value("${gift.public-base-url:${gift.production.public-origin:}}") String origin,@Value("${gift.otp.email.provider:disabled}") String provider,@Value("${gift.otp.email.resend.api-key:}") String key,@Value("${gift.otp.email.from:}") String from){this.branding=branding;this.json=json;this.origin=origin.replaceAll("/+$","");this.provider=provider;this.key=key;this.from=from;}
 public boolean available(){try{URI uri=URI.create(origin);return "resend".equals(provider)&&!key.isBlank()&&!from.isBlank()&&"https".equalsIgnoreCase(uri.getScheme())&&uri.getHost()!=null&&uri.getUserInfo()==null&&(uri.getPath().isEmpty()||uri.getPath().equals("/"))&&uri.getQuery()==null&&uri.getFragment()==null;}catch(IllegalArgumentException ex){return false;}}
 public String url(String path){if(!path.startsWith("/")||path.startsWith("//")||path.contains("\\")||path.contains("\r")||path.contains("\n"))throw new IllegalArgumentException("Invalid email link.");return origin+path;}
 public record Email(String subject,String text,String html){}
 public static String substitute(String template,Map<String,String> values,boolean html){var matcher=Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9]*)}}").matcher(template);var output=new StringBuffer();while(matcher.find()){String value=values.get(matcher.group(1));if(value==null)throw new IllegalArgumentException("Missing email variable: "+matcher.group(1));matcher.appendReplacement(output,java.util.regex.Matcher.quoteReplacement(html?ThankYouMessageRenderer.escapeHtml(value):value));}matcher.appendTail(output);return output.toString();}
 public Email render(String template,Map<String,String> supplied,String language)throws java.io.IOException{
  if(!Set.of("registration","email-verification","password-reset","event-invitation","contribution-received","payment-success","event-completed").contains(template))throw new IllegalArgumentException("Unknown email template.");
  var brand=branding.get();Map<String,String> values=new HashMap<>(supplied);values.put("languageCode",language!=null&&language.matches("[A-Za-z0-9-]{2,63}")?language:"en");
  values.put("brandLogoUrl",url(brand.logoUrl().isEmpty()?"/giftwise-icon.svg":"/backend"+brand.logoUrl()));values.put("supportUrl",brand.supportEmail().isEmpty()?url("/contact"):"mailto:"+brand.supportEmail());values.put("privacyUrl",url("/privacy"));values.put("termsUrl",url("/terms"));
  for(var entry:values.entrySet())if(entry.getKey().endsWith("Url")&&!Set.of("brandLogoUrl","supportUrl","privacyUrl","termsUrl").contains(entry.getKey())){if(entry.getValue().startsWith("/")&&!entry.getValue().startsWith("//"))entry.setValue(url(entry.getValue()));URI link=URI.create(entry.getValue());if(!Objects.equals(link.getScheme(),URI.create(origin).getScheme())||!Objects.equals(link.getRawAuthority(),URI.create(origin).getRawAuthority())||link.getUserInfo()!=null)throw new IllegalArgumentException("Email action must use the configured public origin.");}
  String html=new ClassPathResource("email-templates/"+template+".html").getContentAsString(StandardCharsets.UTF_8);String text=new ClassPathResource("email-templates/"+template+".txt").getContentAsString(StandardCharsets.UTF_8);
  values.put("applicationName",brand.applicationName());values.put("footerText",brand.footerText());
  html=html.replace("© 2026 GiftWise","{{footerText}}").replace("GiftWise","{{applicationName}}").replace("#5b21b6",brand.brandColor());text=text.replace("© 2026 GiftWise","{{footerText}}").replace("GiftWise","{{applicationName}}");
  html=substitute(html,values,true);text=substitute(text,values,false);
  String subject=text.lines().skip(2).findFirst().orElse(brand.applicationName());return new Email(subject,text,html);
 }
 public Email security(String subject,String message){var brand=branding.get();String name=brand.applicationName();subject=subject.replace("GiftWise",name);String text=message+"\n\nContact Support: "+url("/contact")+"\nPrivacy Policy: "+url("/privacy")+"\nTerms & Conditions: "+url("/terms")+"\n"+brand.footerText();
  String logo=url(brand.logoUrl().isEmpty()?"/giftwise-icon.svg":"/backend"+brand.logoUrl());String support=brand.supportEmail().isEmpty()?url("/contact"):"mailto:"+brand.supportEmail();
  String html="<!doctype html><html><body style=\"font-family:Arial,sans-serif;max-width:600px;margin:24px auto;padding:24px;line-height:1.6\"><img width=\"144\" src=\""+esc(logo)+"\" alt=\""+esc(name)+"\"><h1>"+esc(subject)+"</h1><p>"+esc(message).replace("\n","<br>")+"</p><hr><p><a href=\""+esc(support)+"\">Contact Support</a> · <a href=\""+esc(url("/privacy"))+"\">Privacy Policy</a> · <a href=\""+esc(url("/terms"))+"\">Terms &amp; Conditions</a></p><p>"+esc(brand.footerText())+"</p></body></html>";return new Email(subject,text,html);
 }
 private String esc(String value){return ThankYouMessageRenderer.escapeHtml(value);}
 public void send(String recipient,Email email,String idempotencyKey){if(!available())throw new IllegalStateException("Configure email delivery and an HTTPS public website URL.");if(recipient==null||recipient.length()>254||!recipient.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("Invalid email recipient.");
  try{Map<String,Object> data=new LinkedHashMap<>();data.put("from",from);data.put("to",List.of(recipient));data.put("subject",email.subject());data.put("text",email.text());data.put("html",email.html());String payload=json.writeValueAsString(data);var request=HttpRequest.newBuilder(URI.create("https://api.resend.com/emails")).timeout(Duration.ofSeconds(12)).header("Authorization","Bearer "+key).header("Content-Type","application/json").header("Idempotency-Key",idempotencyKey).POST(HttpRequest.BodyPublishers.ofString(payload)).build();var response=client.send(request,HttpResponse.BodyHandlers.discarding());if(response.statusCode()<200||response.statusCode()>=300)throw new IllegalStateException("Email provider rejected delivery (HTTP "+response.statusCode()+").");
  }catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException("Email delivery interrupted.");}catch(java.io.IOException ex){throw new IllegalStateException("Unable to connect to the email provider.");}
 }
}
