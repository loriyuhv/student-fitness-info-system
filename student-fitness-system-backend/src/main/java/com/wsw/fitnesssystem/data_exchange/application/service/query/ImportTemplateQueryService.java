package com.wsw.fitnesssystem.data_exchange.application.service.query;

import cn.idev.excel.FastExcel;
import com.wsw.fitnesssystem.data_exchange.application.dto.result.ImportTemplateDownloadResult;
import com.wsw.fitnesssystem.data_exchange.domain.vo.ImportTemplate;
import com.wsw.fitnesssystem.data_exchange.application.port.output.TemplateConfigPort;
import com.wsw.fitnesssystem.shared.kernel.exception.BizException;
import com.wsw.fitnesssystem.shared.kernel.exception.SystemException;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 导入模板应用服务（读操作）。
 * <p>职责：根据业务类型读取模板配置，并在内存中生成对应的 Excel 字节内容。
 * 本服务<b>不接触任何 Web 技术栈</b>，返回 {@link ImportTemplateDownloadResult}
 * 交由接口层写入 HTTP 响应；因此可被其他入口（定时任务、MQ 消费等）复用。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:52
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTemplateQueryService {

    private final TemplateConfigPort templateConfigPort;

    /**
     * 获取指定业务类型的导入模板文件内容。
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验业务类型是否支持模板（不支持则抛 {@code BizException(PARAM_TYPE_ERROR)}）；</li>
     *   <li>从 {@link TemplateConfigPort} 加载模板配置（列头、示例数据、sheet 名）；</li>
     *   <li>兜底校验配置完整性（缺失必填字段则视为系统配置错误）；</li>
     *   <li>用 FastExcel 将模板写入内存 {@link ByteArrayOutputStream}，封装为
     *       {@link ImportTemplateDownloadResult} 返回。</li>
     * </ol>
     *
     * @param bizType 业务类型编码（如 {@code USER_IMPORT}），非空且需被模板体系支持
     * @return 模板文件元信息与二进制内容
     * @throws BizException    业务类型不支持（{@code PARAM_TYPE_ERROR}）
     *                         或模板配置不完整（{@code SYSTEM_ERROR}）
     * @throws SystemException Excel 写入过程发生 IO 异常
     */
    public ImportTemplateDownloadResult getTemplateFile(String bizType) {
        // 1. 校验类型是否被模板体系支持
        if (!templateConfigPort.isTemplateSupported(bizType)) {
            throw new BizException(CommonErrorCode.PARAM_TYPE_ERROR, "模板不支持此类型");
        }

        // 2. 获取模板配置（列头 / 示例数据 / sheet 名）
        ImportTemplate template = templateConfigPort.getTemplate(bizType);

        // 3. 兜底校验：Port 层已校验，此处作为第二道防线
        if (!template.isValid()) {
            log.error("Invalid template config, bizType={}", bizType);
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "模板配置不完整");
        }

        // 4. 生成 Excel 到内存（不落盘，直接返回字节）
        List<List<String>> headRows = template.getHeadRows();
        List<List<String>> dataRows = template.getDataRows();
        String sheetName = template.getSheetName() != null ? template.getSheetName() : "模板";

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            FastExcel.write(bos)
                .head(headRows)
                .sheet(sheetName)
                .doWrite(dataRows);

            log.info("Template generated: fileName={}, bizType={}", template.getFileName(), bizType);
            return ImportTemplateDownloadResult.builder()
                .fileName(template.getFileName())
                .content(bos.toByteArray())
                .build();
        } catch (IOException e) {
            log.error("Failed to generate template, bizType={}", bizType, e);
            throw new SystemException(CommonErrorCode.FILE_GENERATE_ERROR, e);
        }
    }

}
