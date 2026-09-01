package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.entity.ImportTemplateConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 02:24
 * @since 1.0
 */
@Mapper
public interface ImportTemplateConfigMapper extends BaseMapper<ImportTemplateConfigEntity> {

    /**
     * 根据业务类型查询启用的模板配置
     *
     * @param bizType 业务类型
     * @return 模板配置
     */
    default ImportTemplateConfigEntity findEnabledByBizType(@Param("bizType") String bizType) {
        return selectOne(
            new LambdaQueryWrapper<ImportTemplateConfigEntity>()
                .eq(ImportTemplateConfigEntity::getBizType, bizType)
                .eq(ImportTemplateConfigEntity::getStatus, 1)
                .eq(ImportTemplateConfigEntity::getDeleted, 0)
        );
    }

}
