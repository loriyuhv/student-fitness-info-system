package com.wsw.fitnesssystem.fitness.interfaces.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 教师提交体测记录请求（Web 层协议输入）
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Data
public class FitnessRecordSubmitRequest {

    /** 学生学号（必填） */
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20位")
    private String studentNo;

    /** 第几次体测（选填，缺省 1） */
    private Integer testRound;

    /** 体测时间（选填，缺省当前时间），格式：yyyy-MM-dd HH:mm:ss */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        message = "体测时间格式应为 yyyy-MM-dd HH:mm:ss")
    private String testTime;

    /**
     * 原始成绩：项目编码 → 原始值（必填，需覆盖 7 个必测项目）
     * <p>项目编码：BMI / VITAL_CAPACITY / 50M / SIT_AND_REACH /
     * STANDING_LONG_JUMP / PULL_UP / RUN_1000_800</p>
     */
    @NotEmpty(message = "体测原始成绩不能为空")
    private Map<String, Double> rawScores;

}
