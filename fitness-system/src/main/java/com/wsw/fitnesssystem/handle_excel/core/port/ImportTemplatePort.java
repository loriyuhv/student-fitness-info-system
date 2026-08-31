package com.wsw.fitnesssystem.handle_excel.core.port;

import com.wsw.fitnesssystem.handle_excel.core.model.ImportTemplate;
import com.wsw.fitnesssystem.shared.exception.BizException;

/**
 * Excel 模板数据端口
 * <p>Core 层定义契约，Infrastructure 层实现</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:47
 * @since 1.0
 */
public interface ImportTemplatePort {

    /**
     * 根据业务类型获取模板配置
     *
     * @param bizType 业务类型（如 USER_IMPORT）
     * @return 模板领域对象
     * @throws BizException 当模板不存在时抛出
     */
    ImportTemplate getTemplate(String bizType);

    /**
     * 检查业务类型是否支持模板下载
     */
    boolean isTemplateSupported(String bizType);

}
