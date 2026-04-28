package com.whisky.note_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @EnableJpaAuditing을 별도 클래스로 분리합니다.
 * 메인 클래스에 두면 @DataJpaTest 슬라이스 테스트 실행 시 AuditorAware 빈 누락으로 오류가 발생합니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
