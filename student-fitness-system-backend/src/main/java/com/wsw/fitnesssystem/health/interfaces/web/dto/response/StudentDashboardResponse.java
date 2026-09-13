package com.wsw.fitnesssystem.health.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 学生首页仪表盘响应（Web 层出参）
 *
 * <p>对应前端 {@code GET /health/dashboard}，字段命名对外统一 snake_case。</p>
 *
 * <p><b>数据来源：</b>
 * <ul>
 *   <li>student：user 模块（StudentInfo）</li>
 *   <li>latestTest：fitness 模块（最近一次体测）</li>
 *   <li>indicators：fitness 模块（最近一次体测的 BMI / 肺活量；身高体重暂缺，可置 0）</li>
 * </ul>
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/13 18:25
 * @since 1.0
 */
@Data
@Builder
public class StudentDashboardResponse {

    /** 学生基础档案 */
    private Student student;

    /** 最近一次体测（从未体测时为 null） */
    @JsonProperty("latest_test")
    private LatestTest latestTest;

    /** 关键指标 */
    private Indicators indicators;

    // ==================== 嵌套结构 ====================

    @Data
    @Builder
    public static class Student {
        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("student_no")
        private String studentNo;

        private String name;

        /** 性别：1-男 2-女 */
        private Integer gender;

        @JsonProperty("class_name")
        private String className;

        private String college;

        @JsonProperty("enroll_year")
        private Integer enrollYear;
    }

    @Data
    @Builder
    public static class LatestTest {
        @JsonProperty("record_id")
        private Long recordId;

        @JsonProperty("test_time")
        private String testTime;

        @JsonProperty("test_round")
        private Integer testRound;

        @JsonProperty("total_score")
        private java.math.BigDecimal totalScore;

        private String level;
    }

    @Data
    @Builder
    public static class Indicators {
        /** 身高 cm（当前表结构无此字段，暂置 0） */
        private java.math.BigDecimal height;

        /** 体重 kg（当前表结构无此字段，暂置 0） */
        private java.math.BigDecimal weight;

        /** BMI 指数 */
        private java.math.BigDecimal bmi;

        /** 肺活量 ml */
        @JsonProperty("vital_capacity")
        private java.math.BigDecimal vitalCapacity;
    }

}
