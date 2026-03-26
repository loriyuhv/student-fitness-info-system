package com.wsw.fitnesssystem.shared.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 接口统一返回状态码
 * 规则：
 *  - code：业务状态码（前端识别）
 *  - httpStatus：HTTP 状态
 *  - message：默认提示信息
 * 编码规范：
 *  200xxx  成功
 *  400xxx  参数 / 请求错误
 *  401xxx  认证 / Token
 *  403xxx  权限
 *  404xxx  数据不存在
 *  500xxx  系统错误
 *  600xxx  外部 / 内部接口调用
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:12
 * @since 1.0
 */
@Getter
public enum ResultCode {

    /* ================= 成功 ================= */
    SUCCESS(200000, HttpStatus.OK, "操作成功"),
    LOGOUT_SUCCESS(200101, HttpStatus.OK, "用户登出成功"),
    KICKOUT_SUCCESS(200102, HttpStatus.OK, "用户被踢出成功"),

    /* ================= 参数 / 请求错误 400xxx ================= */
    PARAM_INVALID(400001, HttpStatus.BAD_REQUEST, "参数不合法"),
    PARAM_MISSING(400002, HttpStatus.BAD_REQUEST, "参数缺失"),
    PARAM_TYPE_ERROR(400003, HttpStatus.BAD_REQUEST, "参数类型错误"),
    REQUEST_FORMAT_ERROR(400004, HttpStatus.BAD_REQUEST, "请求格式错误"),

    /* ================= 认证 / 登录 401xxx ================= */
    USER_NOT_LOGIN(401001, HttpStatus.UNAUTHORIZED, "用户未登录"),
    USER_LOGIN_ERROR(401002, HttpStatus.UNAUTHORIZED, "账号或密码错误"),
    // 和SpringSecurity的BadCredentialsException语义相同
    PASSWORD_ERROR(401003, HttpStatus.UNAUTHORIZED, "密码错误"),
    TOKEN_EXPIRED(401101, HttpStatus.UNAUTHORIZED, "Token已过期"),
    TOKEN_INVALID(401102, HttpStatus.UNAUTHORIZED, "Token无效"),
    TOKEN_SIGNATURE_ERROR(401103, HttpStatus.UNAUTHORIZED, "Token签名错误"),
    TOKEN_MALFORMED(401104, HttpStatus.UNAUTHORIZED, "Token格式错误"),

    /* ================= 权限 / 访问控制 403xxx ================= */
    PERMISSION_DENIED(403001, HttpStatus.FORBIDDEN, "权限不足"),
    ROLE_NOT_ASSIGNED(403002, HttpStatus.FORBIDDEN, "未分配角色"),
    ACCOUNT_DISABLED(403003, HttpStatus.FORBIDDEN, "账号已被禁用"),
    ACCOUNT_LOCKED(403004, HttpStatus.FORBIDDEN, "账号已被锁定"),
    PERMISSION_EXPIRED(403005, HttpStatus.FORBIDDEN, "权限已过期"),

    /* ================= 数据不存在 404xxx ================= */
    USER_NOT_EXIST(404001, HttpStatus.NOT_FOUND, "用户不存在"),
    ACCOUNT_NOT_EXIST(404002, HttpStatus.NOT_FOUND, "账号不存在"),
    FITNESS_DATA_NOT_FOUND(404101, HttpStatus.NOT_FOUND, "体测数据不存在"),

    /* ================= 业务冲突 / 状态异常 409xxx ================= */
    USER_ALREADY_EXIST(409001, HttpStatus.CONFLICT, "用户已存在"),
    ACCOUNT_ALREADY_EXIST(409002, HttpStatus.CONFLICT, "账号已存在"),
    FITNESS_DATA_ALREADY_EXIST(409101, HttpStatus.CONFLICT, "体测数据已存在"),

    /* ================= 体测业务错误 422xxx ================= */
    FITNESS_SCORE_CALCULATE_ERROR(422101, HttpStatus.UNPROCESSABLE_ENTITY, "成绩计算失败"),
    FITNESS_DATA_IMPORT_ERROR(422102, HttpStatus.UNPROCESSABLE_ENTITY, "体测数据导入失败"),
    FITNESS_DATA_EXPORT_ERROR(422103, HttpStatus.UNPROCESSABLE_ENTITY, "体测数据导出失败"),

    /* ================= 系统错误 500xxx ================= */
    SYSTEM_ERROR(500000, HttpStatus.INTERNAL_SERVER_ERROR, "系统异常"),
    DATABASE_ERROR(500001, HttpStatus.INTERNAL_SERVER_ERROR, "数据库操作异常"),
    CACHE_ERROR(500002, HttpStatus.INTERNAL_SERVER_ERROR, "缓存服务异常"),
    FILE_UPLOAD_ERROR(500003, HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(500004, HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败"),
    LOGOUT_FAILED(500101, HttpStatus.INTERNAL_SERVER_ERROR, "用户登出失败"),
    KICKOUT_FAILED(500102, HttpStatus.INTERNAL_SERVER_ERROR, "用户被踢出失败"),

    /* ================= 接口 / 第三方调用 600xxx ================= */
    INNER_INTERFACE_ERROR(600001, HttpStatus.INTERNAL_SERVER_ERROR, "内部系统接口调用异常"),
    OUTER_INTERFACE_ERROR(600002, HttpStatus.BAD_GATEWAY, "外部系统接口调用异常"),
    INTERFACE_FORBIDDEN(600003, HttpStatus.FORBIDDEN, "接口禁止访问"),
    INTERFACE_ADDRESS_INVALID(600004, HttpStatus.BAD_REQUEST, "接口地址无效"),
    INTERFACE_TIMEOUT(600005, HttpStatus.GATEWAY_TIMEOUT, "接口请求超时");

    /** 业务状态码 */
    private final Integer code;
    /** HTTP 状态 */
    private final HttpStatus httpStatus;
    /** 默认提示信息 */
    private final String message;

    ResultCode(Integer code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int httpCode() {
        return httpStatus.value();
    }
}

