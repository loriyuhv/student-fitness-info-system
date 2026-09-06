package com.wsw.fitnesssystem.fitness.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 08:58
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class StudentFitnessRecord {

    private Long recordId;
    private Long studentUserId;
    private Long operatorUserId;
    private LocalDateTime testTime;
    private Integer testRound;
    private Integer testType;
    private BigDecimal totalScore;
    private String totalLevel;
    private Integer status;
    private Integer confirmStatus;
    private LocalDateTime confirmTime;
    private String remark;

    /** 明细列表（用于保存时批量插入） */
    private List<StudentFitnessRecordItem> items;

    /**
     * 业务方法：确认记录
     */
    public void confirm() {
        this.confirmStatus = 1;
        this.confirmTime = LocalDateTime.now();
    }

    /**
     * 业务方法：作废记录
     */
    public void invalidate() {
        this.status = 0;
    }

}
