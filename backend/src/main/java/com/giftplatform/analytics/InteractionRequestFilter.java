package com.giftplatform.analytics;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/** HTTP evidence is recorded independently of browser-reported click outcomes. */
public class InteractionRequestFilter extends OncePerRequestFilter {
 private final InteractionService service;
 private static final Pattern EVENT=Pattern.compile("/api/v1/events/([a-fA-F0-9-]{36})(?:/|$)");
 private static final Pattern TOKEN=Pattern.compile("/api/public/(?:events|celebrations|groups|payments/direct)/([a-fA-F0-9-]{36})(?:/|$)");
 public InteractionRequestFilter(InteractionService service){this.service=service;}
 private static UUID headerUuid(HttpServletRequest req,String name){try{return UUID.fromString(req.getHeader(name));}catch(Exception e){return null;}}
 public static String action(String method,String path){
  if(!path.startsWith("/api/")||path.contains("/interactions")||path.startsWith("/api/admin/")||path.equals("/api/auth/csrf")||path.endsWith("/logout"))return null;
  if(method.equals("POST")){
   if(path.equals("/api/auth/register"))return "HOST_REGISTER";
   if(path.equals("/api/v1/events"))return "EVENT_CREATE";
   if(path.matches("/api/v1/events/[a-fA-F0-9-]{36}/publish"))return "EVENT_PUBLISH";
   if(path.endsWith("/rsvp"))return "GUEST_RSVP";
  }
  if(path.matches("/api/public/events/[a-fA-F0-9-]{36}"))return "INVITATION_OPEN";
  if(path.contains("/qr/invitations/")&&path.endsWith("/png"))return "INVITATION_IMAGE_FETCH";
  if(path.endsWith("/qr/png"))return "QR_IMAGE_FETCH";
  if(path.endsWith("/calendar"))return "CALENDAR_DOWNLOAD_FETCH";
  if(path.matches(".*\\.(csv|xlsx|pdf|png)$"))return "FILE_DOWNLOAD_FETCH";
  if(path.contains("/member-")||path.contains("/members/"))return "MEMBER_REQUEST";
  if(path.contains("/payments/")||path.contains("/checkout")||path.endsWith("/confirm")||path.contains("/coordination/"))return "PAYMENT_REQUEST";
  if(!method.equals("GET")&&!method.equals("HEAD")&&!method.equals("OPTIONS"))return "API_"+method;
  return null;
 }
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  if(!"v2".equals(req.getHeader("X-Gift-Analytics-Consent"))){chain.doFilter(req,res);return;}
  String action=action(req.getMethod(),req.getRequestURI());if(action==null){chain.doFilter(req,res);return;}
  // Establish the anonymous visitor before the response can commit its headers.
  try{req.setAttribute("gift.analytics.visitor",InteractionService.visitor(req));}catch(RuntimeException e){logger.debug("Interaction session unavailable",e);}
  long start=System.nanoTime();boolean failed=false;
  try{chain.doFilter(req,res);}catch(ServletException|IOException|RuntimeException e){failed=true;throw e;}
  finally {
   try{var event=EVENT.matcher(req.getRequestURI());var token=TOKEN.matcher(req.getRequestURI());
    service.request(req,failed?500:res.getStatus(),(System.nanoTime()-start)/1000000,event.find()?UUID.fromString(event.group(1)):headerUuid(req,"X-Gift-Event-Id"),token.find()?UUID.fromString(token.group(1)):headerUuid(req,"X-Gift-Event-Token"),action);
   }catch(RuntimeException e){logger.debug("Interaction recording unavailable",e);}
  }
 }
}
