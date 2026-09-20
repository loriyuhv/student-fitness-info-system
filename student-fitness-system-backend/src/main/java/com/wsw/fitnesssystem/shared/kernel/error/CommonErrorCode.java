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
    SUCCESS("common.success", HttpStatus.OK, "操作成功"),

    /* ================= 参数 / 请求 ================= */
    PARAM_INVALID("common.param.invalid", HttpStatus.BAD_REQUEST, "参数不合法"),
    PARAM_MISSING("common.param.missing", HttpStatus.BAD_REQUEST, "参数缺失"),
    PARAM_TYPE_ERROR("common.param.type_error", HttpStatus.BAD_REQUEST, "参数类型错误"),
    REQUEST_FORMAT_ERROR("common.request.format_error", HttpStatus.BAD_REQUEST, "请求格式错误"),

    /* ================= 文件 ================= */
    FILE_NOT_FOUND("common.file.not_found", HttpStatus.NOT_FOUND, "文件不存在"),
    FILE_UPLOAD_ERROR("common.file.upload_error", HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败"),
    FILE_DOWNLOAD_ERROR("common.file.download_error", HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败"),
    FILE_GENERATE_ERROR("common.file.generate_error", HttpStatus.INTERNAL_SERVER_ERROR, "文件生成失败"),

    /* ================= 通用冲突 ================= */
    DATA_ALREADY_EXISTS("common.data.already_exists", HttpStatus.CONFLICT, "数据已存在，请勿重复提交"),

    /* ================= 系统 ================= */
    SYSTEM_ERROR("common.system.error", HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员"),
    DATABASE_ERROR("common.system.database_error", HttpStatus.INTERNAL_SERVER_ERROR, "数据库操作异常"),
    CACHE_ERROR("common.system.cache_error", HttpStatus.INTERNAL_SERVER_ERROR, "缓存服务异常"),
    SERVER_TEMP_ERROR("common.system.temp_error", HttpStatus.INTERNAL_SERVER_ERROR, "系统处理异常，请稍候重试"),

    /* ================= 接口 / 第三方调用 ================= */
    INNER_INTERFACE_ERROR("common.interface.inner_error", HttpStatus.INTERNAL_SERVER_ERROR, "内部系统接口调用异常"),
    OUTER_INTERFACE_ERROR("common.interface.outer_error", HttpStatus.BAD_GATEWAY, "外部系统接口调用异常"),
    INTERFACE_FORBIDDEN("common.interface.forbidden", HttpStatus.FORBIDDEN, "接口禁止访问"),
    INTERFACE_ADDRESS_INVALID("common.interface.address_invalid", HttpStatus.BAD_REQUEST, "接口地址无效"),
    INTERFACE_TIMEOUT("common.interface.timeout", HttpStatus.GATEWAY_TIMEOUT, "接口请求超时");

    private final String code;
    private final HttpStatus httpStatus;   // P6 移除
    private final String message;

    CommonErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

}
