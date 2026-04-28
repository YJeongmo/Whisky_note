package com.whisky.note_app.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * controller 패키지의 모든 public 메서드에 요청/응답 로그 및 처리 시간을 기록합니다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.whisky.note_app.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        long startTime = System.currentTimeMillis();

        log.info("[API 요청] {}.{}()", className, methodName);

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("[API 완료] {}.{}() | 처리시간: {}ms", className, methodName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API 오류] {}.{}() | 처리시간: {}ms | 예외: {} - {}",
                    className, methodName, duration,
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
