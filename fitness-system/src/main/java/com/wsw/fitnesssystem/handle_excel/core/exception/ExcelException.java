package com.wsw.fitnesssystem.handle_excel.core.exception;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * Excel 导入模块专用业务异常
 * 继承 BizException，统一走 GlobalExceptionHandler 处理
 * <p>使用场景：</p>
 * <li>1. Excel 解析失败（格式损坏、密码保护等）</li>
 * <li>2. 导入任务执行异常（线程池满、文件转存失败等）</li>
 * <li>3. 业务适配器内部异常包装</li>
 * @author loriyuhv
 * @version 1.0 2026/8/21 22:38
 * @since 1.0
 */
public class ExcelException extends BizException {

    public ExcelException(ResultCode resultCode) {
        super(resultCode);
    }

    public ExcelException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public ExcelException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    public ExcelException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

}
