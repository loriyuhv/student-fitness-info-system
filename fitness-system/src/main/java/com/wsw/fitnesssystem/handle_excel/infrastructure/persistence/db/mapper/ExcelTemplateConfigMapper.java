package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.ExcelTemplateConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 02:24
 * @since 1.0
 */
@Mapper
public interface ExcelTemplateConfigMapper extends BaseMapper<ExcelTemplateConfigEntity> {

    /**
     * 根据业务类型查询启用的模板配置
     *
     * @param bizType 业务类型
     * @return 模板配置
     */
    default ExcelTemplateConfigEntity findEnabledByBizType(@Param("bizType") String bizType) {
        return selectOne(
            new LambdaQueryWrapper<ExcelTemplateConfigEntity>()
                .eq(ExcelTemplateConfigEntity::getBizType, bizType)
                .eq(ExcelTemplateConfigEntity::getStatus, 1)
                .eq(ExcelTemplateConfigEntity::getDeleted, 0)
        );
    }

}
