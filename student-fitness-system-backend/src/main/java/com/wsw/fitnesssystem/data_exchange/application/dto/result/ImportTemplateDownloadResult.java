package com.wsw.fitnesssystem.data_exchange.application.dto.result;

import lombok.Builder;
import lombok.Getter;

/**
 * 导入模板下载结果（应用层内部返回）。
 * <p>承载模板文件的元信息与二进制内容，供接口层写入 HTTP 响应流。
 * 应用层不直接接触 {@code HttpServletResponse}，故以本对象作为
 * “生成”与“下载”两步之间的数据载体。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 00:55
 * @since 1.0
 */
@Getter
@Builder
public class ImportTemplateDownloadResult {

    /**
     * 建议的下载文件名（含扩展名，如 {@code 用户导入模板.xlsx}）。
     * <p>不含路径分隔符；由接口层负责 URL 编码后写入 Content-Disposition。</p>
     */
    private final String fileName;

    /**
     * 文件二进制内容（Excel 字节流）。
     * <p>由应用层在内存中生成，避免中间落盘；典型大小 &lt; 1MB。</p>
     */
    private final byte[] content;

}
