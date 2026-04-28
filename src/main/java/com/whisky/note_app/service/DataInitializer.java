package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 애플리케이션 시작 시 위스키 마스터 CSV를 DB에 적재합니다.
 * 이미 존재하는 항목은 건너뜁니다.
 *
 * ApplicationRunner를 사용하는 이유: Spring 컨텍스트가 완전히 초기화된 후 실행되므로
 * @Transactional이 정상 동작합니다. @PostConstruct는 AOP 프록시 준비 전에 실행되어
 * @Transactional이 무시됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

    private final MasterWhiskyRepository masterRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource("whisky_master.csv");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            br.readLine(); // 헤더 행 건너뜀

            int newCount = 0;
            String line;
            while ((line = br.readLine()) != null) {
                // 큰따옴표 내부의 쉼표를 구분자로 처리하지 않기 위한 CSV 표준 파싱
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String name = data[0].replace("\"", "").trim();

                if (masterRepository.existsByWhiskyName(name)) {
                    continue;
                }

                MasterWhisky master = MasterWhisky.builder()
                        .whiskyName(name)
                        .distillery(data[1].replace("\"", "").trim())
                        .category(data[2].replace("\"", "").trim())
                        .subCategory(data[3].replace("\"", "").trim())
                        .nose(data[4].replace("\"", "").trim())
                        .palate(data[5].replace("\"", "").trim())
                        .finish(data[6].replace("\"", "").trim())
                        .build();

                masterRepository.save(master);
                newCount++;
            }

            if (newCount > 0) {
                log.info("새로운 위스키 마스터 데이터 {}건이 추가되었습니다.", newCount);
            } else {
                log.info("모든 위스키 데이터가 이미 최신 상태입니다.");
            }

        } catch (Exception e) {
            log.error("CSV 로딩 중 오류 발생: {}", e.getMessage());
        }
    }
}
