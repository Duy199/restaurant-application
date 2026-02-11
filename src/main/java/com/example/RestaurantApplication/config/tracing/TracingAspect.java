package com.example.RestaurantApplication.config.tracing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.logstash.logback.marker.Markers;

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
        Class<?> targetClass = joinPoint.getTarget().getClass();
        // JPA proxy → lấy interface gốc (VD: UserRoleRepository)
        if (targetClass.getName().contains("$Proxy")) {
            Class<?>[] interfaces = targetClass.getInterfaces();
            if (interfaces.length > 0) {
                targetClass = interfaces[0];
            }
        }
        Logger log = LoggerFactory.getLogger(targetClass);

        String method = joinPoint.getSignature().toShortString();
        log.info(">> {}", method);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info(Markers.append("duration_ms", duration), "<< {}", method);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            log.error(Markers.append("duration_ms", duration), "<< {} FAILED - {}", method, e.getMessage());
            throw e;
        }
    }
}
