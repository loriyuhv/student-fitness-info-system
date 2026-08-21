package com.wsw.fitnesssystem.handle_excel.core.parser;

import com.alibaba.excel.EasyExcel;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.EasyExcelListener;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.StreamBatchListener;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel 通用解析器
 * 支持两种模式：
 * 1. 全量解析：小文件，直接返回 List（简单场景）
 * 2. 流式分片解析：大文件，边读边处理，内存占用极低（推荐）
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
public class ExcelParser {
    /**
     * 模式一：全量解析（适合小文件 < 1万条）
     * 返回完整的 List<T>
     * @param file Excel文件
     * @param dtoClass 对应业务类型DTO
     * @return 解析成功后的结果列表
     * @param <T> DTO class
     */
    public <T> List<T> parse(File file, Class<T> dtoClass) {
        List<T> list = new ArrayList<>();
        try {
            EasyExcel.read(file, dtoClass, new EasyExcelListener<>(list))
                    .sheet().doRead();
        } catch (Exception e) {
            log.error("Excel 解析失败, dtoClass={}, file={}",
                    dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            // throw new BizException(ResultCode.PARAM_TYPE_ERROR, "Excel 解析失败: " + e.getMessage());
            throw new BizException(ResultCode.PARAM_TYPE_ERROR);
        }
        log.info("Excel 全量解析完成, dtoClass={}, 共 {} 条",
                dtoClass.getSimpleName(), list.size());
        return list;
    }


    /**
     * 模式二：流式分片解析（适合大文件 >= 1万条）
     * 每攒够 batchSize 条就回调 consumer，内存里只存当前批次
     * @param file Excel 文件
     * @param dtoClass DTO 类型
     * @param batchSize 每批条数
     * @param consumer 批次处理器（在回调里直接处理，不要长期持有引用）
     * @param <T> 解析后的文件
     */
    public <T> void parseStream(
            File file, Class<T> dtoClass, int batchSize, Consumer<List<T>> consumer) {
        try {
            EasyExcel.read(file, dtoClass,
                            new StreamBatchListener<>(consumer, batchSize))
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            log.error("Excel 流式解析失败, dtoClass={}, file={}",
                    dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            // throw new BizException(ResultCode.PARAM_TYPE_ERROR, "Excel 解析失败: " + e.getMessage());
            throw new BizException(ResultCode.PARAM_TYPE_ERROR);
        }
    }

}
