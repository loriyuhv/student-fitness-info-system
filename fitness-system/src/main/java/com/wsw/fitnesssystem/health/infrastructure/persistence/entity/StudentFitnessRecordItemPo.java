package com.wsw.fitnesssystem.health.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 学生体测记录项目明细 PO
 * <p>对应表：student_fitness_record_item（见 sql/fitness_manage.sql）</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Data
@TableName("student_fitness_record_item")
public class StudentFitnessRecordItemPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 体测记录ID */
    @TableField("record_id")
    private Long recordId;

    /** 体测项目ID（逻辑外键：fitness_item.item_id） */
    @TableField("item_id")
    private Long itemId;

    /** 项目原始成绩 */
    @TableField("item_value")
    private BigDecimal itemValue;

    /** 项目得分 */
    private BigDecimal score;

    /** 逻辑删除 */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

}
