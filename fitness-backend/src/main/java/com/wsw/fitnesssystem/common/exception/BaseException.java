package com.wsw.fitnesssystem.common.exception;

import com.wsw.fitnesssystem.interfaces.response.ResultCode;
import lombok.Getter;

/**
 * 异常基类
 * - 所有自定义异常的父类
 * - 不关心 HTTP、Controller、接口协议
 * - 只承载“错误语义”
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
@Getter
public abstract class BaseException extends RuntimeException {
    protected final ResultCode resultCode;

    protected BaseException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    protected BaseException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
    }
}

