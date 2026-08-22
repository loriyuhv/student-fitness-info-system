package com.wsw.fitnesssystem.handle_excel.core.port;

import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;

import java.util.List;

/**
 * 导入进度上报端口 — 核心层只声明契约，不关心 Redis/MySQL/本地内存
 * <p>符合 DDD Lite 的"依赖倒置"原则：core 层定义接口，infrastructure 层实现</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 01:07
 * @since 1.0
 */
public interface ImportProgressPort {

    /**
     * 初始化进度（解析完 Excel 后调用）
     * @param taskId 任务ID
     * @param total 总数据条数
     */
    void init(String taskId, int total);

    /**
     * 更新进度（每批处理完调用）
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param errorMsgList 错误信息列表
     */
    void updateProgress(String taskId, int successCount, int failCount, List<String> errorMsgList);

    /**
     * 全部成功完成
     * @param taskId 任务ID
     * @param successCount 成功数量
     */
    void finish(String taskId, int successCount);

    /**
     * 部分成功（有失败记录）
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param errorMsgList 错误信息列表
     */
    void partial(String taskId, int successCount, int failCount, List<String> errorMsgList);

    /**
     * 任务失败
     * @param taskId 任务ID
     * @param errorMsg 错误信息
     */
    void fail(String taskId, String errorMsg);

    /**
     * 查询进度
     * @param taskId 任务ID
     * @return 进度 DTO
     */
    ImportProgressDTO getProgress(String taskId);

}
