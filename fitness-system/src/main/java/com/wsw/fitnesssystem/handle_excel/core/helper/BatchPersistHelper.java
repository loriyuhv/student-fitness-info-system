package com.wsw.fitnesssystem.handle_excel.core.helper;

import com.google.common.collect.Lists;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelSysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 通用批量插入助手
 * 实现"分批试错回退"策略：
 * 1. 大块批量插入（2000条/批）
 * 2. 失败后拆分为子批（50条/批）
 * 3. 子批仍失败则逐条插入并精确定位脏数据
 *
 * @author loriyuhv
 * @version 1.0 2026/8/29 15:27
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchPersistHelper {

    private final ExcelSysUserMapper userMapper;

    /**
     * 安全批量插入（带回退机制）
     *
     * @param entities         待插入实体列表
     * @param batchInsertFunc  批量插入函数（由适配器提供，如 userMapper::batchInsert）
     * @param singleInsertFunc 单条插入函数（由适配器提供，用于降级时逐条插入）
     * @param batchSize        大块批量大小（建议 2000）
     * @param collector        错误收集器
     * @param <E>              实体类型
     * @return 成功插入数量
     */
    public <E> int safeBatchInsert(
        List<E> entities,
        Function<List<E>, Integer> batchInsertFunc,
        Function<E, Integer> singleInsertFunc,
        int batchSize,
        ErrorCollector collector
    ) {
        if (entities == null || entities.isEmpty()) return 0;

        int totalSuccess = 0;
        int bigSize = batchSize > 0 ? batchSize : 2000;

        // 1. 大块分片
        List<List<E>> bigBatches = Lists.partition(entities, bigSize);

        for (List<E> bigBatch : bigBatches) {
            try {
                // 尝试大块批量插入
                totalSuccess += batchInsertFunc.apply(bigBatch);
            } catch (DataIntegrityViolationException e) {
                // 2. 大块失败 → 拆小组（50条）
                log.warn("大块批量插入失败 ({} 条)，拆分为小组重试...", bigBatch.size());
                int subSize = 50;
                List<List<E>> subBatches = Lists.partition(bigBatch, subSize);

                for (List<E> subBatch : subBatches) {
                    try {
                        totalSuccess += batchInsertFunc.apply(subBatch);
                    } catch (Exception subEx) {
                        // 3. 小组失败 → 逐条插入（精确定位脏数据）
                        log.warn("子批插入失败 ({} 条)，降级为逐条插入...", subBatch.size());
                        for (E entity : subBatch) {
                            try {
                                totalSuccess += singleInsertFunc.apply(entity);
                            } catch (DuplicateKeyException singleEx) {
                                // 精确捕获重复键异常
                                collector.addError(-1, "数据重复: " + singleEx.getMessage());
                            } catch (DataIntegrityViolationException singleEx) {
                                collector.addError(-1, "数据格式异常: " + singleEx.getMostSpecificCause().getMessage());
                            } catch (Exception singleEx) {
                                collector.addError(-1, "插入失败: " + singleEx.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 其他未知异常，整批记录错误
                log.error("批量插入未知异常", e);
                for (E entity : bigBatch) {
                    collector.addError(-1, "系统异常: " + e.getMessage());
                }
            }
        }
        return totalSuccess;
    }

}
