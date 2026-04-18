package com.whisky.note_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [JPA Auditing 설정 분리]
 *
 * [왜 WhiskyNoteApplication에서 분리했는가?]
 *
 * 문제: @EnableJpaAuditing을 @SpringBootApplication이 있는 메인 클래스에 두면
 *       @DataJpaTest 슬라이스 테스트가 깨집니다.
 *
 * 원인:
 * - @DataJpaTest는 JPA 관련 빈만 로드하는 '슬라이스 테스트'입니다.
 * - @SpringBootApplication이 붙은 클래스 전체를 로드하지 않습니다.
 * - 하지만 @EnableJpaAuditing은 AuditorAware 빈을 필요로 합니다.
 * - 슬라이스 테스트에선 그 빈이 없으므로 → 오류 발생!
 *
 * 해결:
 * - @EnableJpaAuditing을 별도 @Configuration 클래스로 분리합니다.
 * - @DataJpaTest는 @Configuration 클래스는 정상적으로 로드합니다.
 * - 결과적으로 슬라이스 테스트에서도 @CreatedDate가 정상 동작합니다.
 *
 * [슬라이스 테스트란?]
 * - @SpringBootTest: 전체 애플리케이션 컨텍스트 로드 (느림, 통합 테스트)
 * - @DataJpaTest: JPA 관련 빈만 로드 (빠름, 단위 테스트)
 * - @WebMvcTest: 웹 레이어 빈만 로드 (빠름, 컨트롤러 테스트)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // 설정만 담당하는 클래스라 메서드가 필요 없습니다.
    // @EnableJpaAuditing 어노테이션 하나가 모든 역할을 합니다.
}
