package com.wsw.fitnesssystem.fitness.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生体测记录主表 PO
 * <p>对应表：student_fitness_record（见 sql/fitness_manage.sql）</p>
 * <p>仅承载落库所需字段；未赋值的列（诊断、审计等）由数据库默认值兜底。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Data
@TableName("student_fitness_record")
public class StudentFitnessRecordPo {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    /** 学生用户ID（逻辑外键：sys_user.user_id） */
    @TableField("student_user_id")
    private Long studentUserId;

    /** 操作人用户ID（逻辑外键：sys_user.user_id） */
    @TableField("operator_user_id")
    private Long operatorUserId;

    /** 体测时间 */
    @TableField("test_time")
    private LocalDateTime testTime;

    /** 第几次体测 */
    @TableField("test_round")
    private Integer testRound;

    /** 体测类型：1-正式 2-补测 3-重测 */
    @TableField("test_type")
    private Integer testType;

    /** 总分 */
    @TableField("total_score")
    private BigDecimal totalScore;

    /** 总评等级名称（如：优秀 / 及格） */
    @TableField("total_level")
    private String totalLevel;

    /** 状态：0-作废 1-正常 2-已汇总 */
    private Integer status;

    /** 确认状态：0-未确认 1-已确认 */
    @TableField("confirm_status")
    private Integer confirmStatus;

    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    /** 备注 */
    private String remark;

    /** 逻辑删除 */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

}
