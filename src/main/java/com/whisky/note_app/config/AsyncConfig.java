package com.whisky.note_app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * [AsyncConfig — Step 11]
 *
 * [@EnableAsync]
 * @Async 애너테이션이 동작하도록 활성화합니다.
 * 이게 없으면 @Async를 붙여도 동기로 실행됩니다.
 *
 * [왜 기본 Executor를 쓰지 않나?]
 * Spring 기본값(SimpleAsyncTaskExecutor)은 요청마다 스레드를 새로 생성합니다.
 * 스레드 생성 비용이 크고, 무한정 생성되어 서버 과부하가 발생할 수 있습니다.
 * ThreadPoolTaskExecutor로 스레드 풀을 직접 관리합니다.
 *
 * [스레드 풀 파라미터]
 * - corePoolSize:  평상시 유지할 스레드 수
 * - maxPoolSize:   최대 생성 가능한 스레드 수
 * - queueCapacity: 스레드가 모두 사용 중일 때 대기 큐 크기
 * - threadNamePrefix: 로그에서 스레드를 식별하기 위한 이름 접두사
 *
 * [처리 흐름]
 * 요청 → 유휴 스레드 할당 → 없으면 큐 대기(최대 50)
 *      → 큐도 꽉 차면 maxPoolSize(10)까지 스레드 추가 생성
 *      → 그것도 꽉 차면 RejectedExecutionException
 *
 * [@ConditionalOnMissingBean]
 * 같은 이름의 빈이 이미 등록되어 있으면 이 빈을 생성하지 않습니다.
 * 테스트에서 TestAsyncConfig가 SyncTaskExecutor를 먼저 등록하면
 * 이 빈은 건너뛰어 BeanDefinitionOverrideException을 방지합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "analysisExecutor")
    @ConditionalOnMissingBean(name = "analysisExecutor")
    public Executor analysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // 평상시 5개 스레드 유지
        executor.setMaxPoolSize(10);       // 최대 10개까지 생성
        executor.setQueueCapacity(50);     // 최대 50개 요청 대기
        executor.setThreadNamePrefix("analysis-"); // 로그에서 식별 용이
        executor.initialize();
        return executor;
    }
}
