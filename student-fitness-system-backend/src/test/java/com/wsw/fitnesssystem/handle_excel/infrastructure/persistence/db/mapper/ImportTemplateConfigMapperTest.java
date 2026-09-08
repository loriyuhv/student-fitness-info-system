package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper;

import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.entity.ImportTemplateConfigEntity;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.mapper.ImportTemplateConfigMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:12
 * @since 1.0
 */
@SpringBootTest
@Transactional
@DisplayName("ExcelTemplateConfigMapper 集成测试")
class ImportTemplateConfigMapperTest {

    @Autowired
    private ImportTemplateConfigMapper mapper;

    @Test
    @DisplayName("查询所有模板配置")
    void shouldSelectAll() {
        var list = mapper.selectList(null);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("根据业务类型查询 - JSON 自动映射")
    void shouldFindEnabledByBizType() {
        // When
        ImportTemplateConfigEntity entity = mapper.findEnabledByBizType("USER_IMPORT");

        // Then
        assertThat(entity).isNotNull();
        // 验证 JSON 字段被正确解析为 List
        assertThat(entity.getHeaders()).isInstanceOf(List.class);
        assertThat(entity.getRules()).isInstanceOf(List.class);
        assertThat(entity.getExamples()).isInstanceOf(List.class);
        // 验证内容
        assertThat(entity.getHeaders()).contains("校区", "用户账号");
        assertThat(entity.getRules()).anyMatch(rule -> rule.contains("必填"));
        assertThat(entity.getExamples().get(0)).hasSize(7);
    }

    @Test
    @DisplayName("插入模板配置 - JSON 自动序列化")
    void shouldInsertWithJsonFields() {
        // Given
        ImportTemplateConfigEntity entity = new ImportTemplateConfigEntity();
        entity.setBizType("TEST_INTEGRATION");
        entity.setFileName("test.xlsx");
        entity.setSheetName("测试");
        entity.setHeaders(List.of("姓名", "年龄"));
        entity.setRules(List.of("必填", "数字"));
        entity.setExamples(List.of(List.of("张三", "25"), List.of("李四", "30")));

        // When
        mapper.insert(entity);
        ImportTemplateConfigEntity result = mapper.findEnabledByBizType("TEST_INTEGRATION");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHeaders()).containsExactly("姓名", "年龄");
        assertThat(result.getExamples()).hasSize(2);
        assertThat(result.getExamples().get(0)).containsExactly("张三", "25");
    }

}