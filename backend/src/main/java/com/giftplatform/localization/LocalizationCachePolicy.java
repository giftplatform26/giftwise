package com.giftplatform.localization;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class LocalizationCachePolicy implements WebMvcConfigurer {
 @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor(){
  @Override public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response,Object handler){response.setHeader("Cache-Control","no-store");return true;}
 }).addPathPatterns("/api/admin/localization/**","/api/admin/localization","/api/auth/language","/api/localization/catalog","/api/public/localization/catalog");}
}
