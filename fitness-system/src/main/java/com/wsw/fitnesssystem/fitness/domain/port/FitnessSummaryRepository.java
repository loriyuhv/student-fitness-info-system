package com.wsw.fitnesssystem.fitness.domain.port;

import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessSummary;

/**
 * 学生体测汇总仓储（Domain 层 Port）
 * <p>由 Infrastructure 层实现。每个学生逻辑上只有一条汇总记录
 * （表结构唯一键：user_id + deleted），存在则更新、不存在则插入。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
public interface FitnessSummaryRepository {

    /**
     * 按用户 upsert 体测汇总（存在则覆盖为最新一次体测，不存在则插入）。
     *
     * @param summary 汇总聚合
     */
    void upsertByUserId(StudentFitnessSummary summary);

}
