package com.wsw.fitnesssystem.health.application.dto.command;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 教师提交体测记录命令
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5 08:54
 * @since 1.0
 */
@Data
@Builder
public class FitnessRecordSubmitCommand {

    /** 学生学号 */
    private String studentNo;

    /** 教师操作人ID（由 Controller 从 SecurityContext 获取） */
    private Long operatorUserId;

    /** 第几次体测 */
    private Integer testRound;

    /** 体测时间（可选，默认当前时间） */
    private String testTime;

    /** 原始成绩：项目编码 → 原始值 */
    private Map<String, Double> rawScores;

}
