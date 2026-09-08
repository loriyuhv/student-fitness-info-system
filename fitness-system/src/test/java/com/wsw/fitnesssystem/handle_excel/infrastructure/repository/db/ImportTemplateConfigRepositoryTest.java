package com.wsw.fitnesssystem.handle_excel.infrastructure.repository.db;

import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.entity.ImportTemplateConfigEntity;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.repository.ImportTemplateConfigRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 02:33
 * @since 1.0
 */
@Slf4j
@SpringBootTest
// 测试后自动回滚
@Transactional
@DisplayName("ImportTemplateConfigRepository 单元测试")
class ImportTemplateConfigRepositoryTest {

    @Autowired
    private ImportTemplateConfigRepository repository;

    @Test
    @DisplayName("根据业务类型查询模板配置 - 成功")
    void shouldFindByBizType_whenExists() {
        // Given: 预先插入的 USER_IMPORT 模板数据
        // When
        ImportTemplateConfigEntity entity = repository.findByBizType("USER_IMPORT");

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getBizType()).isEqualTo("USER_IMPORT");
        assertThat(entity.getHeaders()).containsExactly(
            "校区", "用户账号", "密码", "昵称", "手机号码", "邮箱", "用户类型"
        );
        assertThat(entity.getRules()).hasSize(7);
        assertThat(entity.getExamples()).hasSize(2);
    }

    @Test
    @DisplayName("根据业务类型查询模板配置 - 不存在时返回 null")
    void shouldReturnNull_whenBizTypeNotFound() {
        // When
        ImportTemplateConfigEntity entity = repository.findByBizType("NOT_EXIST_TYPE");

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("查询模板配置 - 存在则返回，不存在则抛出异常")
    void shouldThrowException_whenBizTypeNotFound() {
        // When & Then
        assertThatThrownBy(() -> {
            ImportTemplateConfigEntity result = repository.findOrThrow("NOT_EXIST_TYPE");
            assertThat(result).isNull();
        }).isInstanceOf(BizException.class).hasMessageContaining("Template not configured");
    }

    @Test
    @DisplayName("保存模板配置 - 新增")
    void shouldInsert_whenIdIsNull() {
        // Given
        ImportTemplateConfigEntity entity = new ImportTemplateConfigEntity();
        entity.setBizType("TEST_TYPE");
        entity.setFileName("test_template.xlsx");
        entity.setSheetName("测试模板");
        entity.setHeaders(List.of("列1", "列2", "列3"));
        entity.setRules(List.of("必填", "选填", "数字"));
        entity.setExamples(List.of(
            List.of("A1", "B1", "C1"),
            List.of("A2", "B2", "C2")
        ));

        // When
        repository.saveOrUpdate(entity);
        ImportTemplateConfigEntity result = repository.findByBizType("TEST_TYPE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBizType()).isEqualTo("TEST_TYPE");
        assertThat(result.getHeaders()).hasSize(3);
    }

    @Test
    @DisplayName("删除模板配置 - 逻辑删除")
    void shouldDisable_whenDelete() {
        // Given
        String bizType = "USER_IMPORT";

        // When
        repository.deleteByBizType(bizType);
        ImportTemplateConfigEntity entity = repository.findByBizType(bizType);

        // Then
        assertThat(entity).isNull();  // 查询时过滤了 enabled=0
    }

    @Test
    @DisplayName("检查模板配置是否存在")
    void shouldReturnTrue_whenExists() {
        assertThat(repository.exists("USER_IMPORT")).isTrue();
        assertThat(repository.exists("NOT_EXIST_TYPE")).isFalse();
    }

}