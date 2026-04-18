package com.whisky.note_app.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * [LoggingAspect — 전역 API 로깅]
 *
 * [적용 대상]
 * controller 패키지의 모든 public 메서드에 자동 적용됩니다.
 * 기존 NoteController, MasterWhiskyController, RecommendController, AuthController
 * 전부 코드 수정 없이 적용되고, 앞으로 추가되는 컨트롤러도 자동 적용됩니다.
 *
 * [왜 @Around를 선택했는가?]
 * - @Before: 메서드 실행 전만 가능 → 처리 시간 측정 불가
 * - @AfterReturning: 정상 응답만 처리 → 예외 시 로그 없음
 * - @Around: 실행 전/후를 모두 감싸므로 처리 시간 측정 + 예외 처리 모두 가능
 *
 * [Spring AOP 동작 원리 — 프록시 패턴]
 * Spring이 이 Aspect를 감지하면, 대상 Bean을 프록시 객체로 감쌉니다.
 * 실제 호출 흐름: 클라이언트 → 프록시(로그 출력) → 실제 컨트롤러 메서드
 * 컨트롤러 코드 자체는 전혀 변경되지 않습니다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * [Pointcut + Advice 정의]
     *
     * "execution(* com.whisky.note_app.controller.*.*(..))"
     *  ↑           ↑  ↑                          ↑ ↑  ↑
     *  execution   반환타입(*)  패키지            클래스(*)  메서드(*)  파라미터(..)
     *
     * 해석: controller 패키지 안의 모든 클래스의 모든 메서드(파라미터 무관)
     */
    @Around("execution(* com.whisky.note_app.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // 호출된 클래스명, 메서드명 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        // 처리 시간 측정 시작
        long startTime = System.currentTimeMillis();

        log.info("[API 요청] {}.{}()", className, methodName);

        try {
            // 실제 컨트롤러 메서드 실행
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("[API 완료] {}.{}() | 처리시간: {}ms", className, methodName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            // 예외 발생 시 어떤 예외인지, 어디서 발생했는지 로그
            log.error("[API 오류] {}.{}() | 처리시간: {}ms | 예외: {} - {}",
                    className, methodName, duration,
                    e.getClass().getSimpleName(), e.getMessage());
            // 예외는 다시 던져서 GlobalExceptionHandler가 처리하게 합니다
            throw e;
        }
    }
}
