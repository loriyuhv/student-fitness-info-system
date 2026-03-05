package com.wsw.fitnesssystem.shared.exception;

import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * 业务异常
 * - 用于参数校验失败、状态不合法、权限不足等
 * - 语义：请求合法，但业务不成立
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
public class BizException extends BaseException {
    public BizException(ResultCode resultCode) {
        super(resultCode);
    }

    public BizException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
