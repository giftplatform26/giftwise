package com.giftplatform.notification.email;
import com.giftplatform.branding.BrandingService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class BrandedEmailServiceTest {
 private BrandedEmailService service(){var brand=mock(BrandingService.class);when(brand.get()).thenReturn(new BrandingService.Settings("Example & Co","#112233","© Example & Co","support@example.com","","/api/public/branding/assets/00000000-0000-0000-0000-000000000001","",2));return new BrandedEmailService(brand,new ObjectMapper(),"https://example.com","resend","key","sender@example.com");}
 @Test void escapesUserContentWithoutChangingPlainText()throws Exception{var message=service().render("event-invitation",Map.of("eventName","<script>unsafe</script>","hostName","A & B","eventDate","Tomorrow","invitationUrl","https://example.com/e/test"),"en");assertThat(message.html()).contains("&lt;script&gt;unsafe&lt;/script&gt;","Example &amp; Co","/backend/api/public/branding/assets/").doesNotContain("<script>");assertThat(message.text()).contains("<script>unsafe</script>");}
 @Test void rejectsExternalActionLinks(){assertThatThrownBy(()->service().render("registration",Map.of("signInUrl","https://evil.example/login"),"en")).hasMessageContaining("public origin");}
 @Test void rendersAllRequiredTemplates()throws Exception{var values=Map.ofEntries(Map.entry("signInUrl","/login"),Map.entry("verificationCode","123456"),Map.entry("expiresInMinutes","10"),Map.entry("resetUrl","/forgot-password"),Map.entry("hostName","Host"),Map.entry("eventName","Party"),Map.entry("eventDate","Tomorrow"),Map.entry("invitationUrl","/e/token"),Map.entry("formattedAmount","INR 10.00"),Map.entry("transactionReference","reference"),Map.entry("contributionUrl","/events/id/contributions"),Map.entry("recipientName","Host"),Map.entry("paymentUrl","/e/token"),Map.entry("eventUrl","/events/id"));for(String template:new String[]{"registration","email-verification","password-reset","event-invitation","contribution-received","payment-success","event-completed"})assertThat(service().render(template,values,"en").html()).doesNotContain("{{").contains("Contact Support","Privacy Policy","Terms &amp; Conditions");}
 @Test void securityEmailUsesCurrentBrand(){var result=service().security("GiftWise security code","Code <123456>");assertThat(result.subject()).contains("Example & Co");assertThat(result.html()).contains("Code &lt;123456&gt;","© Example &amp; Co");}
}
