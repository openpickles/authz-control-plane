package org.openpickles.policy.engine.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpickles.policy.engine.model.AuditLog;
import org.openpickles.policy.engine.service.AuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditHandlers {

    private final AuditService auditService;

    @Bean
    public AuthenticationSuccessHandler auditAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            log.info("Login successful for user: {}", authentication.getName());
            auditService.log(AuditLog.builder()
                    .timestamp(java.time.Instant.now())
                    .actorUsername(authentication.getName())
                    .action("LOGIN")
                    .resourceType("USER")
                    .resourceId(authentication.getName())
                    .status("SUCCESS")
                    .clientIp(request.getRemoteAddr())
                    .userAgent(request.getHeader("User-Agent"))
                    .sessionId(request.getSession().getId())
                    .requestId(UUID.randomUUID().toString()));
            response.setStatus(HttpServletResponse.SC_OK);
            // If redirect is needed, one can use
            // SavedRequestAwareAuthenticationSuccessHandler
            // For now, we just return OK or redirect to root
            response.sendRedirect("/");
        };
    }

    @Bean
    public AuthenticationFailureHandler auditAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            log.warn("Login failed for user: {}", username);
            auditService.log(AuditLog.builder()
                    .timestamp(java.time.Instant.now())
                    .actorUsername(username != null ? username : "UNKNOWN")
                    .action("LOGIN")
                    .resourceType("USER")
                    .status("FAILURE")
                    .failureReason(exception.getMessage())
                    .clientIp(request.getRemoteAddr())
                    .userAgent(request.getHeader("User-Agent"))
                    .requestId(UUID.randomUUID().toString()));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
        };
    }

    @Bean
    public LogoutSuccessHandler auditLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                auditService.log(AuditLog.builder()
                        .timestamp(java.time.Instant.now())
                        .actorUsername(authentication.getName())
                        .action("LOGOUT")
                        .resourceType("USER")
                        .status("SUCCESS")
                        .clientIp(request.getRemoteAddr())
                        .userAgent(request.getHeader("User-Agent"))
                        .requestId(UUID.randomUUID().toString()));
            }
            response.sendRedirect("/login?logout");
        };
    }
}
