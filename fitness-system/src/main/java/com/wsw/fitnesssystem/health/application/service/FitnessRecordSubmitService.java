package com.wsw.fitnesssystem.health.application.service;

import com.wsw.fitnesssystem.health.application.dto.command.FitnessRecordSubmitCommand;
import com.wsw.fitnesssystem.health.application.dto.result.FitnessRecordSubmitResult;

/**
 * 体测记录提交服务
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5 09:03
 * @since 1.0
 */
public interface FitnessRecordSubmitService {

    /**
     * 提交一条学生体测记录（完整落库：评分 → 记录主表 + 明细 → 汇总 upsert）
     *
     * @param command 提交命令（学号 + 操作人 + 原始成绩）
     * @return 提交结果（含 recordId、总分、等级、加分）
     */
    FitnessRecordSubmitResult submit(FitnessRecordSubmitCommand command);

}
