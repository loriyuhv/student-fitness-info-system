package com.wsw.fitnesssystem.shared.kernel.error;

import org.springframework.http.HttpStatus;

/**
 * 通用错误码（共享内核）。
 *
 * <p>跨模块共用的错误码：成功、参数校验、系统错误、文件操作、接口调用等。</p>
 * <p><b>注意：</b>本枚举暂时保留 {@code HttpStatus} 字段，P3 阶段将移除。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:40
 * @since 1.0
 */
public enum CommonErrorCode implements ErrorCode {

    /* ================= 成功 ================= */
    SUCCESS(200000, HttpStatus.OK, "操作成功"),

    /* ================= 参数 / 请求 ================= */
    PARAM_INVALID(400001, HttpStatus.BAD_REQUEST, "参数不合法"),
    PARAM_MISSING(400002, HttpStatus.BAD_REQUEST, "参数缺失"),
    PARAM_TYPE_ERROR(400003, HttpStatus.BAD_REQUEST, "参数类型错误"),
    REQUEST_FORMAT_ERROR(400004, HttpStatus.BAD_REQUEST, "请求格式错误"),

    /* ================= 文件 ================= */
    FILE_NOT_FOUND(404003, HttpStatus.NOT_FOUND, "文件不存在"),
    FILE_UPLOAD_ERROR(500003, HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(500004, HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败"),
    FILE_GENERATE_ERROR(500006, HttpStatus.INTERNAL_SERVER_ERROR, "文件生成失败"),

    /* ================= 通用冲突 ================= */
    DATA_ALREADY_EXISTS(409208, HttpStatus.CONFLICT, "数据已存在，请勿重复提交"),

    /* ================= 系统 ================= */
    SYSTEM_ERROR(500000, HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员"),
    DATABASE_ERROR(500001, HttpStatus.INTERNAL_SERVER_ERROR, "数据库操作异常"),
    CACHE_ERROR(500002, HttpStatus.INTERNAL_SERVER_ERROR, "缓存服务异常"),
    SERVER_TEMP_ERROR(500005, HttpStatus.INTERNAL_SERVER_ERROR, "系统处理异常，请稍候重试"),

    /* ================= 接口 / 第三方调用 ================= */
    INNER_INTERFACE_ERROR(600001, HttpStatus.INTERNAL_SERVER_ERROR, "内部系统接口调用异常"),
    OUTER_INTERFACE_ERROR(600002, HttpStatus.BAD_GATEWAY, "外部系统接口调用异常"),
    INTERFACE_FORBIDDEN(600003, HttpStatus.FORBIDDEN, "接口禁止访问"),
    INTERFACE_ADDRESS_INVALID(600004, HttpStatus.BAD_REQUEST, "接口地址无效"),
    INTERFACE_TIMEOUT(600005, HttpStatus.GATEWAY_TIMEOUT, "接口请求超时");

    private final int code;
    private final HttpStatus httpStatus;   // P3 移除
    private final String message;

    CommonErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    /**
     * P3 阶段移除
     */
    public HttpStatus httpStatus() {
        return httpStatus;
    }

}
