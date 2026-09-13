package com.wsw.fitnesssystem.fitness.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 体测记录详情响应（Web 层出参）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>对应前端 {@code GET /fitness/records/{recordId}} 的响应结构</li>
 *   <li>包含：记录头信息 + 学生简要信息 + 项目明细 + 诊断摘要</li>
 *   <li>字段命名对外统一 snake_case，与项目协议对齐</li>
 * </ul>
 *
 * <p><b>数据来源：</b>
 * <ul>
 *   <li>记录头 + 明细：fitness 模块</li>
 *   <li>学生简要信息：user 模块</li>
 *   <li>诊断摘要（physiqueType / kValue / sportPrescription）：health 模块</li>
 * </ul>
 * Controller 负责聚合三者后返回本对象。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/13 15:53
 * @since 1.0
 */
@Data
@Builder
public class FitnessRecordDetailResponse {

    /** 体测记录ID */
    @JsonProperty("record_id")
    private Long recordId;

    /** 学生简要信息 */
    private StudentBrief student;

    /** 体测时间 */
    @JsonProperty("test_time")
    private LocalDateTime testTime;

    /** 第几次体测 */
    @JsonProperty("test_round")
    private Integer testRound;

    /** 总分 */
    @JsonProperty("total_score")
    private BigDecimal totalScore;

    /** 等级名称 */
    private String level;

    /** 项目明细列表 */
    private List<ItemDetail> items;

    /** 诊断摘要（可能为 null，表示尚未生成诊断） */
    private Summary summary;

    // ==================== 嵌套结构 ====================

    /**
     * 学生简要信息（用于详情页头部展示）
     */
    @Data
    @Builder
    public static class StudentBrief {

        /** 学号 */
        @JsonProperty("student_no")
        private String studentNo;

        /** 姓名 */
        private String name;

        /** 班级名称 */
        @JsonProperty("class_name")
        private String className;
    }

    /**
     * 单项明细
     */
    @Data
    @Builder
    public static class ItemDetail {

        /** 项目编码（如 BMI / VITAL_CAPACITY） */
        @JsonProperty("item_code")
        private String itemCode;

        /** 项目名称 */
        @JsonProperty("item_name")
        private String itemName;

        /** 计量单位 */
        private String unit;

        /** 原始成绩 */
        @JsonProperty("item_value")
        private BigDecimal itemValue;

        /** 项目得分 */
        private Integer score;

        /** 加分（无加分为 0） */
        private Integer bonus;
    }

    /**
     * 诊断摘要（来自 health 模块）
     */
    @Data
    @Builder
    public static class Summary {

        /** 体质类型（如"匀称耐力型"） */
        @JsonProperty("physique_type")
        private String physiqueType;

        /** K 值（聚类编号） */
        @JsonProperty("k_value")
        private Integer kValue;

        /** 运动处方列表 */
        @JsonProperty("sport_prescription")
        private List<String> sportPrescription;
    }

}
