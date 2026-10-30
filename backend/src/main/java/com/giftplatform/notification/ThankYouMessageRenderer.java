package com.giftplatform.notification;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Text-only template substitution; HTML escape at the email rendering boundary. */
public final class ThankYouMessageRenderer {
  private static final Pattern TOKEN = Pattern.compile("\\{\\{(guestName|hostName|eventName|giftName|amount)\\}\\}");
  private ThankYouMessageRenderer() {}
  public static String render(String template, Map<String,String> values) {
    Objects.requireNonNull(template, "template");
    Objects.requireNonNull(values, "values");
    Matcher matcher = TOKEN.matcher(template);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String value = Objects.toString(values.get(matcher.group(1)), "");
      matcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(result);
    return result.toString();
  }
  public static String paymentWording(boolean captured) {
    return captured
        ? "Your contribution has been received. Thank you for sharing your love!"
        : "Thank you for sharing your love! Your transfer details have been submitted.";
  }
  public static String escapeHtml(String value) {
    if (value == null) return "";
    return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
        .replace("\"","&quot;").replace("'","&#39;");
  }
}
