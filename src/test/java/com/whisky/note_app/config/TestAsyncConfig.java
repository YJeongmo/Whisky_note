package com.whisky.note_app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * [TestAsyncConfig]
 *
 * 테스트 환경에서 @Async 메서드를 동기로 실행하도록 설정합니다.
 *
 * [왜 필요한가?]
 * @Async는 별도 스레드에서 실행되므로 테스트에서 assert 시점에
 * 아직 분석이 완료되지 않아 검증이 실패할 수 있습니다.
 * SyncTaskExecutor로 교체하면 @Async 메서드가 호출 스레드에서
 * 동기적으로 실행되어 완료 후 assert가 가능합니다.
 */
@TestConfiguration
public class TestAsyncConfig {

    @Bean(name = "analysisExecutor")
    public Executor analysisExecutor() {
        return new SyncTaskExecutor(); // 테스트에서는 동기 실행
    }
}
