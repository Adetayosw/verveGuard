package com.adetayo.verve_guard.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("@annotation(com.adetayo.verve_guard.aop.Loggable)")
    public void loggableMethods() {
        // Pointcut for @Loggable
    }

    @Before("loggableMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[ENTRY] {}.{} | args: {}", className, methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "loggableMethods()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String returnSummary = result == null ? "null" : result.toString();
        if (returnSummary.length() > 500) {
            returnSummary = returnSummary.substring(0, 500) + "...";
        }

        log.info("[EXIT]  {}.{} | returned: {}", className, methodName, returnSummary);
    }

    @AfterThrowing(pointcut = "loggableMethods()", throwing = "ex")
    public void logMethodError(JoinPoint joinPoint, Throwable ex) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[ERROR] {}.{} | exception: {}", className, methodName, ex.getMessage(), ex);
    }

    @Around("loggableMethods()")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long timeTaken = System.currentTimeMillis() - start;

            String className = pjp.getTarget().getClass().getSimpleName();
            String methodName = pjp.getSignature().getName();

            log.info("[EXIT]  {}.{} | took: {}ms", className, methodName, timeTaken);
            return result;
        } catch (Throwable ex) {
            long timeTaken = System.currentTimeMillis() - start;

            String className = pjp.getTarget().getClass().getSimpleName();
            String methodName = pjp.getSignature().getName();

            log.error("[ERROR] {}.{} | exception: {} | took: {}ms",
                    className, methodName, ex.getMessage(), timeTaken, ex);
            throw ex;
        }
    }
}