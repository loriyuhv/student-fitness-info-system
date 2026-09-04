package com.wsw.fitnesssystem.health.application.service;

import com.wsw.fitnesssystem.health.application.dto.FitnessScoreQueryCommand;
import com.wsw.fitnesssystem.health.application.dto.FitnessScoreResult;

import java.util.Map;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:59
 * @since 1.0
 */
public interface FitnessScoreQueryService {

    /**
     * 计算体测总分
     * @param gender 性别
     * @param grade 年级
     * @param rawScores 原始成绩：项目编码 → 原始值
     * @return 评分结果
     */
    FitnessScoreResult calculate(Integer gender, Integer grade, Map<String, Double> rawScores);

    /**
     * 单项评分查询（用于适配器逐项计算）
     * @param command 体测分数查询命令
     * @return 评分
     */
    Integer querySingleScore(FitnessScoreQueryCommand command);

    /**
     * 单项加分查询
     * @param command 查询命令
     * @return 分数
     */
    Integer querySingleBonus(FitnessScoreQueryCommand command);


}
