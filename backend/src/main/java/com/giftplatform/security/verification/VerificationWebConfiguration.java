package com.giftplatform.security.verification;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/** The switches also guard direct API calls and old client routes. CSRF/authentication remain enforced. */
@Configuration
public class VerificationWebConfiguration implements WebMvcConfigurer {
 private final VerificationSettings settings;
 public VerificationWebConfiguration(VerificationSettings settings){this.settings=settings;}
 @Override public void addInterceptors(InterceptorRegistry registry){
  registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor(){
   @Override public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response,Object handler){
    String path=request.getRequestURI();
    if(path.contains("/contact-mobile/")||path.contains("/phone/otp/"))settings.requireMobile();
    else settings.requireEmail();
    response.setHeader("Cache-Control","no-store");return true;
   }
  }).addPathPatterns("/api/auth/signup-email/**","/api/auth/recovery/**","/api/auth/contact-mobile/**","/api/public/groups/*/contact-email/**","/api/public/groups/*/contact-mobile/**","/api/v1/phone/otp/**");
 }
}
