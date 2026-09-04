package com.wsw.fitnesssystem.health.domain.port;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:43
 * @since 1.0
 */
public interface FitnessScoreRuleRepository {

    /**
     * 查询单项得分
     * @param ruleSetId 规则集ID
     * @param itemCode 项目编码
     * @param gender 性别
     * @param rawValue 原始值
     * @return 得分（可能为 null 表示未匹配到规则）
     */
    Integer findScore(Long ruleSetId, String itemCode, Integer gender, Double rawValue);

}
