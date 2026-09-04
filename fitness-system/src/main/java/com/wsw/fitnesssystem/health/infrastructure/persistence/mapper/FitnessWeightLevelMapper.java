package com.wsw.fitnesssystem.health.infrastructure.persistence.mapper;

import com.wsw.fitnesssystem.health.domain.valueobject.BmiResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 23:51
 * @since 1.0
 */
@Mapper
public interface FitnessWeightLevelMapper {

    @Select("""
            SELECT score, level_code, level_name
            FROM fitness_weight_level_rule
            WHERE gender = #{gender}
              AND #{bmi} >= min_bmi
              AND #{bmi} < max_bmi
              AND status = 1
              AND deleted = 0
        """)
    BmiResult selectByGenderAndBmi(@Param("gender") Integer gender, @Param("bmi") Double bmi);

}
