package com.example.RestaurantApplication.config.tracing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TracingAspect {

    @Pointcut("execution(* com.example.RestaurantApplication.module..*.*(..))")
    public void moduleLayer() {}

    @Pointcut("within(com.example.RestaurantApplication.config.jwt.JwtService)")
    public void jwtService() {}

    @Pointcut("within(com.example.RestaurantApplication.config.redis.TokenBlacklistService)")
    public void redisService() {}

    @Around("moduleLayer() || jwtService() || redisService()")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        String method = joinPoint.getSignature().toShortString();
        log.info(">> {}", method);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("<< {} ({}ms)", method, duration);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            log.error("<< {} FAILED ({}ms) - {}", method, duration, e.getMessage());
            throw e;
        }
    }
}
