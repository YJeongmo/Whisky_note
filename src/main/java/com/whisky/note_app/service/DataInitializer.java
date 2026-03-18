package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializer {

    private final MasterWhiskyRepository masterRepository;

    @PostConstruct
    @Transactional
    public void initData() {
        try {
            ClassPathResource resource = new ClassPathResource("whisky_master.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            br.readLine(); // 첫 번째 행(헤더) 건너뛰기

            int newCount = 0;
            while ((line = br.readLine()) != null) {
                // 쉼표로 구분하되, 큰따옴표 안의 쉼표는 무시
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String name = data[0].replace("\"", "").trim();

                // 1. 이름을 확인하여 DB에 있으면 무시
                if (masterRepository.existsByWhiskyName(name)) {
                    continue;
                }

                // 2. 새로 추가된 위스키 저장
                MasterWhisky master = new MasterWhisky();
                master.setWhiskyName(name);
                master.setDistillery(data[1].replace("\"", "").trim());
                master.setCategory(data[2].replace("\"", "").trim());
                master.setSubCategory(data[3].replace("\"", "").trim());
                master.setNose(data[4].replace("\"", "").trim());
                master.setPalate(data[5].replace("\"", "").trim());
                master.setFinish(data[6].replace("\"", "").trim());

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
