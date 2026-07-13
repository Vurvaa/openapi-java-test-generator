package org.example.openapitestgenerator.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* org.example.openapitestgenerator.service..*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* org.example.openapitestgenerator.infrastructure..*(..))")
    public void llmClientLayer() {}

    @Around("serviceLayer() || llmClientLayer()")
    public Object logDebug(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());

        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String method = joinPoint.getSignature().getName();
        log.debug("-> {}", method);

        boolean isCompleted = false;

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            isCompleted = true;
            log.debug("<- {} completed in {} ms", method, System.currentTimeMillis() - start);

            return result;
        } finally {
            log.debug("<- {} {} in {} ms", method, isCompleted ? "completed" : "failed",
                    System.currentTimeMillis() - start);
        }
    }
}