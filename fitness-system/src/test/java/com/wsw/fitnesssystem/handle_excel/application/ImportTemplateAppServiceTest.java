package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.core.model.ImportTemplate;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportTemplatePort;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 07:01
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImportTemplateAppService 单元测试")
class ImportTemplateAppServiceTest {

    @Mock
    private ImportTemplatePort importTemplatePort;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ImportTemplateAppService service;

    @Test
    @DisplayName("下载模板成功")
    void shouldDownloadTemplate_whenTemplateExists() throws IOException {
        // Given
        String bizType = "USER_IMPORT";
        ImportTemplate template = createValidTemplate();
        when(importTemplatePort.isTemplateSupported(bizType)).thenReturn(true);
        when(importTemplatePort.getTemplate(bizType)).thenReturn(template);

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        // When
        service.downloadTemplate(bizType, response);

        // Then
        verify(importTemplatePort, times(1)).isTemplateSupported(bizType);
        verify(importTemplatePort, times(1)).getTemplate(bizType);
        verify(response, times(1)).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response, times(1)).setHeader(anyString(), anyString());
    }

    @Test
    @DisplayName("模板类型不支持时抛出 BizException")
    void shouldThrowException_whenTemplateNotSupported() {
        // Given
        String bizType = "NOT_SUPPORTED";
        when(importTemplatePort.isTemplateSupported(bizType)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> service.downloadTemplate(bizType, response))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.PARAM_TYPE_ERROR);

        verify(importTemplatePort, never()).getTemplate(anyString());
    }

    @Test
    @DisplayName("模板配置不完整时抛出 BizException")
    void shouldThrowException_whenTemplateInvalid() {
        // Given
        String bizType = "USER_IMPORT";
        ImportTemplate invalidTemplate = new ImportTemplate();
        invalidTemplate.setHeaders(null);  // headers 为空
        invalidTemplate.setRules(List.of("必填", "必填"));
        invalidTemplate.setFileName("test.xlsx");

        when(importTemplatePort.isTemplateSupported(bizType)).thenReturn(true);
        when(importTemplatePort.getTemplate(bizType)).thenReturn(invalidTemplate);

        // When & Then
        assertThatThrownBy(() -> service.downloadTemplate(bizType, response))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.SYSTEM_ERROR);

        verify(importTemplatePort, times(1)).getTemplate(bizType);
    }

    @Test
    @DisplayName("IO 异常时抛出 BizException")
    void shouldThrowException_whenIOExceptionOccurs() throws IOException {
        // Given
        String bizType = "USER_IMPORT";
        ImportTemplate template = createValidTemplate();
        when(importTemplatePort.isTemplateSupported(bizType)).thenReturn(true);
        when(importTemplatePort.getTemplate(bizType)).thenReturn(template);
        when(response.getOutputStream()).thenThrow(new IOException("IO error"));

        // When & Then
        assertThatThrownBy(() -> service.downloadTemplate(bizType, response))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.FILE_DOWNLOAD_ERROR);

        verify(importTemplatePort, times(1)).isTemplateSupported(bizType);
        verify(importTemplatePort, times(1)).getTemplate(bizType);
    }

    private ImportTemplate createValidTemplate() {
        ImportTemplate template = new ImportTemplate();
        template.setBizType("USER_IMPORT");
        template.setFileName("user_import_template.xlsx");
        template.setSheetName("用户导入模板");
        template.setHeaders(List.of("校区", "用户账号", "密码", "昵称", "手机号码", "邮箱", "用户类型"));
        template.setRules(List.of(
            "必填，数字",
            "必填，长度≤50",
            "必填，≥6位",
            "选填，≤20位",
            "选填，11位数字",
            "选填，支持@163.com/@126.com/@qq.com/@gmail.com",
            "必填，0-管理员/1-教师/2-学生"
        ));
        template.setExamples(List.of(
            List.of("1001", "20214202", "123456", "张三", "13800138000", "zhangsan@163.com", "2"),
            List.of("1002", "20214176", "123456", "李四", "13900139000", "lisi@qq.com", "1")
        ));
        return template;
    }

}