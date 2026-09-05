package com.wsw.fitnesssystem.health.domain.port;

import com.wsw.fitnesssystem.health.domain.model.StudentFitnessRecord;

/**
 * 体测记录仓储（Domain 层 Port）
 * <p>由 Infrastructure 层实现，负责体测记录主表 + 明细表的持久化。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
public interface FitnessRecordRepository {

    /**
     * 保存一条体测记录（主表 + 明细，事务内调用）
     *
     * <p>插入成功后回填 {@code recordId}（明细记录同样回填主键）。</p>
     *
     * @param record 体测记录聚合（含明细列表）
     * @return 回填主键后的体测记录
     */
    StudentFitnessRecord save(StudentFitnessRecord record);

}
