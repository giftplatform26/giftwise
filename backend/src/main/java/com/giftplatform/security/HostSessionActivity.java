package com.giftplatform.security;
import jakarta.servlet.http.HttpSession;

/** Automatic refresh requests must not extend a user's idle deadline. */
public final class HostSessionActivity {
 private static final String KEY="gift.host.lastActivity";
 private HostSessionActivity(){}
 public static boolean expired(HttpSession session,long now,boolean userActivity){
  Object previous=session.getAttribute(KEY);long last=previous instanceof Long value?value:now;
  int configured=session.getMaxInactiveInterval();long timeout=(configured>0?configured:1800)*1000L;
  if(now-last>=timeout)return true;
  if(!(previous instanceof Long)||userActivity)session.setAttribute(KEY,now);
  return false;
 }
 public static void start(HttpSession session){session.setAttribute(KEY,System.currentTimeMillis());}
}
