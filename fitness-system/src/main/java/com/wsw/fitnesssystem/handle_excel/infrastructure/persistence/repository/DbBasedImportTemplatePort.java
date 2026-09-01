package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.handle_excel.core.model.ImportTemplate;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportTemplatePort;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.entity.ImportTemplateConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 基于数据库的 Excel 模板端口实现
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:49
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class DbBasedImportTemplatePort implements ImportTemplatePort {

    private final ImportTemplateConfigRepository repository;

    @Override
    public ImportTemplate getTemplate(String bizType) {
        ImportTemplateConfigEntity entity = repository.findOrThrow(bizType);
        return convertToDomain(entity);
    }

    @Override
    public boolean isTemplateSupported(String bizType) {
        return repository.exists(bizType);
    }

    /**
     * Entity → Domain 转换
     */
    private ImportTemplate convertToDomain(ImportTemplateConfigEntity entity) {
        ImportTemplate template = new ImportTemplate();
        template.setBizType(entity.getBizType());
        template.setFileName(entity.getFileName());
        template.setSheetName(entity.getSheetName());
        template.setHeaders(entity.getHeaders());
        template.setRules(entity.getRules());
        template.setExamples(entity.getExamples());
        return template;
    }

}
