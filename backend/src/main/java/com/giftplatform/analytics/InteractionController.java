package com.giftplatform.analytics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.giftplatform.analytics.InteractionService.Input;

@RestController
public class InteractionController {
 private final InteractionService service;
 public InteractionController(InteractionService service){this.service=service;}
 public record Batch(@NotNull @Size(min=1,max=50) List<@NotNull @Valid Input> events){}
 @PostMapping("/api/public/interactions")
 public ResponseEntity<Void> collect(@Valid @RequestBody Batch batch,HttpServletRequest request){
  if(!"v2".equals(request.getHeader("X-Gift-Analytics-Consent")))return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
  // Bound writes per browser session. CSRF remains required for anonymous and signed-in visitors.
  var session=request.getSession();synchronized(session){
   long now=System.currentTimeMillis();Long start=(Long)session.getAttribute("analytics.window");
   int count=start==null||now-start>60000?0:(Integer)session.getAttribute("analytics.count");
   if(count+batch.events().size()>600)return ResponseEntity.status(429).build();
   if(count==0)session.setAttribute("analytics.window",now);session.setAttribute("analytics.count",count+batch.events().size());
  }
  for(var event:batch.events())service.browser(event,request);
  return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
 }
}
