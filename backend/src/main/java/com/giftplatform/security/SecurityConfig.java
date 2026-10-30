
package com.giftplatform.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import jakarta.servlet.DispatcherType;
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            GiftUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new ChangeSessionIdAuthenticationStrategy();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository contextRepository,
            SessionAuthenticationStrategy sessionStrategy,
            com.giftplatform.security.recovery.CredentialSessionVersionStore versions
    ) throws Exception {

        http.addFilterBefore(new org.springframework.web.filter.OncePerRequestFilter() {
            @Override protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest req,
                    jakarta.servlet.http.HttpServletResponse res, jakarta.servlet.FilterChain chain)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && authentication.getPrincipal() instanceof GiftUserPrincipal principal) {
                    var session = req.getSession(false);
                    Object epoch = session == null ? null : session.getAttribute("gift.credential.epoch");
                    String kind = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")) ? "PLATFORM_ADMIN" : "HOST";
                    if (!(epoch instanceof Long v) || v.longValue() != versions.current(kind, principal.getUserId())) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        if (session != null) session.invalidate();
                        res.sendError(401, "Session expired. Please sign in again.");
                        return;
                    }
                }
                chain.doFilter(req, res);
            }
        }, org.springframework.security.web.access.intercept.AuthorizationFilter.class);
        http
                .securityContext(context -> context
                        .securityContextRepository(contextRepository)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                        .sessionAuthenticationStrategy(sessionStrategy)
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/webhooks/razorpay",
                                "/api/public/celebrations/*/rsvp",
                                "/api/public/celebrations/*/wishes",
                                 "/api/public/celebrations/*/wishes/photo"
                        )
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/recovery/request", "/api/auth/recovery/complete", "/api/auth/recovery/username/request", "/api/auth/signup-email/start", "/api/auth/signup-email/verify").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/recovery/enroll-email", "/api/auth/recovery/verify-email").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/auth/recovery/email-status").authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/csrf"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/public/events/*", "/api/public/events/*/features", "/api/public/events/*/gifts/*/photo", "/api/public/experience"
                        ).permitAll()
                         .requestMatchers(HttpMethod.GET,
                                 "/api/public/celebrations/*/wishes").permitAll()
                         .requestMatchers(HttpMethod.POST,
                                 "/api/public/celebrations/*/rsvp",
                                 "/api/public/celebrations/*/wishes",
                                  "/api/public/celebrations/*/wishes/photo").permitAll()
                          .requestMatchers(HttpMethod.GET,
                                  "/api/public/celebrations/*/wishes/*/photo").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/public/payments/direct/*/destinations").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/public/payments/direct/*/claims").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/public/payments/verified-destinations/*").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/public/payments/context/*",
                                "/api/public/gifts/progress/*"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/public/invitation-assets",
                                "/api/public/invitation-assets/*/image"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/public/payments/checkout",
                                "/api/public/payments/confirm"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/webhooks/razorpay"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me",
                                "/api/auth/profile-name"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/profile-name"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/phone/otp/request",
                                "/api/v1/phone/otp/verify"
                        ).authenticated()
                        .requestMatchers(
                                "/api/v1/events", "/api/v1/events/**"
                        ).authenticated()
                        .requestMatchers(
                                "/api/v1/host/notifications",
                                "/api/v1/host/notifications/**"
                        ).authenticated()
                        .requestMatchers(
                                "/api/v1/host/payment-profiles/**"
                         ).authenticated()
                         .requestMatchers(
                                 "/api/v1/host/settlement/**"
                        ).authenticated()
                        .requestMatchers(
                                "/api/admin/**", "/actuator/metrics", "/actuator/metrics/**"
                        ).hasRole("PLATFORM_ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/public/groups/*/register",
                                "/api/public/groups/*/login",
                                "/api/public/groups/*/join/*",
                                "/api/public/groups/*/member-login/*",
                                "/api/public/groups/*/member-logout/*").permitAll()
                        .requestMatchers("/api/public/groups/*/payments/*/destinations",
                                "/api/public/groups/*/payments/*/preferred",
                                "/api/public/groups/*/payments/*/destinations/*/enabled",
                                "/api/public/groups/*/payments/*/destinations/*/qr",
                                "/api/public/groups/*/payments/*/destinations/*/qr/upload",
                                "/api/public/groups/*/payments/*/destinations/*/qr/generate").permitAll()
                        // Group self-service routes: controller enforces the page-bound
                        // group session and current password where required.
                        // permitAll only bypasses platform-user authentication;
                        // CSRF protection still applies to POST requests.
                        .requestMatchers(HttpMethod.POST,
                                "/api/public/groups/*/profile-name",
                                "/api/public/groups/*/pin/setup",
                                "/api/public/groups/*/member-pin/setup/*").permitAll()
                        .requestMatchers("/api/public/groups/*/guest/*/invitation",
                                "/api/public/groups/*/guest/*/destination/*",
                                "/api/public/groups/*/guest/*/destination/*/qr",
                                "/api/public/groups/*/guest/*/report",
                                "/api/public/groups/*/guest/*/member-report",
                                "/api/public/groups/*/guest/*/member-history",
                                "/api/public/groups/*/guest/*/creator-report",
                                "/api/public/groups/*/guest/*/creator-history",
                                "/api/public/groups/*/guest/*/settings",
                                "/api/public/groups/*/guest/*/history").permitAll()
                        .requestMatchers("/api/public/groups/*/dashboard",
                                "/api/public/groups/*/setup",
                                "/api/public/groups/*/logout",
                                "/api/public/groups/*/privacy",
                                "/api/public/groups/*/join/*",
                                "/api/public/groups/*/member-view/*",
                                "/api/public/groups/*/members/*",
                                "/api/public/groups/*/members/*/review",
                                "/api/public/groups/*/publish/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/groups/*/account-policy").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/groups/*/contact-email/start", "/api/public/groups/*/contact-email/complete").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/public/groups/*/contact-email/status").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/public/groups/*/contact-mobile/start", "/api/public/groups/*/contact-mobile/complete").permitAll()
                        .anyRequest().denyAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, auth) -> {
                            response.setStatus(
                                    HttpServletResponse.SC_NO_CONTENT
                            );
                        })
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN
                                        )
                        )
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
