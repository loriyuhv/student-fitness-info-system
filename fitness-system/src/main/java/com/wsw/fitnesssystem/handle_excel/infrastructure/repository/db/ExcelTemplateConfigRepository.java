package com.wsw.fitnesssystem.handle_excel.infrastructure.repository.db;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.ExcelTemplateConfigEntity;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelTemplateConfigMapper;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 02:27
 * @since 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ExcelTemplateConfigRepository {

    private final ExcelTemplateConfigMapper mapper;

    /**
     * 根据业务类型查询模板配置
     * @param bizType 业务类型
     * @return 模板配置实体，如果不存在返回 null
     */
    public ExcelTemplateConfigEntity findByBizType(String bizType) {
        return mapper.findEnabledByBizType(bizType);
    }

    /**
     * 查询模板配置，如果不存在则抛出异常
     */
    public ExcelTemplateConfigEntity findOrThrow(String bizType) {
        ExcelTemplateConfigEntity entity = findByBizType(bizType);
        if (entity == null) {
            log.warn("Template config not found for bizType: {}", bizType);
            throw new BizException(ResultCode.PARAM_INVALID,
                "Template not configured for import type: " + bizType);
        }
        return entity;
    }

    /**
     * 保存或更新模板配置
     */
    public void saveOrUpdate(ExcelTemplateConfigEntity entity) {
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
    }

    /**
     * 检查模板配置是否存在
     */
    public boolean exists(String bizType) {
        return mapper.findEnabledByBizType(bizType) != null;
    }

    /**
     * 删除模板配置（逻辑删除，设置 status = 0）
     */
    public void deleteByBizType(String bizType) {
        mapper.update(null,
            new LambdaUpdateWrapper<ExcelTemplateConfigEntity>()
                .eq(ExcelTemplateConfigEntity::getBizType, bizType)
                .set(ExcelTemplateConfigEntity::getStatus, 0)
                .set(ExcelTemplateConfigEntity::getDeleted, 1)
        );
        log.info("Template disabled for bizType: {}", bizType);
    }

}
