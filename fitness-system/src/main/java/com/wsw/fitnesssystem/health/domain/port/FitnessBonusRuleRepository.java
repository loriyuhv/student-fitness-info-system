package com.wsw.fitnesssystem.health.domain.port;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:44
 * @since 1.0
 */
public interface FitnessBonusRuleRepository {

    /**
     * 查询加分值
     * @param ruleSetId 规则集ID
     * @param itemCode 项目编码
     * @param gender 性别
     * @param rawValue 原始值
     * @param isReverse 是否反向项目（true=值越小越好，false=值越大越好）
     * @return 加分值
     */
    Integer findBonus(
        Long ruleSetId, String itemCode, Integer gender, Double rawValue, boolean isReverse
    );

}
