package com.whisky.note_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // 날짜 자동 기록 활성화
@SpringBootApplication
public class WhiskyNoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhiskyNoteApplication.class, args);
	}

}
