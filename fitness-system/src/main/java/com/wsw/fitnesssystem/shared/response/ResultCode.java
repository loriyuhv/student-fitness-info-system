package com.wsw.fitnesssystem.shared.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * <p>接口统一返回状态码</p>
 * <p>规则：</p>
 * <ul>
 *     <li>code：业务状态码（前端识别）</li>
 *     <li>httpStatus：HTTP 状态</li>
 *     <li>message：默认提示信息</li>
 * </ul>
 *
 *  <p>编码规范（code 前缀 = HTTP 语义大类，第4位为模块标识）：</p>
 *  <ul>
 *      <li>200xxx 成功（登录/登出/踢人/解锁等）</li>
 *      <li>400xxx 参数 / 请求错误 4002xx 会话-请求错误</li>
 *      <li>401xxx 认证 / Token 4011xx Token / 4012xx 会话认证</li>
 *      <li>403xxx 权限 / 账号状态   4030xx 权限不足 / 4031xx 风控锁定</li>
 *      <li>404xxx 数据不存在        4041xx 体测 / 4042xx 会话</li>
 *      <li>409xxx 业务冲突</li>
 *      <li>422xxx 业务规则处理失败（体测计分/导入导出）</li>
 *      <li>500xxx 系统错误</li>
 *      <li>600xxx 内部 / 外部接口调用</li>
 *  </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:12
 * @since 1.0
 */
@Getter
public enum ResultCode {

    /* ================= 成功（仅允许真正的成功语义） ================= */
    SUCCESS(200000, HttpStatus.OK, "操作成功"),
    LOGOUT_SUCCESS(200101, HttpStatus.OK, "用户登出成功"),
    KICKOUT_SUCCESS(200102, HttpStatus.OK, "用户被踢出成功"),
    ACCOUNT_UNLOCKED(200103, HttpStatus.OK, "账号已解锁"),

    /* ===================================================================================== */
    /* ============================ AUTH 模块 xxx001~xxx099        ========================== */
    /* ===================================================================================== */

    /* ==============================  认证 / 登录 401xxx ============================= */
    // 对外统一"账号或密码错误"，不暴露账号是否存在（防枚举）；细分码仅供审计/日志
    AUTH_ACCOUNT_NOT_EXIST(401001, HttpStatus.UNAUTHORIZED, "认证用户账号不存在"),
    AUTH_PASSWORD_ERROR(401002, HttpStatus.UNAUTHORIZED, "认证用户密码错误"),
    AUTH_USER_NOT_LOGIN(401003, HttpStatus.UNAUTHORIZED, "认证用户未登录"),
    AUTH_CREDENTIAL_INVALID(401004, HttpStatus.UNAUTHORIZED, "认证用户登录凭证无效"),
    AUTH_USER_NOT_FOUND(401005, HttpStatus.UNAUTHORIZED, "认证用户不存在"),
    AUTH_CREDENTIAL_EXPIRED(401006, HttpStatus.UNAUTHORIZED, "认证用户登录凭证过期"),
    AUTH_USER_LOGIN_ERROR(401007, HttpStatus.UNAUTHORIZED, "账号或密码错误"),

    /* =========================== 业务冲突 / 状态异常 409xxx  ============================= */

    AUTH_ACCOUNT_ALREADY_EXIST(409001, HttpStatus.CONFLICT, "认证账号已存在"),

    /* ===================================================================================== */
    /* ==============================   TOKEN 模块 401101~401199  ========================== */
    /* ===================================================================================== */

    TOKEN_INVALID(401101, HttpStatus.UNAUTHORIZED, "Token无效"),
    TOKEN_EXPIRED(401102, HttpStatus.UNAUTHORIZED, "Token已过期"),
    TOKEN_SIGNATURE_ERROR(401103, HttpStatus.UNAUTHORIZED, "Token签名错误"),
    TOKEN_MALFORMED(401104, HttpStatus.UNAUTHORIZED, "Token格式错误"),
    TOKEN_VERSION_MISMATCH(401105, HttpStatus.UNAUTHORIZED, "Token版本失效"),
    TOKEN_BLACKLISTED(401106, HttpStatus.UNAUTHORIZED, "Token已加入黑名单"),
    REFRESH_TOKEN_INVALID(401111, HttpStatus.UNAUTHORIZED, "RefreshToken无效"),
    REFRESH_TOKEN_EXPIRED(401112, HttpStatus.UNAUTHORIZED, "RefreshToken已过期"),

    /* ===================================================================================== */
    /* ==============================  SESSION 模块 401201~401299 ========================== */
    /* ===================================================================================== */

