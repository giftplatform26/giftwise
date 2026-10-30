package com.giftplatform.admin;

import com.giftplatform.event.repository.EventRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ModulePrivilegeWebConfiguration implements WebMvcConfigurer {
    private final GroupModuleInterceptor groups;
    public ModulePrivilegeWebConfiguration(EventRepository events,EffectiveModulePermissionService permissions) {
        groups=new GroupModuleInterceptor(events,permissions);
    }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(groups).addPathPatterns("/api/public/groups/**");
    }
}
