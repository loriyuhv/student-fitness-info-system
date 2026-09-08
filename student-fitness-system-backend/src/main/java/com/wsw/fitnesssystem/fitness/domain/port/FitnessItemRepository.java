package com.wsw.fitnesssystem.fitness.domain.port;

import java.util.Optional;

/**
 * 体测项目字典仓储（Domain 层 Port）
 * <p>由 Infrastructure 层实现，负责按项目编码查询项目主键。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
public interface FitnessItemRepository {

    /**
     * 根据项目编码查询项目ID（如 BMI / VITAL_CAPACITY / PULL_UP ...）
     *
     * @param itemCode 项目编码
     * @return 项目ID；未找到返回 empty
     */
    Optional<Long> findIdByCode(String itemCode);

}
