package org.openpickles.policy.engine.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.openpickles.policy.engine.model.AuditLog;
import org.openpickles.policy.engine.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String failureReason = null;
        Object result = null;
        String oldValues = null;
        String newValues = null;
        String resourceId = null;

        // Capture request context ASAP
        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
                .action(auditable.action())
                .resourceType(auditable.resourceType());

        populateActorAndRequestDetails(logBuilder);

        try {
            // TODO: Retrieve 'oldValues' here for UPDATE actions if necessary by querying
            // DB logic

            result = joinPoint.proceed();

            // Capture 'newValues' or Resource ID from result
            if (result != null) {
                try {
                    newValues = objectMapper.writeValueAsString(result);
                    // Attempt to extract an ID if generic
                    // extracting ID genericly is hard without enforcement;
                    // usually we rely on Return Type having 'getId()' or similar.
                } catch (Exception e) {
                    newValues = "Could not serialize result: " + e.getMessage();
                }
            }

            return result;
        } catch (Throwable ex) {
            status = "FAILURE";
            failureReason = ex.getMessage();
            throw ex;
        } finally {
            logBuilder.status(status)
                    .failureReason(failureReason)
                    .resourceId(resourceId) // If we extracted it
                    .newValues(newValues);

            auditService.log(logBuilder);
        }
    }

    private void populateActorAndRequestDetails(AuditLog.AuditLogBuilder builder) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                builder.actorUsername(auth.getName());
                // builder.actorUserId(...) if using custom principal
            } else {
                builder.actorUsername("ANONYMOUS");
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                builder.clientIp(request.getRemoteAddr());
                builder.userAgent(request.getHeader("User-Agent"));
                builder.sessionId(request.getSession().getId());
                String requestId = (String) request
                        .getAttribute(org.openpickles.policy.engine.config.RequestIdFilter.REQUEST_ID_ATTRIBUTE);
                builder.requestId(requestId != null ? requestId : UUID.randomUUID().toString());
            }
        } catch (Exception e) {
            log.warn("Could not capture full audit context", e);
        }
    }
}
