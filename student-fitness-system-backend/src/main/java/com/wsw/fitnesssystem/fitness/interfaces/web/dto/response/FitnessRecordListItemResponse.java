package com.wsw.fitnesssystem.fitness.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/13 15:51
 * @since 1.0
 */
@Data
@Builder
public class FitnessRecordListItemResponse {

    /** 体测记录ID */
    @JsonProperty("record_id")
    private Long recordId;

    /** 体测时间 */
    @JsonProperty("test_time")
    private LocalDateTime testTime;

    /** 第几次体测 */
    @JsonProperty("test_round")
    private Integer testRound;

    /** 总分 */
    @JsonProperty("total_score")
    private BigDecimal totalScore;

    /** 等级名称（优秀 / 良好 / 及格 / 不及格） */
    @JsonProperty("level")
    private String level;

}
