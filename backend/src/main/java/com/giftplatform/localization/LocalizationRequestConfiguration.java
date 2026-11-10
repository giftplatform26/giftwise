package com.giftplatform.localization;

import com.giftplatform.security.GiftUserPrincipal;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import jakarta.servlet.http.*;
import java.util.*;

@Configuration
public class LocalizationRequestConfiguration implements WebMvcConfigurer {
    private final LocalizationService service;
    public LocalizationRequestConfiguration(org.springframework.beans.factory.ObjectProvider<LocalizationService> service){this.service=service.getIfAvailable();}
    @Override public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new HandlerInterceptor(){
            @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
                if(service==null)return true;
                request.setAttribute("gift.previous.locale",LocaleContextHolder.getLocaleContext());
                if("POST".equals(request.getMethod())&&(request.getRequestURI().endsWith("/logout")||request.getRequestURI().contains("/member-logout/")))return true;
                var configuration=service.settings();
                if(!configuration.enabled()){LocaleContextHolder.setLocale(Locale.forLanguageTag(configuration.defaultLanguage()));return true;}
                UUID host=null,event=null;boolean admin=false;String requested=null;
                boolean publicPath=request.getRequestURI().startsWith("/api/public/");
                var authentication=SecurityContextHolder.getContext().getAuthentication();
                if(!publicPath&&authentication!=null&&authentication.getPrincipal() instanceof GiftUserPrincipal principal){host=principal.getUserId();admin=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));}
                if(publicPath){Object variables=request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);if(variables instanceof Map<?,?> map&&map.get("token") instanceof String token){try{UUID[] scope=service.publicEvent(UUID.fromString(token));host=scope[0];event=scope[1];}catch(IllegalArgumentException ignored){}}}
                if(publicPath||host==null){String header=request.getHeader("X-Gift-Language");if(header!=null&&header.length()<=63)requested=header;if(requested==null&&request.getCookies()!=null)for(Cookie cookie:request.getCookies())if(cookie.getName().equals("gift.language")&&cookie.getValue().length()<=63)requested=cookie.getValue();}
                String selected=service.resolve(host,event,requested,admin);LocaleContextHolder.setLocale(Locale.forLanguageTag(selected));return true;
            }
            @Override public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception exception){if(service!=null)LocaleContextHolder.setLocaleContext((org.springframework.context.i18n.LocaleContext)request.getAttribute("gift.previous.locale"));}
        }).addPathPatterns("/api/auth/**","/api/public/events/**","/api/public/celebrations/**","/api/public/groups/**","/api/public/payments/direct/**","/api/v1/**")
          .excludePathPatterns("/api/auth/csrf","/api/public/events/*/gifts/*/photo","/api/public/celebrations/*/wishes/*/photo","/api/v1/events/*/album/*/photo");
    }
}
