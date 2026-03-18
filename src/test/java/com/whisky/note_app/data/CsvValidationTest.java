package com.whisky.note_app.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class CsvValidationTest {

    @Test
    @DisplayName("CSV 마스터 데이터 형식 검증 - 컬럼 개수 및 필수값 체크")
    void validateWhiskyCsvFormat() {
        try {
            ClassPathResource resource = new ClassPathResource("whisky_master.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            String header = br.readLine(); // 헤더 읽기
            int expectedColumnCount = 7; // 위스키이름, 증류소, 분류, 소분류, N, P, F 총 7개
            int lineCount = 1;

            while ((line = br.readLine()) != null) {
                lineCount++;

                // 정규식을 이용해 쉼표 분리 (데이터 내 쉼표 허용 로직 포함)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // 1. 컬럼 개수 검증
                if (data.length != expectedColumnCount) {
                    fail(String.format("줄번호 %d: 컬럼 개수가 맞지 않습니다. (기대치: %d, 실제: %d) -> 내용: %s",
                            lineCount, expectedColumnCount, data.length, line));
                }

                // 2. 필수 필드 빈값 검증 (이름, 분류 등은 비어있으면 안 됨)
                assertThat(data[0].trim()).withFailMessage("줄번호 %d: 위스키 이름이 비어있습니다.", lineCount).isNotEmpty();
                assertThat(data[2].trim()).withFailMessage("줄번호 %d: 분류(Category)가 비어있습니다.", lineCount).isNotEmpty();

                // 3. 따옴표 짝이 맞는지 검증
                long quoteCount = line.chars().filter(ch -> ch == '"').count();
                if (quoteCount % 2 != 0) {
                    fail(String.format("줄번호 %d: 큰따옴표(\")의 짝이 맞지 않습니다.", lineCount));
                }
            }
            System.out.println("검증 완료: 총 " + lineCount + "개의 라인이 정상입니다.");

        } catch (Exception e) {
            fail("파일을 읽는 도중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }
}