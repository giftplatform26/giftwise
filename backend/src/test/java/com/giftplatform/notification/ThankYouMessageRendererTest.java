package com.giftplatform.notification;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ThankYouMessageRendererTest {
  @Test void substitutesKnownVariablesAndPreservesUnknownOnes() {
    assertEquals("Dear Ana, from Family, {{other}}",
      ThankYouMessageRenderer.render("Dear {{guestName}}, from {{hostName}}, {{other}}",
        Map.of("guestName","Ana","hostName","Family")));
  }
  @Test void safelySubstitutesSpecialReplacementCharacters() {
    assertEquals("Hi $5\\guest",
      ThankYouMessageRenderer.render("Hi {{guestName}}",Map.of("guestName","$5\\guest")));
  }
  @Test void manualAcknowledgementNeverClaimsCapture() {
    String manual=ThankYouMessageRenderer.paymentWording(false);
    assertTrue(manual.contains("submitted"));
    assertFalse(manual.contains("received"));
    assertTrue(ThankYouMessageRenderer.paymentWording(true).contains("received"));
  }
  @Test void escapesHtmlForEmailCardRendering() {
    assertEquals("&lt;img src=&quot;x&quot;&gt;&amp;",
      ThankYouMessageRenderer.escapeHtml("<img src=\"x\">&"));
  }
  @Test void rejectsNullTemplate() {
    assertThrows(NullPointerException.class,
      () -> ThankYouMessageRenderer.render(null,Map.of()));
  }
}
