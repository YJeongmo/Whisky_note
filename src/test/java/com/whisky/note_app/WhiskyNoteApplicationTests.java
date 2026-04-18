package com.whisky.note_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // H2 인메모리 DB 사용 (PostgreSQL 연결 없이 실행)
class WhiskyNoteApplicationTests {

	@Test
	void contextLoads() {
	}

}
