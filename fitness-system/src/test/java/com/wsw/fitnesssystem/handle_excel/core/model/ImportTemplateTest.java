package com.wsw.fitnesssystem.handle_excel.core.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 06:52
 * @since 1.0
 */
@Slf4j
@DisplayName("ImportTemplate 领域模型测试")
class ImportTemplateTest {

    @Test
    @DisplayName("构建完整模板数据：表头 + 规则 + 示例")
    void shouldBuildCompleteTemplate() {
        // Given
        ImportTemplate template = new ImportTemplate();
        template.setHeaders(List.of("校区", "用户账号", "密码"));
        template.setRules(List.of("必填", "必填", "选填"));
        template.setExamples(List.of(
            List.of("1001", "user1", "123456"),
            List.of("1002", "user2", "123456")
        ));

        // When
        List<List<String>> headRows = template.getHeadRows();
        List<List<String>> dataRows = template.getDataRows();

        // Then
        assertThat(headRows).hasSize(3);
        assertThat(headRows.get(0)).containsExactly("校区");
        assertThat(headRows.get(1)).containsExactly( "用户账号");
        assertThat(headRows.get(2)).containsExactly("密码");
        assertThat(dataRows).hasSize(3);
        assertThat(dataRows.get(0)).containsExactly("必填", "必填", "选填");
        assertThat(dataRows.get(1)).containsExactly("1001", "user1", "123456");
        assertThat(dataRows.get(2)).containsExactly("1002", "user2", "123456");
    }

    @Test
    @DisplayName("无示例数据时，数据行第一行有值，其他为空")
    void shouldReturnEmptyDataRows_whenNoExamples() {
        // Given
        ImportTemplate template = new ImportTemplate();
        template.setHeaders(List.of("校区", "用户账号"));
        template.setRules(List.of("必填", "必填"));
        template.setExamples(null);

        // When
        List<List<String>> dataRows = template.getDataRows();

        // Then
        assertThat(dataRows.get(0)).containsExactly("必填", "必填");
    }

    @Test
    @DisplayName("校验有效的模板配置")
    void shouldBeValid_whenAllFieldsPresent() {
        // Given
        ImportTemplate template = new ImportTemplate();
        template.setFileName("user_template.xlsx");
        template.setHeaders(List.of("校区", "用户账号"));
        template.setRules(List.of("必填", "必填"));

        // Then
        assertThat(template.isValid()).isTrue();
    }

    @Test
    @DisplayName("缺少 headers 时校验失败")
    void shouldBeInvalid_whenHeadersMissing() {
        // Given
        ImportTemplate template = new ImportTemplate();
        template.setFileName("user_template.xlsx");
        template.setHeaders(null);
        template.setRules(List.of("必填", "必填"));

        // Then
        assertThat(template.isValid()).isFalse();
    }

    @Test
    @DisplayName("缺少 rules 时校验失败")
    void shouldBeInvalid_whenRulesMissing() {
        // Given
        ImportTemplate template = new ImportTemplate();
        template.setFileName("user_template.xlsx");
        template.setHeaders(List.of("校区", "用户账号"));
        template.setRules(null);

        // Then
        assertThat(template.isValid()).isFalse();
    }

    @Test
    @DisplayName("缺少 fileName 时校验失败")
    void shouldBeInvalid_whenFileNameMissing() {
        // Given
        ImportTemplate template = new ImportTemplate();
        template.setFileName(null);
        template.setHeaders(List.of("校区", "用户账号"));
        template.setRules(List.of("必填", "必填"));

        // Then
        assertThat(template.isValid()).isFalse();
    }

}