package org.example.openapitestgenerator.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.openapitestgenerator.conditions.DebugCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Conditional(DebugCondition.class)
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("""
            execution(* org.example.openapitestgenerator.service..*(..)) ||
                           execution(* org.example.openapitestgenerator.infrastructure..*(..))""")
    public void logServiceMethods() {
    }

    @Around("logServiceMethods()")
    public Object logMethodEntryAndExit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        logger.debug("Entering method [{}] with arguments: {}", joinPoint.getSignature(), Arrays.toString(args));

        Object result = joinPoint.proceed();

        logger.debug("Exiting method [{}] with result: {}", joinPoint.getSignature(), result);

        return result;
    }
}
