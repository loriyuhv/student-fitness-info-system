package com.wsw.fitnesssystem.health.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 08:56
 * @since 1.0
 */
@Data
@Builder
public class FitnessRecordSubmitResult {

    /** 体测记录ID */
    private Long recordId;

    /** 各单项得分 */
    private Map<String, Integer> itemScores;

    /** 总分 */
    private BigDecimal totalScore;

    /** 等级 */
    private String totalLevel;

    /** 加分总和 */
    private Integer totalBonus;

    /** 创建时间 */
    private LocalDateTime createTime;

}
