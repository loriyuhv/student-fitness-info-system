package com.wsw.fitnesssystem.shared.exception;

import com.wsw.fitnesssystem.shared.common.exception.BizException;
import com.wsw.fitnesssystem.shared.common.exception.SystemException;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * - 负责将异常 → HTTP 响应
 * - 是“异常的最后一站”
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:23
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 业务异常 → 400
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Object> handleBizException(BizException e) {
        ResultCode rc = e.getResultCode();
        log.warn("业务异常: {}", rc.getMessage());
        return ApiResult.error(rc);
    }

    /**
     * 系统异常 → 500
     */
    @ExceptionHandler(SystemException.class)
    public ApiResult<Object> handleSystemException(SystemException e) {
        ResultCode rc = e.getResultCode();
        log.error("系统异常: {}", rc.getMessage(), e);
        return ApiResult.error(rc);
    }

    /**
     * 未捕获异常 → 系统兜底
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleUnknownException(Exception e) {
        log.error("未知异常", e);
        return ApiResult.error(ResultCode.SYSTEM_ERROR);
    }
}
