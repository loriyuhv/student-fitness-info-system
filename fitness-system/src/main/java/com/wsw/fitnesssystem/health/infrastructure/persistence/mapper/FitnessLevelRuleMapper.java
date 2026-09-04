package com.wsw.fitnesssystem.health.infrastructure.persistence.mapper;

import com.wsw.fitnesssystem.health.domain.valueobject.ScoreLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:56
 * @since 1.0
 */
@Mapper
public interface FitnessLevelRuleMapper {

    @Select("""
        SELECT level_code, level_name FROM fitness_score_level_rule
        WHERE rule_set_id = #{ruleSetId}
          AND (gender = #{gender} OR gender = 0)
          AND #{totalScore} >= min_score
          AND #{totalScore} < max_score
          AND status = 1
          AND deleted = 0
        ORDER BY gender DESC
        LIMIT 1
    """)
    ScoreLevel selectLevel(
        @Param("ruleSetId") Long ruleSetId,
        @Param("gender") Integer gender,
        @Param("totalScore") BigDecimal totalScore
    );

}
