package com.whisky.note_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * [애플리케이션 진입점]
 *
 * [@EnableJpaAuditing 제거 이유]
 * - 기존에는 여기에 @EnableJpaAuditing이 있었습니다.
 * - @DataJpaTest 슬라이스 테스트와의 충돌을 막기 위해
 *   config/JpaAuditingConfig.java로 분리했습니다.
 * - 기능은 동일하게 동작합니다. (JpaAuditingConfig가 @Configuration이므로 자동 감지)
 */
@SpringBootApplication
public class WhiskyNoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhiskyNoteApplication.class, args);
	}

}
