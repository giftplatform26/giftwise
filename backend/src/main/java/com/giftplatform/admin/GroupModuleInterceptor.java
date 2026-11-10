package com.giftplatform.admin;

import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.UUID;

/** Applies the same module gate to creator, member, guest and payment group routes. */
public final class GroupModuleInterceptor implements HandlerInterceptor {
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;
    public GroupModuleInterceptor(EventRepository events, EffectiveModulePermissionService permissions) {
        this.events=events;this.permissions=permissions;
    }
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) {
        // Existing sessions must still be able to sign out after a module is disabled.
        String path=request.getRequestURI();
        if("POST".equals(request.getMethod())&&(path.endsWith("/logout")||path.contains("/member-logout/")))return true;
        Object attributes=request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if(!(attributes instanceof Map<?,?> variables)||!(variables.get("token") instanceof String token))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable");
        UUID id;
        try{id=UUID.fromString(token);}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND);}
        var event=events.findByPublicToken(id).filter(e->e.getStatus()==EventStatus.PUBLISHED)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(!event.isGiftingEnabled())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gifting is not enabled for this invitation");
        permissions.require(event.getOwnerId(),"GROUP_GIFTING");
        if(path.matches(".*/guest-gifts(?:/.*)?"))permissions.require(event.getOwnerId(),"GUEST_GIFTS");
        return true;
    }
}
