package com.wsw.fitnesssystem.health.domain.port;

import com.wsw.fitnesssystem.health.domain.valueobject.BmiResult;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 23:50
 * @since 1.0
 */
public interface FitnessWeightLevelRepository {

    /**
     * 根据性别和 BMI 值查询等级和得分
     * @param gender 性别：1-男 2-女
     * @param bmi BMI 值
     * @return BMI 结果（得分 + 等级）
     */
    BmiResult findByGenderAndBmi(Integer gender, Double bmi);

}
