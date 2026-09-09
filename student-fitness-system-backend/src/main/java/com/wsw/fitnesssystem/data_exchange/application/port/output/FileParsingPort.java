package com.wsw.fitnesssystem.data_exchange.application.port.output;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * 文件解析端口（输出端口）。
 * <p>
 * 定义文件解析契约，由基础设施层实现。
 * 支持全量解析和流式解析两种模式。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 00:18
 * @since 1.0
 */
public interface FileParsingPort {

    /**
     * 全量解析（适合小文件 &lt; 1万行）。
     *
     * @param file      待解析的文件
     * @param targetClass 目标 DTO 类型
     * @param taskId    任务 ID
     * @param <T>       DTO 类型
     * @return 完整的 DTO 列表
     */
    <T> List<T> parseFull(File file, Class<T> targetClass, String taskId);

    /**
     * 流式解析（适合大文件 ≥ 1万行）。
     *
     * @param file       待解析的文件
     * @param targetClass 目标 DTO 类型
     * @param batchSize  每批处理条数
     * @param consumer   批次处理器
     * @param <T>        DTO 类型
     */
    <T> void parseStream(File file, Class<T> targetClass, int batchSize, Consumer<List<T>> consumer);

    /**
     * 快速预估文件行数。
     *
     * @param file 待预估的文件
     * @return 预估行数（0 表示预估失败）
     */
    int estimatedRowCount(File file);

}
