package com.wsw.fitnesssystem.health.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:49
 * @since 1.0
 */
@Mapper
public interface FitnessScoreRuleMapper {

    @Select("""
        SELECT score FROM fitness_score_rule
        WHERE rule_set_id = #{ruleSetId}
          AND item_code = #{itemCode}
          AND gender = #{gender}
          AND #{rawValue} >= min_value
          AND #{rawValue} < max_value
          AND status = 1
          AND deleted = 0
        LIMIT 1
    """)
    Integer selectScore(
        @Param("ruleSetId") Long ruleSetId, @Param("itemCode") String itemCode,
        @Param("gender") Integer gender, @Param("rawValue") Double rawValue
    );

}
