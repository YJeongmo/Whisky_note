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
 * [CSV 초기 데이터 로더: DataInitializer]
 *
 * [ApplicationRunner를 사용하는 이유 — @PostConstruct 대신]
 *
 * 문제점: 기존의 @PostConstruct + @Transactional 조합은 동작하지 않습니다.
 *
 * 원인:
 * - @PostConstruct는 빈 초기화 단계에서 실행됩니다.
 * - 이 시점은 Spring AOP 프록시가 아직 완전히 준비되지 않은 상태입니다.
 * - @Transactional은 AOP 프록시를 통해 동작하므로 (트랜잭션 시작/종료를 프록시가 처리),
 *   @PostConstruct에서 직접 호출하면 프록시를 거치지 않아 @Transactional이 무시됩니다.
 * - 결과: DB 오류가 발생해도 롤백이 되지 않습니다.
 *
 * 해결책: ApplicationRunner
 * - ApplicationRunner는 Spring 컨텍스트가 완전히 초기화된 후 실행됩니다.
 * - AOP 프록시가 준비된 상태이므로 @Transactional이 정상적으로 동작합니다.
 * - run() 메서드 하나만 구현하면 되는 간단한 인터페이스입니다.
 *
 * [@Profile("!test")]
 * - "!test": test 프로필이 아닐 때만 이 빈을 생성합니다.
 * - 테스트 환경에서는 CSV 로딩이 불필요하고, ClassPathResource를 찾지 못할 수도 있습니다.
 * - @SpringBootTest를 사용하는 통합 테스트에서 오류를 방지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

    private final MasterWhiskyRepository masterRepository;

    @Override
    @Transactional // ApplicationRunner에서는 AOP 프록시가 정상 동작 → @Transactional 유효
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource("whisky_master.csv");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            br.readLine(); // 첫 번째 행(헤더) 건너뛰기

            int newCount = 0;
            String line;
            while ((line = br.readLine()) != null) {
                // 쉼표로 구분하되, 큰따옴표 안의 쉼표는 무시 (CSV 표준 파싱)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String name = data[0].replace("\"", "").trim();

                // 이미 DB에 있는 위스키는 건너뜁니다 (중복 방지)
                if (masterRepository.existsByWhiskyName(name)) {
                    continue;
                }

                // @Builder 패턴으로 MasterWhisky 생성 (new + setter 방식 대신)
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
