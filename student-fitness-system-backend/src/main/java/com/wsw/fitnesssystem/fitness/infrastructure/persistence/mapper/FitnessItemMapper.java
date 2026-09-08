package com.wsw.fitnesssystem.fitness.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 体测项目字典 Mapper
 * <p>对应表：fitness_item</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Mapper
public interface FitnessItemMapper {

    /**
     * 根据项目编码查询项目ID（BMI / VITAL_CAPACITY / PULL_UP ...）
     *
     * @param itemCode 项目编码
     * @return 项目ID；不存在返回 null
     */
    @Select("""
            SELECT item_id
            FROM fitness_item
            WHERE item_code = #{itemCode}
              AND status = 1
              AND deleted = 0
            LIMIT 1
            """)
    Long selectItemIdByCode(@Param("itemCode") String itemCode);

}
