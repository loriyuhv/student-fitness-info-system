package com.wsw.fitnesssystem.health.application.service;

import com.wsw.fitnesssystem.health.application.dto.FitnessScoreResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 21:10
 * @since 1.0
 */
@SpringBootTest
class FitnessScoreQueryServiceTest {

    @Autowired
    private FitnessScoreQueryService fitnessScoreQueryService;

    @Test
    void testCalculate() {
        // 大一男生，各项成绩
        Map<String, Double> rawScores = new HashMap<>();
        rawScores.put("BMI", 22.0);              // 100分（正常）
        rawScores.put("VITAL_CAPACITY", 5040.0); // 100分
        rawScores.put("50M", 6.7);              // 100分
        rawScores.put("SIT_AND_REACH", 24.9);   // 100分
        rawScores.put("STANDING_LONG_JUMP", 273.0); // 100分
        rawScores.put("PULL_UP", 22.0);          // 100分 + 3分
        rawScores.put("RUN_1000_800", 189.0);    // 100分 + 2分
        // 3'10

        FitnessScoreResult result = fitnessScoreQueryService.calculate(1, 1, rawScores);

        System.out.println("单项得分: " + result.getItemScores());
        System.out.println("加分: " + result.getBonusDetails() + " 共 " + result.getTotalBonus() + " 分");
        System.out.println("总分: " + result.getTotalScore());
        System.out.println("等级: " + result.getLevelName());

        assertThat(result.getTotalScore()).isGreaterThan(BigDecimal.valueOf(100));
        assertThat(result.getLevelCode()).isEqualTo("EXCELLENT");
    }

}