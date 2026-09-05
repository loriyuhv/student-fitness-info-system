package com.wsw.fitnesssystem.health.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 08:59
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class StudentFitnessRecordItem {

    private Long id;
    private Long recordId;
    private Long itemId;
    private BigDecimal itemValue;
    private Integer score;

    /**
     * 从评分引擎明细结果创建
     */
    public static StudentFitnessRecordItem fromScoreDetail(
        Long itemId,
        BigDecimal itemValue,
        Integer score
    ) {
        return StudentFitnessRecordItem.builder()
            .itemId(itemId)
            .itemValue(itemValue)
            .score(score)
            .build();
    }

}
