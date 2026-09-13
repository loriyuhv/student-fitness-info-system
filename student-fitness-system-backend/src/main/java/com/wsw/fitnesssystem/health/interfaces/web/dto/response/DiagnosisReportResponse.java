package com.wsw.fitnesssystem.health.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 诊断报告响应（Web 层出参）
 *
 * <p>对应前端 {@code GET /health/diagnosis}，字段命名对外统一 snake_case。</p>
 *
 * <p><b>当前阶段：</b>诊断模块尚未实现，接口返回 Mock；
 * 真实实现时由 health 模块聚合 fitness 记录 + 聚类模型 + 规则表生成。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/13 18:27
 * @since 1.0
 */
@Data
@Builder
public class DiagnosisReportResponse {

    /** 关联的体测记录 ID */
    @JsonProperty("record_id")
    private Long recordId;

    /** 报告生成时间 */
    @JsonProperty("generate_time")
    private String generateTime;

    /** 总分 */
    @JsonProperty("total_score")
    private BigDecimal totalScore;

    /** 等级名称 */
    private String level;

    /** K-means 聚类值 */
    @JsonProperty("k_value")
    private Integer kValue;

    /** 体质类型 */
    @JsonProperty("physique_type")
    private String physiqueType;

    /** 运动处方（多条建议） */
    @JsonProperty("sport_prescription")
    private List<String> sportPrescription;

    /** 健康风险提示 */
    @JsonProperty("health_risks")
    private List<HealthRisk> healthRisks;

    // ==================== 嵌套结构 ====================

    @Data
    @Builder
    public static class HealthRisk {

        private String title;

        /** 风险级别：high-高 / medium-中 / low-低 */
        private String severity;

        private String risk;

        private String suggestion;
    }

}