    SESSION_ALREADY_OFFLINE(400201, HttpStatus.BAD_REQUEST, "会话已下线"),
    SESSION_NOT_FOUND(404201, HttpStatus.NOT_FOUND, "会话不存在"),
    SESSION_TOKEN_INVALID(401201, HttpStatus.UNAUTHORIZED, "会话Token无效"),
    SESSION_MAX_DEVICES_EXCEEDED(403201, HttpStatus.FORBIDDEN, "超过最大登录设备数"),

    /* ===================================================================================== */
    /* ============================== PERM 模块 403001~403099   ============================ */
    /* ===================================================================================== */

    PERMISSION_DENIED(403001, HttpStatus.FORBIDDEN, "权限不足"),
    ROLE_NOT_ASSIGNED(403002, HttpStatus.FORBIDDEN, "未分配角色"),
    PERMISSION_EXPIRED(403003, HttpStatus.FORBIDDEN, "权限已过期"),

    /* ===================================================================================== */
    /* ================================== RISK 模块 403101~403199 =========================== */
    /* ===================================================================================== */

    RISK_ACCOUNT_LOCKED(403101, HttpStatus.FORBIDDEN, "账号已被锁定"),
    RISK_ACCOUNT_DISABLED(403102, HttpStatus.FORBIDDEN, "账号已被禁用"),
    RISK_FAIL_THRESHOLD_EXCEEDED(403103, HttpStatus.FORBIDDEN, "失败次数已达上限"),
    RISK_CHECK_FAILED(403105, HttpStatus.FORBIDDEN, "风控检查不通过"),

    /* ===================================================================================== */
    /* ================================== USER 模块 404001~404099 ========================== */
    /* ===================================================================================== */

    USER_NOT_FOUND(404001, HttpStatus.NOT_FOUND, "用户不存在"),
    USER_ALREADY_EXIST(409003, HttpStatus.CONFLICT, "用户已存在"),

    /* ===================================================================================== */
    /* ================================== 参数/请求错误 400xxx =============================== */
    /* ===================================================================================== */

    PARAM_INVALID(400001, HttpStatus.BAD_REQUEST, "参数不合法"),
    PARAM_MISSING(400002, HttpStatus.BAD_REQUEST, "参数缺失"),
    PARAM_TYPE_ERROR(400003, HttpStatus.BAD_REQUEST, "参数类型错误"),
    REQUEST_FORMAT_ERROR(400004, HttpStatus.BAD_REQUEST, "请求格式错误"),

    /* ===================================================================================== */
    /* ================================== 数据不存在 404xxx ================================= */
    /* ===================================================================================== */

    ACCOUNT_NOT_EXIST(404002, HttpStatus.NOT_FOUND, "账号不存在"),
    FITNESS_DATA_NOT_FOUND(404101, HttpStatus.NOT_FOUND, "体测数据不存在"),
    FITNESS_DATA_ALREADY_EXIST(409101, HttpStatus.CONFLICT, "体测数据已存在"),
    FILE_NOT_FOUND(404003, HttpStatus.NOT_FOUND, "文件不存在"),
    IMPORT_TASK_NOT_FOUND(404105, HttpStatus.NOT_FOUND, "导入任务不存在"),


    /* ===================================================================================== */
    /* ================================== 业务冲突 409xxx ================================= */
    /* ===================================================================================== */

    TASK_CANCELLED(409002, HttpStatus.CONFLICT, "导入任务已被用户取消"),

    /* ================= 体测业务错误 422xxx ================= */

    FITNESS_SCORE_CALCULATE_ERROR(422101, HttpStatus.UNPROCESSABLE_ENTITY, "成绩计算失败"),
    FITNESS_DATA_IMPORT_ERROR(422102, HttpStatus.UNPROCESSABLE_ENTITY, "体测数据导入失败"),
    FITNESS_DATA_EXPORT_ERROR(422103, HttpStatus.UNPROCESSABLE_ENTITY, "体测数据导出失败"),

    /* ================= 系统错误 500xxx ================= */
    SYSTEM_ERROR(500000, HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员"),
    DATABASE_ERROR(500001, HttpStatus.INTERNAL_SERVER_ERROR, "数据库操作异常"),
    CACHE_ERROR(500002, HttpStatus.INTERNAL_SERVER_ERROR, "缓存服务异常"),
    FILE_UPLOAD_ERROR(500003, HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(500004, HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败"),
    SERVER_TEMP_ERROR(500005, HttpStatus.INTERNAL_SERVER_ERROR, "系统处理异常，请稍候重试"),
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
