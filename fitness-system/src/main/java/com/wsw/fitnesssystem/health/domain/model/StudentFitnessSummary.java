package com.wsw.fitnesssystem.health.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 09:01
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class StudentFitnessSummary {

    private Long summaryId;
    private Long userId;
    private Long recordId;
    private BigDecimal totalScore;
    private String totalLevel;
    private LocalDateTime latestTestTime;
    private Integer status;

    /**
     * 从体测记录创建汇总
     */
    public static StudentFitnessSummary fromRecord(Long userId, StudentFitnessRecord record) {
        return StudentFitnessSummary.builder()
            .userId(userId)
            .recordId(record.getRecordId())
            .totalScore(record.getTotalScore())
            .totalLevel(record.getTotalLevel())
            .latestTestTime(record.getTestTime())
            .status(1)
            .build();
    }

    /**
     * 更新汇总（用于已有汇总时）
     */
    public void updateFromRecord(StudentFitnessRecord record) {
        this.recordId = record.getRecordId();
        this.totalScore = record.getTotalScore();
        this.totalLevel = record.getTotalLevel();
        this.latestTestTime = record.getTestTime();
    }

}
