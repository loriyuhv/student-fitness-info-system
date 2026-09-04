package com.wsw.fitnesssystem.health.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:41
 * @since 1.0
 */
@Data
@Builder
public class FitnessScoreResult {

    /** 各单项得分（项目编码 → 得分） */
    private Map<String, Integer> itemScores;

    /** BMI 等级得分 */
    private Integer bmiScore;

    /** BMI 等级名称 */
    private String bmiLevel;

    /** 加分总和 */
    private Integer totalBonus;

    /** 加分明细（项目编码 → 加分值） */
    private Map<String, Integer> bonusDetails;

    /** 总分（加权总分 + 加分） */
    private BigDecimal totalScore;

    /** 等级编码 */
    private String levelCode;

    /** 等级名称 */
    private String levelName;

}
