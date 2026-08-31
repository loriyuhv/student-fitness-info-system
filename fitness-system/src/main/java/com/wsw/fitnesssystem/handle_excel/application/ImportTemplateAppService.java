package com.wsw.fitnesssystem.handle_excel.application;

import com.alibaba.excel.EasyExcel;
import com.wsw.fitnesssystem.handle_excel.core.model.ImportTemplate;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportTemplatePort;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 模板应用服务
 * <p>负责模板的获取、生成和下载</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:52
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTemplateAppService {

    private final ImportTemplatePort importTemplatePort;

    /**
     * 下载 Excel 导入模板
     *
     * @param bizType  业务类型
     * @param response HTTP 响应
     */
    public void downloadTemplate(String bizType, HttpServletResponse response) {
        // 1. 校验类型
        boolean templateSupported = importTemplatePort.isTemplateSupported(bizType);
        if (!templateSupported) {
            throw new BizException(ResultCode.PARAM_TYPE_ERROR, "模板不支持此类型");
        }
        // 2. 获取模板配置
        ImportTemplate template = importTemplatePort.getTemplate(bizType);

        // 3. 兜底校验（虽然 Port 层已经校验，但双重保障）
        if (!template.isValid()) {
            log.error("Invalid template for bizType: {}, missing required fields", bizType);
            throw new BizException(ResultCode.SYSTEM_ERROR, "模板配置不完整");
        }

        // 4. 设置响应头
        try {
            String fileName = URLEncoder.encode(template.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            // 1）获取生成Excel所需数据
            // 表头：列名
            List<List<String>> headRows = template.getHeadRows();

            // 数据：第一行：表头说明 第二+N行：示例数据（可能为空）
            List<List<String>> dataRows = template.getDataRows();

            // 2）生成Excel文件
            EasyExcel.write(response.getOutputStream())
                .head(headRows)
                .sheet(template.getSheetName() != null ? template.getSheetName() : "模板")
                .doWrite(dataRows);

            log.info("Template downloaded: {}, bizType: {}", template.getFileName(), bizType);

        } catch (IOException e) {
            log.error("Template download failed, bizType: {}", bizType, e);
            throw new BizException(ResultCode.FILE_DOWNLOAD_ERROR, "模板下载失败");
        }
    }

}
