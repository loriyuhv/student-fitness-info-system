package com.wsw.fitnesssystem.health.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:53
 * @since 1.0
 */
@Mapper
public interface FitnessBonusRuleMapper {

    Integer selectBonus(
        @Param("ruleSetId") Long ruleSetId,
        @Param("itemCode") String itemCode,
        @Param("gender") Integer gender,
        @Param("rawValue") Double rawValue,
        @Param("isReverse") boolean isReverse
    );

}
