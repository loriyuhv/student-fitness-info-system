package com.wsw.fitnesssystem.health.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生体测汇总 PO
 * <p>对应表：student_fitness_summary（见 sql/fitness_manage.sql）</p>
 * <p>逻辑上每个学生仅一条汇总（唯一键 user_id + deleted）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Data
@TableName("student_fitness_summary")
public class StudentFitnessSummaryPo {

    @TableId(type = IdType.AUTO)
    private Long summaryId;

    /** 学生用户ID（逻辑外键：sys_user.user_id） */
    @TableField("user_id")
    private Long userId;

    /** 最近一次体测记录ID */
    @TableField("record_id")
    private Long recordId;

    /** 总分 */
    @TableField("total_score")
    private BigDecimal totalScore;

    /** 总体等级 */
    @TableField("total_level")
    private String totalLevel;

    /** 最近体测时间 */
    @TableField("latest_test_time")
    private LocalDateTime latestTestTime;

    /** 状态：0-无效 1-正常 2-冻结 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

}
