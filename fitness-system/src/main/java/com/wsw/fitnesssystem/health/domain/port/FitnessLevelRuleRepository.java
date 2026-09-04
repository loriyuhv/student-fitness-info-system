package com.wsw.fitnesssystem.health.domain.port;

import com.wsw.fitnesssystem.health.domain.valueobject.ScoreLevel;

import java.math.BigDecimal;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:44
 * @since 1.0
 */
public interface FitnessLevelRuleRepository {

    /**
     * 查询总分对应的等级
     * @param ruleSetId 规则集ID
     * @param gender 性别（0-通用）
     * @param totalScore 总分
     * @return 等级
     */
    ScoreLevel findLevel(Long ruleSetId, Integer gender, BigDecimal totalScore);

}
