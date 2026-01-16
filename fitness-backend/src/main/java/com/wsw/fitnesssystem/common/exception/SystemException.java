package com.wsw.fitnesssystem.common.exception;

import com.wsw.fitnesssystem.interfaces.response.ResultCode;

/**
 * 系统异常（兜底）
 * - 用于不可预期错误：DB、IO、第三方接口
 * - 一般不会主动 throw，而是包装
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
public class SystemException extends BaseException {
    public SystemException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
