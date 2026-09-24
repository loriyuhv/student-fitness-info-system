package com.wsw.fitnesssystem.shared.kernel.error;

/**
 * 通用错误码（共享内核）。
 *
 * <p><b>职责：</b>承载跨限界上下文共用的错误码。凡是「任何模块都可能抛出」的错误
 * 都属于本枚举；模块专属的错误码放在各自的 {@code {module}.error} 包中。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>成功：</b>{@link #SUCCESS}</li>
 *   <li><b>参数校验：</b>参数不合法、缺失、类型错误、请求格式错误</li>
 *   <li><b>文件操作：</b>文件不存在、上传/下载/生成失败</li>
 *   <li><b>数据冲突：</b>数据已存在（通用兜底）</li>
 *   <li><b>系统错误：</b>系统异常、数据库异常、缓存异常、临时故障</li>
 *   <li><b>接口调用：</b>内部/外部接口异常、超时、禁用、地址无效</li>
 * </ul>
 *
 * <p><b>归属判断：</b></p>
 * <ul>
 *   <li>✅ 放本枚举：{@code common.param.invalid}、{@code common.system.error}</li>
 *   <li>❌ 不放本枚举：{@code iam.token.expired}（iam 专属）、
 *       {@code user.phone.already_exists}（user 专属）</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code common.{子域}.{错误}}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:40
 * @since 1.0
 */
public enum CommonErrorCode implements ErrorCode {

    /* ================= 成功 ================= */

    /**
     * 通用成功
     */
    SUCCESS("common.success", "操作成功"),

    /* ================= 参数 / 请求 ================= */

    /**
     * 参数不合法（如格式错误、取值范围越界）
     */
    PARAM_INVALID("common.param.invalid", "参数不合法"),

    /**
     * 缺少必填参数
     */
    PARAM_MISSING("common.param.missing", "参数缺失"),

    /**
     * 参数类型错误（如期望数字传入字符串）
     */
    PARAM_TYPE_ERROR("common.param.type_error", "参数类型错误"),

    /**
     * 请求体格式错误（如 JSON 非法）
     */
    REQUEST_FORMAT_ERROR("common.request.format_error", "请求格式错误"),

    /* ================= 文件 ================= */

    /**
     * 文件不存在
     */
    FILE_NOT_FOUND("common.file.not_found", "文件不存在"),

    /**
     * 文件上传失败（存储层故障）
     */
    FILE_UPLOAD_ERROR("common.file.upload_error", "文件上传失败"),

    /**
     * 文件下载失败
     */
    FILE_DOWNLOAD_ERROR("common.file.download_error", "文件下载失败"),

    /**
     * 文件生成失败（如导出 Excel）
     */
    FILE_GENERATE_ERROR("common.file.generate_error", "文件生成失败"),

    /* ================= 通用冲突 ================= */

    /**
     * 数据已存在（通用兜底，具体场景应使用模块专属错误码）
     */
    DATA_ALREADY_EXISTS("common.data.already_exists", "数据已存在，请勿重复提交"),

    /* ================= 系统 ================= */

    /**
     * 系统异常（兜底）
     */
    SYSTEM_ERROR("common.system.error", "系统异常，请联系管理员"),

    /**
     * 数据库操作异常
     */
    DATABASE_ERROR("common.system.database_error", "数据库操作异常"),

    /**
     * 缓存服务异常
     */
    CACHE_ERROR("common.system.cache_error", "缓存服务异常"),

    /**
     * 数据序列化失败
     */
    SERIALIZATION_ERROR("common.serialization.error", "数据序列化失败"),

    /**
     * 系统临时故障（可重试）
     */
    SERVER_TEMP_ERROR("common.system.temp_error", "系统处理异常，请稍候重试"),

    /* ================= 接口 / 第三方调用 ================= */

    /**
     * 内部系统接口调用异常
     */
    INNER_INTERFACE_ERROR("common.interface.inner_error", "内部系统接口调用异常"),

    /**
     * 外部系统接口调用异常
     */
    OUTER_INTERFACE_ERROR("common.interface.outer_error", "外部系统接口调用异常"),

    /**
     * 接口禁止访问
     */
    INTERFACE_FORBIDDEN("common.interface.forbidden", "接口禁止访问"),

    /**
     * 接口地址无效
     */
    INTERFACE_ADDRESS_INVALID("common.interface.address_invalid", "接口地址无效"),

    /**
     * 接口请求超时
     */
    INTERFACE_TIMEOUT("common.interface.timeout", "接口请求超时");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
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

}
